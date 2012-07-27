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

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class ParameterTypeInformation {

  private String name;
  private String type;
  private final Set<String> permissibleValues;

  public ParameterTypeInformation(String name, String type, Collection<String> permissibleValues) {
    this.name = name;
    this.type = type;
    this.permissibleValues = new LinkedHashSet<String>(permissibleValues);
  }

  public String getType() {
    return type;
  }

  public String getName() {
    return name;
  }

  public Set<String> getPermissibleValues() {
    return Collections.unmodifiableSet(permissibleValues);
  }
}
