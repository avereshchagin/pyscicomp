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
package com.jetbrains.pyscicomp;

import com.intellij.openapi.components.*;
import com.intellij.util.containers.hash.HashMap;
import com.intellij.util.xmlb.XmlSerializerUtil;

import java.util.Map;

@State(name = "NumpyNamesService",
       storages = {
         @Storage(file = StoragePathMacros.APP_CONFIG + "/numpy.xml")
       }
)
public class NumpyNamesService implements PersistentStateComponent<NumpyNamesService> {
  public Map<String, String> functionsToReturnTypes = new HashMap<String, String>();

  @Override
  public NumpyNamesService getState() {
    return this;
  }

  @Override
  public void loadState(NumpyNamesService state) {
    XmlSerializerUtil.copyBean(state, this);
  }

  public static NumpyNamesService getInstance() {
    return ServiceManager.getService(NumpyNamesService.class);
  }
}
