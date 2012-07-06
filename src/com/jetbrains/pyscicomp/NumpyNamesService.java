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
