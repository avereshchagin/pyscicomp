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
