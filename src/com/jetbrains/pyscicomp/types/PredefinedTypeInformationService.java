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
import com.intellij.util.containers.hash.HashMap;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

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
}
