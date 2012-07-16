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
package com.jetbrains.pyscicomp.types;

import com.intellij.util.containers.hash.HashMap;

import java.util.Map;

public class FunctionTypeInfo {

  public Map<String, Parameter> parameters = new HashMap<String, Parameter>();
  public Type returnType;

  public static class Parameter {

    public Type type;
    public boolean optional;

    public Parameter(Type parameterType, boolean isOptional) {
      type = parameterType;
      optional = isOptional;
    }
  }

  public static class Type {

    public String name;

    public Type() {
    }

    public Type(String typeName) {
      name = typeName;
    }
  }
}
