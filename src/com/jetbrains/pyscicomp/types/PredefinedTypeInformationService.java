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

import com.intellij.openapi.components.*;
import com.intellij.openapi.util.Pair;
import com.intellij.util.containers.hash.HashMap;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

@State(name = "PredefinedTypeInformationService",
       storages = {
         @Storage(file = StoragePathMacros.APP_CONFIG + "/types_db.xml")
       }
)
public class PredefinedTypeInformationService implements PersistentStateComponent<PredefinedTypeInformationService> {

  public Map<String, FunctionTypeInfo> functions = new HashMap<String, FunctionTypeInfo>();

  @Override
  public PredefinedTypeInformationService getState() {
    return this;
  }

  @Override
  public void loadState(PredefinedTypeInformationService state) {
    XmlSerializerUtil.copyBean(state, this);
  }

  public static PredefinedTypeInformationService getInstance() {
    return ServiceManager.getService(PredefinedTypeInformationService.class);
  }

  @Nullable
  public static String getReturnType(@NotNull String functionName) {
    PredefinedTypeInformationService service = getInstance();
    FunctionTypeInfo typeInfo = service.functions.get(functionName);
    if (typeInfo != null) {
      return typeInfo.returnType.name;
    }
    return null;
  }

  public static void setReturnType(@NotNull String functionName, @NotNull String returnType) {
    PredefinedTypeInformationService service = getInstance();

    FunctionTypeInfo typeInfo = service.functions.get(functionName);
    if (typeInfo == null) {
      typeInfo = new FunctionTypeInfo();
    }
    typeInfo.returnType = new FunctionTypeInfo.Type(returnType);

    service.functions.put(functionName, typeInfo);
  }

  @Nullable
  public static List<FunctionTypeInfo.Parameter> getParameters(@NotNull String functionName) {
    PredefinedTypeInformationService service = getInstance();

    FunctionTypeInfo typeInfo = service.functions.get(functionName);
    if (typeInfo != null) {
      return Collections.unmodifiableList(typeInfo.parameters);
    }
    return null;
  }

  public static void setParameters(@NotNull String functionName, @NotNull List<FunctionTypeInfo.Parameter> parameters) {
    PredefinedTypeInformationService service = getInstance();

    FunctionTypeInfo typeInfo = service.functions.get(functionName);
    if (typeInfo == null) {
      typeInfo = new FunctionTypeInfo();
    }
    typeInfo.parameters = parameters;

    service.functions.put(functionName, typeInfo);
  }

  @NotNull
  public static Map<Pair<Integer, String>, Set<String>> getPermissibleArguments(@NotNull String functionName) {
    PredefinedTypeInformationService service = getInstance();

    FunctionTypeInfo typeInfo = service.functions.get(functionName);
    if (typeInfo != null) {
      Map<Pair<Integer, String>, Set<String>> result = new HashMap<Pair<Integer, String>, Set<String>>();
      for (int i = 0; i < typeInfo.parameters.size(); i++) {
        FunctionTypeInfo.Parameter parameter = typeInfo.parameters.get(i);
        if (parameter.permissibleValues.size() != 0) {
          result.put(new Pair<Integer, String>(i, parameter.name), Collections.unmodifiableSet(parameter.permissibleValues));
        }
      }
      return result;
    }
    return Collections.emptyMap();
  }
}
