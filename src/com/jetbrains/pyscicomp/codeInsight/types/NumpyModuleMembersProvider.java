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

import com.jetbrains.python.codeInsight.PyDynamicMember;
import com.jetbrains.python.psi.PyFile;
import com.jetbrains.python.psi.resolve.ResolveImportUtil;
import com.jetbrains.python.psi.types.PyModuleMembersProvider;

import java.util.*;

public class NumpyModuleMembersProvider extends PyModuleMembersProvider {

  private static final List<String> NUMPY_NUMERIC_TYPES = Arrays.asList(
    "int8", "uint8", "int16", "uint16", "int32", "uint32", "int64", "uint64", "int128", "uint128",
    "float16", "float32", "float64", "float80", "float96", "float128", "float256",
    "complex32", "complex64", "complex128", "complex160", "complex192", "complex256", "complex512"
  );

  private static final String TARGET_TYPE = "numpy.core.multiarray.dtype";

  @Override
  protected Collection<PyDynamicMember> getMembersByQName(PyFile module, String qName, ResolveImportUtil.PointInImport point) {
    if (qName.equals("numpy")) {
      List<PyDynamicMember> members = new ArrayList<PyDynamicMember>();
      for (String type : NUMPY_NUMERIC_TYPES) {
        members.add(new PyDynamicMember(type, TARGET_TYPE, false));
      }
      return members;
    }
    return Collections.emptyList();
  }
}
