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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.intellij.openapi.application.PathManager;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TypeInformationCache {

  private static final String CACHE_PATH = PathManager.getOptionsPath() + File.separator + "types_db.json";
  private static final Type DATA_TYPE = new TypeToken<List<FunctionTypeInformation>>(){}.getType();

  private static final TypeInformationCache CACHE = new TypeInformationCache();

  public static TypeInformationCache getInstance() {
    return CACHE;
  }

  private final Map<String, FunctionTypeInformation> myNamesToFunctions;

  private TypeInformationCache() {
    List<FunctionTypeInformation> functions = load();
    myNamesToFunctions = new HashMap<String, FunctionTypeInformation>();
    if (functions != null) {
      for (FunctionTypeInformation function : functions) {
        myNamesToFunctions.put(function.getName(), function);
      }
    }
  }

  public FunctionTypeInformation getFunction(String name) {
    return myNamesToFunctions.get(name);
  }

  public void putFunction(FunctionTypeInformation function, boolean autoSave) {
    myNamesToFunctions.put(function.getName(), function);
    if (autoSave) {
      save();
    }
  }

  public List<FunctionTypeInformation> getAsList() {
    return new ArrayList<FunctionTypeInformation>(myNamesToFunctions.values());
  }

  public void save() {
    OutputStream outputStream = null;
    try {
      outputStream = new FileOutputStream(CACHE_PATH);
      Writer writer = new OutputStreamWriter(outputStream, "UTF-8");
      Gson gson = new GsonBuilder().setPrettyPrinting().create();
      String json = gson.toJson(getAsList());
      writer.write(json);
      writer.flush();
    } catch (IOException e) {
    } finally {
      if (outputStream != null) {
        try {
          outputStream.close();
        } catch (IOException e) {
        }
      }
    }
  }

  @Nullable
  private List<FunctionTypeInformation> load() {
    InputStream inputStream = null;
    try {
      inputStream = new FileInputStream(CACHE_PATH);
      BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
      Gson gson = new Gson();
      return gson.fromJson(reader, DATA_TYPE);
    } catch (IOException e) {
      return null;
    } finally {
      if (inputStream != null) {
        try {
          inputStream.close();
        } catch (IOException e) {
        }
      }
    }
  }
}
