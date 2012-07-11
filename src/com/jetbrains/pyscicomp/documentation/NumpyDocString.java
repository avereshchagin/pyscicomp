package com.jetbrains.pyscicomp.documentation;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NumpyDocString {

  private static final Pattern LINE_SEPARATOR = Pattern.compile("\n|\r|\r\n");
  private static final Pattern WHITESPACED_LINE = Pattern.compile("^[ \t]+$");
  private static final Pattern ANY_INDENT = Pattern.compile("(^[ \t]*)[^ \t\r\n]");
  private static final Pattern HAS_INDENT = Pattern.compile("(^[ \t]+)[^ \t\r\n]");
  private static final Pattern SECTION_HEADER = Pattern.compile("^[-=]+");
  private static final Pattern PARAMETER_WITH_TYPE = Pattern.compile("^(.+) : (.+)$");

  private final List<DocStringParameter> myParameters = new ArrayList<DocStringParameter>();
  private final List<DocStringParameter> myReturns = new ArrayList<DocStringParameter>();

  private NumpyDocString(String text) {
    List<String> lines = splitByLines(text);
    deindent(lines);

    // removing function signature
    lines.remove(0);
    deindent(lines);

    parseSections(lines);
  }

  public List<DocStringParameter> getParameters() {
    return myParameters;
  }

  public List<DocStringParameter> getReturns() {
    return myReturns;
  }

  @NotNull
  public static NumpyDocString parse(String text) {
    return new NumpyDocString(text);
  }

  @NotNull
  private static List<String> splitByLines(@NotNull String text) {
    List<String> lines = new ArrayList<String>();
    for (String line : LINE_SEPARATOR.split(text)) {
      if (!line.isEmpty() && !WHITESPACED_LINE.matcher(line).matches()) {
        lines.add(line);
      }
    }
    return lines;
  }

  private static void deindent(@NotNull List<String> lines) {
    String margin = null;
    for (String line : lines) {
      Matcher matcher = ANY_INDENT.matcher(line);
      if (matcher.find() && matcher.groupCount() != 0) {
        String indent = matcher.group(1);
        if (margin == null) {
          margin = indent;
        } else if (indent.startsWith(margin)) {
        } else if (margin.startsWith(indent)) {
          margin = indent;
        } else {
          margin = "";
          break;
        }
      }
    }

    if (margin != null && !margin.isEmpty()) {
      for (int i = 0; i < lines.size(); i++) {
        lines.set(i, lines.get(i).substring(margin.length()));
      }
    }
  }

  private static int indexOfMatch(@NotNull List<String> lines, @NotNull Pattern pattern, int start) {
    for (int i = start; i < lines.size(); i++) {
      if (pattern.matcher(lines.get(i)).matches()) {
        return i;
      }
    }
    return -1;
  }

  @NotNull
  private static List<String> copyOfRange(@NotNull List<String> src, int start, int end) {
    List<String> dest = new ArrayList<String>();
    if (start < 0) {
      start = 0;
    }
    if (end < 0) {
      end = src.size();
    }
    for (int i = start; i < end; i++) {
      dest.add(src.get(i));
    }
    return dest;
  }

  private void parseSections(@NotNull List<String> lines) {
    int current = indexOfMatch(lines, SECTION_HEADER, 1);
    while (current != -1) {
      int next = indexOfMatch(lines, SECTION_HEADER, current + 1);
      String sectionName = lines.get(current - 1);
      if ("Parameters".equalsIgnoreCase(sectionName)) {
        parseParametersSection(copyOfRange(lines, current + 1, next - 1), myParameters);
      } else if ("Returns".equalsIgnoreCase(sectionName)) {
        parseParametersSection(copyOfRange(lines, current + 1, next - 1), myReturns);
      }
      current = next;
    }
  }

  private static void parseParametersSection(@NotNull List<String> lines, List<DocStringParameter> parameters) {
    DocStringParameterBuilder builder = null;
    for (String line : lines) {
      if (!HAS_INDENT.matcher(line).find()) {
        if (builder != null) {
          parameters.add(builder.build());
        }
        builder = new DocStringParameterBuilder();
        Matcher parameterMatcher = PARAMETER_WITH_TYPE.matcher(line);
        if (parameterMatcher.matches()) {
          if (parameterMatcher.groupCount() >= 2) {
            builder.setName(parameterMatcher.group(1));
            builder.setType(parameterMatcher.group(2));
          }
        }
      } else {
        if (builder != null) {
          builder.appendDescription(line.trim());
        }
      }
    }
    if (builder != null) {
      parameters.add(builder.build());
    }
  }
}
