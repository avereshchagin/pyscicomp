package com.jetbrains.pyscicomp.documentation;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DocStringParameter {

  private final String myName;
  private final String myType;
  private final String myDescription;

  public DocStringParameter(@NotNull String name, @Nullable String type, @Nullable String description) {
    myName = name;
    myType = type;
    myDescription = description;
  }

  public String getName() {
    return myName;
  }

  public String getType() {
    return myType;
  }

  public String getDescription() {
    return myDescription;
  }
}
