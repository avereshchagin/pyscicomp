package com.jetbrains.pyscicomp.documentation;

public class DocStringParameterBuilder {

  private String myName = null;
  private String myType = null;
  private StringBuilder myDescription = new StringBuilder();

  public void setName(String name) {
    myName = name;
  }

  public void setType(String type) {
    myType = type;
  }

  public void appendDescription(String text) {
    myDescription.append(" ");
    myDescription.append(text);
  }

  public DocStringParameter build() {
    return new DocStringParameter(myName, myType, myDescription.toString());
  }
}
