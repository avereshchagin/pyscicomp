/*
 * Copyright 2000-2012 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jetbrains.pyscicomp.codeInsight.types;

import com.intellij.psi.PsiElement;
import com.jetbrains.pyscicomp.documentation.DocStringParameter;
import com.jetbrains.pyscicomp.documentation.NumpyDocString;
import com.jetbrains.python.psi.PyFunction;
import com.jetbrains.python.psi.PyNamedParameter;
import com.jetbrains.python.psi.PyQualifiedExpression;
import com.jetbrains.python.psi.types.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Provides type information extracted from Numpy docstring format.
 */
public class NumpyDocTypeProvider extends PyTypeProviderBase {

  private static final Pattern NUMPY_UNION_PATTERN = Pattern.compile("^\\{(.*)\\}$");

  private static final Map<String, String> NUMPY_ALIAS_TO_REAL_TYPE = new HashMap<String, String>();

  static {
    // 184 occurrences
    NUMPY_ALIAS_TO_REAL_TYPE.put("array_like", "collections.Iterable or int or long or float or complex or bool");
    // Parameters marked as 'data-type' actually get any Python type identifier such as 'bool' or
    // an instance of numpy.core.multiarray.dtype, however PyTypeChecker isn't able to check it.
    // 30 occurrences
    NUMPY_ALIAS_TO_REAL_TYPE.put("data-type", "object");
    // 16 occurrences
    NUMPY_ALIAS_TO_REAL_TYPE.put("scalar", "int or long or float or complex or bool");
    // 10 occurrences
    NUMPY_ALIAS_TO_REAL_TYPE.put("array", "collections.Iterable");
    // 9 occurrences
    NUMPY_ALIAS_TO_REAL_TYPE.put("any", "object");
    // 5 occurrences
    NUMPY_ALIAS_TO_REAL_TYPE.put("Standard Python scalar object", "int or long or float or complex or bool");
    // 4 occurrences
    NUMPY_ALIAS_TO_REAL_TYPE.put("Python type", "object");
    // 3 occurrences
    NUMPY_ALIAS_TO_REAL_TYPE.put("callable", "collections.Callable");
    // 3 occurrences
    NUMPY_ALIAS_TO_REAL_TYPE.put("number", "int or long or float");
  }

  @NotNull
  private static String cleanupOptional(@NotNull String typeString) {
    int index = typeString.indexOf(", optional");
    if (index >= 0) {
      return typeString.substring(0, index);
    }
    return typeString;
  }

  @NotNull
  private static List<String> getNumpyUnionType(@NotNull String typeString) {
    Matcher matcher = NUMPY_UNION_PATTERN.matcher(typeString);
    if (matcher.matches()) {
      typeString = matcher.group(1);
    }
    return Arrays.asList(typeString.split(" *, *"));
  }

  @Nullable
  private static PyType parseSingleNumpyDocType(@NotNull PsiElement anchor, @NotNull String typeString) {
    String realTypeName = NUMPY_ALIAS_TO_REAL_TYPE.get(typeString);
    if (realTypeName != null) {
      PyType type = PyTypeParser.getTypeByName(anchor, realTypeName);
      if (type != null) {
        return type;
      }
    }
    return PyTypeParser.getTypeByName(anchor, typeString);
  }

  @Nullable
  private static PyType parseNumpyDocType(@NotNull PsiElement anchor, @NotNull String typeString) {
    typeString = NumpyDocString.cleanupOptional(typeString);
    Set<PyType> types = new LinkedHashSet<PyType>();
    for (String typeName : NumpyDocString.getNumpyUnionType(typeString)) {
      PyType parsedType = parseSingleNumpyDocType(anchor, typeName);
      if (parsedType != null) {
        types.add(parsedType);
      }
    }
    return PyUnionType.union(types);
  }

  @Nullable
  @Override
  public PyType getParameterType(PyNamedParameter parameter, PyFunction function, TypeEvalContext context) {
    String parameterName = parameter.getName();
    if (parameterName != null) {
      return getParameterType(function, parameterName);
    }
    return null;
  }

  @Nullable
  public static PyType getParameterType(PyFunction function, String parameterName) {
    NumpyDocString docString = NumpyDocString.forFunction(function, function);
    if (docString != null) {
      DocStringParameter parameter = docString.getNamedParameter(parameterName);

      // If parameter name starts with "p_", and we failed to obtain it from docstring,
      // try to obtain parameter named without such prefix.
      if (parameter == null && parameterName.startsWith("p_")) {
        parameter = docString.getNamedParameter(parameterName.substring(2));
      }
      if (parameter != null) {
        return parseNumpyDocType(function, parameter.getType());
      }
    }
    return null;
  }

  @Nullable
  @Override
  public PyType getReturnType(PyFunction function, @Nullable PyQualifiedExpression callSite, TypeEvalContext context) {
    if (callSite != null) {
      return getReturnType(function, callSite);
    }
    return null;
  }

  @Nullable
  public static PyType getReturnType(@NotNull PyFunction function, @NotNull PsiElement reference) {
    NumpyDocString docString = NumpyDocString.forFunction(function, reference);
    return docString != null ? getReturnTypeFromDocString(docString) : null;
  }

  @Nullable
  public static PyType getReturnTypeFromDocString(@NotNull NumpyDocString docString) {
    List<DocStringParameter> returns = docString.getReturns();
    switch (returns.size()) {
      case 0:
        // Function returns nothing
        return PyTypeParser.getTypeByName(docString.getReference(), "None");
      case 1:
        // Function returns single value
        String typeString = returns.get(0).getType();
        if (typeString != null) {
          return parseNumpyDocType(docString.getReference(), typeString);
        }
        return null;
      default:
        // Function returns a tuple
        return PyTypeParser.getTypeByName(docString.getReference(), "tuple");
    }
  }
}
