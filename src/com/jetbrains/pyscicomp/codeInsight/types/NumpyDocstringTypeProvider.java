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

import com.jetbrains.pyscicomp.documentation.DocStringParameter;
import com.jetbrains.pyscicomp.documentation.NumpyDocString;
import com.jetbrains.python.psi.PyFunction;
import com.jetbrains.python.psi.PyQualifiedExpression;
import com.jetbrains.python.psi.types.PyType;
import com.jetbrains.python.psi.types.PyTypeParser;
import com.jetbrains.python.psi.types.PyTypeProviderBase;
import com.jetbrains.python.psi.types.TypeEvalContext;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides type information extracted from Numpy docstring format.
 */
public class NumpyDocstringTypeProvider extends PyTypeProviderBase {

  private static final Map<String, String> PYTHON_ALIAS_TO_REAL_TYPE = new HashMap<String, String>();

  static {
    PYTHON_ALIAS_TO_REAL_TYPE.put("array_like", "collections.Iterable");
    PYTHON_ALIAS_TO_REAL_TYPE.put("data-type", "numpy.core.multiarray.dtype");
    //PYTHON_ALIAS_TO_REAL_TYPE.put("dtype", "numpy.core.multiarray.dtype");
    //PYTHON_ALIAS_TO_REAL_TYPE.put("ndarray", "numpy.core.multiarray.ndarray");
    PYTHON_ALIAS_TO_REAL_TYPE.put("scalar", "int or float");
  }

  @Override
  public PyType getReturnType(PyFunction function, @Nullable PyQualifiedExpression callSite, TypeEvalContext context) {
    if (callSite != null) {
      NumpyDocString docString = NumpyDocString.forFunction(function, callSite);
      List<DocStringParameter> returns = docString.getReturns();
      if (returns.size() > 0) {
        if (returns.size() == 1) {
          String typeString = returns.get(0).getType();
          if (typeString != null) {
            if (typeString.indexOf(',') != -1) {
              //Matcher matcher = Pattern.compile("^(.+),.*$").matcher(typeString);
              //if (matcher.groupCount() > 0) {
              //  return PyTypeParser.getTypeByName(callSite, PYTHON_ALIAS_TO_REAL_TYPE.get(matcher.group(1)));
              //}
            } else {
              String realTypeName = PYTHON_ALIAS_TO_REAL_TYPE.get(typeString);
              if (realTypeName != null) {
                PyType type = PyTypeParser.getTypeByName(callSite, realTypeName);
                if (type != null) {
                  return type;
                }
                return PyTypeParser.getTypeByName(callSite, typeString);
              }
              return PyTypeParser.getTypeByName(callSite, typeString);
            }
          }
        }
      }
    }
    return null;
  }
}
