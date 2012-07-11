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

import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class NumpyDocumentationBuilder {

  private final List<DocStringParameter> myParameters = new ArrayList<DocStringParameter>();
  private final List<DocStringParameter> myReturns = new ArrayList<DocStringParameter>();

  public NumpyDocumentationBuilder() {

  }

  public void addParameter(DocStringParameter parameter) {
    myParameters.add(parameter);
  }

  public void addReturn(DocStringParameter parameter) {
    myReturns.add(parameter);
  }

  @NotNull
  public String build() {
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append("<html><body>");

    stringBuilder.append("<h2>Parameters</h2>");
    if (myParameters.size() > 0) {
      for (DocStringParameter parameter : myParameters) {
        stringBuilder.append("<div style=\"margin-left: 20px; background: #e0e0e0;\">");
        stringBuilder.append("<b>");
        stringBuilder.append(StringUtil.escapeXml(parameter.getName()));
        stringBuilder.append("</b> : <i>");
        stringBuilder.append(StringUtil.escapeXml(parameter.getType()));
        stringBuilder.append("</i>");

        stringBuilder.append("<div style=\"margin-left: 20px;\">");
        stringBuilder.append(StringUtil.escapeXml(parameter.getDescription()));
        stringBuilder.append("</div>");

        stringBuilder.append("</div>");
      }
    } else {
      stringBuilder.append("<i>No parameters.</i>");
    }

    stringBuilder.append("<h2>Returns</h2>");
    if (myReturns.size() > 0) {
      for (DocStringParameter parameter : myReturns) {
        stringBuilder.append("<div style=\"margin-left: 20px; background: #e0e0e0;\">");
        stringBuilder.append("<b>");
        stringBuilder.append(StringUtil.escapeXml(parameter.getName()));
        stringBuilder.append("</b> : <i>");
        stringBuilder.append(StringUtil.escapeXml(parameter.getType()));
        stringBuilder.append("</i>");

        stringBuilder.append("<div style=\"margin-left: 20px;\">");
        stringBuilder.append(StringUtil.escapeXml(parameter.getDescription()));
        stringBuilder.append("</div>");

        stringBuilder.append("</div>");
      }
    } else {
      stringBuilder.append("<i>None.</i>");
    }

    stringBuilder.append("</body></html>");
    return stringBuilder.toString();
  }
}
