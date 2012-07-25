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

import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.util.Function;
import com.jetbrains.pyscicomp.util.PyFunctionUtils;
import com.jetbrains.python.psi.PyFunction;
import com.jetbrains.python.psi.PyParameter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class FunctionTypeInformation {

  private String name;
  private String returnType;
  private List<ParameterTypeInformation> parameters = new ArrayList<ParameterTypeInformation>();

  public FunctionTypeInformation() {
  }

  public FunctionTypeInformation(String name, String returnType, List<ParameterTypeInformation> parameters) {
    this.name = name;
    this.returnType = returnType;
    this.parameters = parameters;
  }

  public static FunctionTypeInformation fromPyFunction(@NotNull PyFunction function, @Nullable PsiElement reference) {
    String functionName = PyFunctionUtils.getQualifiedName(function, reference);
    FunctionTypeInformation typeInformation = TypeInformationCache.getInstance().getFunction(functionName);
    if (typeInformation == null) {
      List<ParameterTypeInformation> parametersList = new ArrayList<ParameterTypeInformation>();
      for (PyParameter parameter : function.getParameterList().getParameters()) {
        parametersList.add(new ParameterTypeInformation(parameter.getName(), ""));
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
  public ParameterTypeInformation getParameter(int index) {
    if (index >= 0 && index < parameters.size()) {
      return parameters.get(index);
    }
    return null;
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
  public Set<String> getPermissibleValuesForParameter(int index) {
    ParameterTypeInformation parameter = getParameter(index);
    if (parameter != null) {
      return Collections.unmodifiableSet(parameter.getPermissibleValues());
    }
    return Collections.emptySet();
  }

  @NotNull
  public Set<String> getPermissibleValuesForParameter(String name) {
    ParameterTypeInformation parameter = getParameter(name);
    if (parameter != null) {
      return Collections.unmodifiableSet(parameter.getPermissibleValues());
    }
    return Collections.emptySet();
  }

  @NotNull
  public Set<Pair<String, String>> getAllPermissibleArguments() {
    Set<Pair<String, String>> result = new HashSet<Pair<String, String>>();
    for (ParameterTypeInformation parameter : parameters) {
      Set<String> values = parameter.getPermissibleValues();
      for (String value : values) {
        result.add(new Pair<String, String>(parameter.getName(), value));
      }
    }
    return result;
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
