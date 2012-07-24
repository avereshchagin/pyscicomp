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
import com.jetbrains.python.psi.types.PyType;
import com.jetbrains.python.psi.types.PyTypeParser;
import com.jetbrains.python.psi.types.PyTypeProviderBase;
import com.jetbrains.python.psi.types.TypeEvalContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Provides type information extracted from Numpy docstring format.
 */
public class NumpyDocstringTypeProvider extends PyTypeProviderBase {

  private static final Map<String, String> NUMPY_ALIAS_TO_REAL_TYPE = new HashMap<String, String>();

  static {
    NUMPY_ALIAS_TO_REAL_TYPE.put("array_like", "collections.Iterable or int or long or float or complex or bool or string");
    // Parameters marked as 'data-type' actually get any Python type identifier such as 'bool' or
    // an instance of numpy.core.multiarray.dtype, however PyTypeChecker isn't able to check it.
    NUMPY_ALIAS_TO_REAL_TYPE.put("data-type", "object");
    //NUMPY_ALIAS_TO_REAL_TYPE.put("dtype", "numpy.core.multiarray.dtype");
    //NUMPY_ALIAS_TO_REAL_TYPE.put("ndarray", "numpy.core.multiarray.ndarray");
    NUMPY_ALIAS_TO_REAL_TYPE.put("scalar", "int or long or float or complex or bool or string");
  }

  /**
   * Type description in Numpy docstring format can contain additional information, i.e. indicate that parameter is optional.
   * Function extracts type name from such string. "data-type" will be extracted for the following example:
   *   data-type, optional
   * @param typeString original type string.
   * @return cleaned type name.
   */
  @NotNull
  private static String cleanNumpyTypeString(@NotNull String typeString) {
    Pattern pattern = Pattern.compile("^([^, ]+)[, ]");
    Matcher matcher = pattern.matcher(typeString);
    if (matcher.find()) {
      return matcher.group(1);
    }
    return typeString;
  }

  @Nullable
  private static PyType resolveTypeFromDocStringTypeAlias(@NotNull PsiElement anchor, @NotNull String alias) {
    alias = cleanNumpyTypeString(alias);
    String realTypeName = NUMPY_ALIAS_TO_REAL_TYPE.get(alias);
    if (realTypeName != null) {
      PyType type = PyTypeParser.getTypeByName(anchor, realTypeName);
      if (type != null) {
        return type;
      }
    }
    return PyTypeParser.getTypeByName(anchor, alias);
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
    DocStringParameter parameter = docString.getNamedParameter(parameterName);

    // If parameter name starts with "p_", and we failed to obtain it from docstring,
    // try to obtain parameter named without such prefix.
    if (parameter == null && parameterName.startsWith("p_")) {
      parameter = docString.getNamedParameter(parameterName.substring(2));
    }
    if (parameter != null) {
      return resolveTypeFromDocStringTypeAlias(function, parameter.getType());
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
    return getReturnTypeFromDocString(docString);
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
          return resolveTypeFromDocStringTypeAlias(docString.getReference(), typeString);
        }
        return null;
      default:
        // Function returns a tuple
        return PyTypeParser.getTypeByName(docString.getReference(), "tuple");
    }
  }
}
