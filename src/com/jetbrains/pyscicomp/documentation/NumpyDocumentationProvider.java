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
package com.jetbrains.pyscicomp.documentation;

import com.intellij.lang.documentation.AbstractDocumentationProvider;
import com.intellij.psi.PsiElement;
import com.jetbrains.pyscicomp.codeInsight.Utils;
import com.jetbrains.python.psi.PyFunction;

public class NumpyDocumentationProvider extends AbstractDocumentationProvider {

  @Override
  public String generateDoc(PsiElement element, PsiElement originalElement) {
    if (element instanceof PyFunction) {
      PyFunction function = (PyFunction) element;
      if (Utils.isNumpyFunction(function, null)) {
        NumpyDocString docString = NumpyDocString.forFunction(function, originalElement);

        NumpyDocumentationBuilder builder = new NumpyDocumentationBuilder();
        builder.setSignature(docString.getSignature());
        for (DocStringParameter parameter : docString.getParameters()) {
          builder.addParameter(parameter);
        }
        for (DocStringParameter parameter : docString.getReturns()) {
          builder.addReturn(parameter);
        }
        return builder.build();
      }
    }
    return null;
  }
}
