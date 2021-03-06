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

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.util.Function;
import com.jetbrains.pyscicomp.util.PyFunctionUtils;
import com.jetbrains.python.psi.PyFunction;
import com.jetbrains.python.psi.PyParameter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FunctionTypeInformation {

  private String name;
  private String returnType;
  private List<ParameterTypeInformation> parameters = new ArrayList<ParameterTypeInformation>();

  public FunctionTypeInformation(String name, String returnType, List<ParameterTypeInformation> parameters) {
    this.name = name;
    this.returnType = returnType;
    this.parameters = parameters;
  }

  public static FunctionTypeInformation forPyFunction(@NotNull PyFunction function, @Nullable PsiElement reference) {
    String functionName = function.getQualifiedName();
    FunctionTypeInformation typeInformation = TypeInformationCache.getInstance().getFunction(functionName);
    if (typeInformation == null) {
      int start = 0;
      if (function.getContainingClass() != null) {
        // Function is a class method, so ignore 'self' parameter
        start = 1;
      }

      List<ParameterTypeInformation> parametersList = new ArrayList<ParameterTypeInformation>();
      PyParameter[] parameters = function.getParameterList().getParameters();
      for (int i = start; i < parameters.length; i++) {
        parametersList.add(new ParameterTypeInformation(parameters[i].getName(), "", Collections.<String>emptySet()));
      }
      typeInformation = new FunctionTypeInformation(functionName, "", parametersList);
    }
    return typeInformation;
  }

  public String getReturnType() {
    return returnType;
  }

  public String getName() {
    return name;
  }

  @Nullable
  public ParameterTypeInformation getParameter(@NotNull String name) {
    for (ParameterTypeInformation parameter : parameters) {
      if (name.equals(parameter.getName())) {
        return parameter;
      }
    }
    return null;
  }

  @Nullable
  public String getParameterType(String name) {
    ParameterTypeInformation parameter = getParameter(name);
    if (parameter != null) {
      return parameter.getType();
    }
    return null;
  }

  @NotNull
  public List<ParameterTypeInformation> getParameters() {
    return Collections.unmodifiableList(parameters);
  }

  @Override
  public String toString() {
    StringBuilder result = new StringBuilder("<html>");
    int prefixEndsAt = name.lastIndexOf('.');
    if (prefixEndsAt != -1 && prefixEndsAt + 1 < name.length()) {
      result.append(name.substring(0, prefixEndsAt + 1));
      result.append("<b>");
      result.append(name.substring(prefixEndsAt + 1));
      result.append("</b>");
    } else {
      result.append(name);
    }
    result.append(" (");
    result.append(
      StringUtil.join(getParameters(),
                      new Function<ParameterTypeInformation, String>() {
                        @Override
                        public String fun(ParameterTypeInformation parameter) {
                          return parameter.getName();
                        }
                      },
                      ", "));
    result.append(")");
    return result.toString();
  }
}
