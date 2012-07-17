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

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FunctionTypeInfo {

  @NotNull
  public List<Parameter> parameters = new ArrayList<Parameter>();
  public Type returnType;

  public static class Parameter {

    public String name;
    public Type type;
    public boolean optional;

    @NotNull
    public Set<String> permissibleValues = new HashSet<String>();

    /**
     * Default constructor is a requirement of XML serializer.
     */
    public Parameter() {
    }

    public Parameter(String parameterName, Type parameterType, boolean isOptional, Set<String> values) {
      name = parameterName;
      type = parameterType;
      optional = isOptional;
      permissibleValues = values;
    }
  }

  public static class Type {

    public String name;

    /**
     * Default constructor is a requirement of XML serializer.
     */
    public Type() {
    }

    public Type(String typeName) {
      name = typeName;
    }
  }
}
