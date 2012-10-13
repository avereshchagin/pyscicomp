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

import com.jetbrains.pyscicomp.util.PyFunctionUtils;
import com.jetbrains.python.psi.PyFunction;
import com.jetbrains.python.psi.PyNamedParameter;
import com.jetbrains.python.psi.PyQualifiedExpression;
import com.jetbrains.python.psi.types.PyType;
import com.jetbrains.python.psi.types.PyTypeParser;
import com.jetbrains.python.psi.types.PyTypeProviderBase;
import com.jetbrains.python.psi.types.TypeEvalContext;
import org.jetbrains.annotations.Nullable;

/**
 * Provides type information stored in user-editable database.
 */
public class PredefinedTypeProvider extends PyTypeProviderBase {

  @Nullable
  @Override
  public PyType getReturnType(PyFunction function, @Nullable PyQualifiedExpression callSite, TypeEvalContext context) {
    if (function.isValid()) {
      String qualifiedName = function.getQualifiedName();
      FunctionTypeInformation typeInformation = TypeInformationCache.getInstance().getFunction(qualifiedName);
      if (typeInformation != null) {
        String returnType = typeInformation.getReturnType();
        if (returnType != null) {
          return PyTypeParser.getTypeByName(function, returnType);
        }
      }
    }
    return null;
  }

  @Nullable
  @Override
  public PyType getParameterType(PyNamedParameter parameter, PyFunction function, TypeEvalContext context) {
    if (function.isValid()) {
      String qualifiedName = function.getQualifiedName();
      FunctionTypeInformation typeInformation = TypeInformationCache.getInstance().getFunction(qualifiedName);
      String parameterName = parameter.getName();
      if (typeInformation != null && parameterName != null) {
        String type = typeInformation.getParameterType(parameterName);
        if (type != null) {
          return PyTypeParser.getTypeByName(function, type);
        }
      }
    }
    return null;
  }
}
