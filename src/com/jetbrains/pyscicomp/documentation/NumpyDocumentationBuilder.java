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

  private StringBuilder myBuilder;

  public NumpyDocumentationBuilder() {

  }

  public void addParameter(DocStringParameter parameter) {
    myParameters.add(parameter);
  }

  public void addReturn(DocStringParameter parameter) {
    myReturns.add(parameter);
  }

  private void startIndent() {
    myBuilder.append("<div style=\"margin-left: 20px;\">");
  }

  private void stopIndent() {
    myBuilder.append("</div>");
  }

  private void subHeader(String title) {
    myBuilder.append("<h2>");
    myBuilder.append(StringUtil.escapeXml(title));
    myBuilder.append("</h2>");
  }

  private void buildParameterList(List<DocStringParameter> parameters, String header, String emptyListMessage) {
    subHeader(header);

    startIndent();
    if (parameters.size() > 0) {
      for (DocStringParameter parameter : parameters) {
        myBuilder.append("<b>");
        myBuilder.append(StringUtil.escapeXml(parameter.getName()));
        myBuilder.append("</b> : <i>");
        myBuilder.append(StringUtil.escapeXml(parameter.getType()));
        myBuilder.append("</i>");

        startIndent();
        myBuilder.append(StringUtil.escapeXml(parameter.getDescription()));
        stopIndent();
      }
    } else {
      myBuilder.append(StringUtil.escapeXml(emptyListMessage));
    }
    stopIndent();
  }

  @NotNull
  public String build() {
    myBuilder = new StringBuilder();
    myBuilder.append("<html><body>");

    buildParameterList(myParameters, "Parameters", "No parameters.");
    buildParameterList(myReturns, "Returns", "None.");

    myBuilder.append("</body></html>");
    return myBuilder.toString();
  }
}
