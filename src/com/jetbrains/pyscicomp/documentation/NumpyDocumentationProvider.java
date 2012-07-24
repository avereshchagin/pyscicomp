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
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.util.Function;
import com.jetbrains.pyscicomp.codeInsight.Utils;
import com.jetbrains.python.psi.PyFunction;
import com.jetbrains.python.psi.PyNamedParameter;
import com.jetbrains.python.psi.PyParameter;
import com.jetbrains.python.psi.PyQualifiedExpression;
import com.jetbrains.python.psi.types.PyType;
import com.jetbrains.python.psi.types.TypeEvalContext;

public class NumpyDocumentationProvider extends AbstractDocumentationProvider {

  public static final String NAMELESS_PARAMETER = "_";
  public static final String UNKNOWN_TYPE = "unknown";

  @Override
  public String getQuickNavigateInfo(PsiElement element, PsiElement originalElement) {
    final PyFunction function = Utils.extractCalleeFunction(element);
    if (function != null) {
      final TypeEvalContext context = TypeEvalContext.fastStubOnly(originalElement.getContainingFile());

      StringBuilder stringBuilder = new StringBuilder();
      stringBuilder.append(function.getName());
      stringBuilder.append(" (");

      stringBuilder.append(StringUtil.join(function.getParameterList().getParameters(), new Function<PyParameter, String>() {
        @Override
        public String fun(PyParameter parameter) {
          StringBuilder sb = new StringBuilder();
          boolean optional = parameter.getDefaultValue() != null;
          if (optional) {
            sb.append("[");
          }
          String name = parameter.getName();
          sb.append(name != null ? name : NAMELESS_PARAMETER);

          sb.append(": ");

          PyNamedParameter namedParameter = parameter.getAsNamed();
          if (namedParameter != null) {
            PyType type = namedParameter.getType(context);
            sb.append(type != null ? type.getName() : UNKNOWN_TYPE);
          } else {
            sb.append(UNKNOWN_TYPE);
          }

          if (optional) {
            sb.append("]");
          }
          return sb.toString();
        }
      }, ", "));

      stringBuilder.append(") -&gt; ");
      PyQualifiedExpression callSite = originalElement instanceof PyQualifiedExpression ? (PyQualifiedExpression) originalElement : null;
      PyType returnType = function.getReturnType(context, callSite);
      stringBuilder.append(returnType != null ? returnType.getName() : UNKNOWN_TYPE);
      return stringBuilder.toString();
    }
    return null;
  }

  @Override
  public String generateDoc(PsiElement element, final PsiElement originalElement) {
    PyFunction function = Utils.extractCalleeFunction(element);
    if (function != null) {
      if (Utils.isNumpyFunction(function, originalElement)) {
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
