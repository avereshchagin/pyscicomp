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
package com.jetbrains.pyscicomp.codeInsight.ui;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.Function;
import com.jetbrains.pyscicomp.codeInsight.types.FunctionTypeInfo;

import java.util.Map;

public class FunctionWrapper {

  private final Map.Entry<String, FunctionTypeInfo> myEntry;

  public FunctionWrapper(Map.Entry<String, FunctionTypeInfo> entry) {
    myEntry = entry;
  }

  @Override
  public String toString() {
    StringBuilder result = new StringBuilder("<html>");
    String functionName = myEntry.getKey();
    int prefixEndsAt = functionName.lastIndexOf('.');
    if (prefixEndsAt != -1 && prefixEndsAt + 1 < functionName.length()) {
      result.append(functionName.substring(0, prefixEndsAt + 1));
      result.append("<b>");
      result.append(functionName.substring(prefixEndsAt + 1));
      result.append("</b>");
    } else {
      result.append(functionName);
    }
    result.append(" (");
    result.append(
      StringUtil.join(myEntry.getValue().parameters,
                      new Function<FunctionTypeInfo.Parameter, String>() {
                        @Override
                        public String fun(FunctionTypeInfo.Parameter parameter) {
                          return parameter.name;
                        }
                      },
                      ", "));
    result.append(")");
    return result.toString();
  }

  public FunctionTypeInfo getTypeInfo() {
    return myEntry.getValue();
  }
}
