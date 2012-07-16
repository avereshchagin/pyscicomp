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

import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFileSystemItem;
import com.jetbrains.python.psi.PyUtil;
import com.jetbrains.python.psi.impl.PyBuiltinCache;
import com.jetbrains.python.psi.impl.PyQualifiedName;
import com.jetbrains.python.psi.resolve.ResolveImportUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NumpyDocString {

  private static final Pattern LINE_SEPARATOR = Pattern.compile("\n|\r|\r\n");
  private static final Pattern WHITESPACED_LINE = Pattern.compile("^[ \t]+$");
  private static final Pattern ANY_INDENT = Pattern.compile("(^[ \t]*)[^ \t\r\n]");
  private static final Pattern HAS_INDENT = Pattern.compile("(^[ \t]+)[^ \t\r\n]");
  private static final Pattern SIGNATURE = Pattern.compile("^([\\w., ]+=)?\\s*[\\w\\.]+\\(.*\\)$");
  private static final Pattern SECTION_HEADER = Pattern.compile("^[-=]+");
  private static final Pattern PARAMETER_WITH_TYPE = Pattern.compile("^(.+) : (.+)$");
  private static final Pattern REDIRECT = Pattern.compile("^Refer to `(.*)` for full documentation.$");

  private final String mySignature;
  private final List<DocStringParameter> myParameters = new ArrayList<DocStringParameter>();
  private final List<DocStringParameter> myReturns = new ArrayList<DocStringParameter>();

  private NumpyDocString(@Nullable String signature, @NotNull List<String> lines) {
    mySignature = signature;
    parseSections(lines);
  }

  @Nullable
  public String getSignature() {
    return mySignature;
  }

  @NotNull
  public List<DocStringParameter> getParameters() {
    return myParameters;
  }

  @NotNull
  public List<DocStringParameter> getReturns() {
    return myReturns;
  }

  @NotNull
  public static NumpyDocString parse(String text, PsiElement reference) {
    List<String> lines = splitByLines(text);
    dedent(lines);

    String signature = null;
    if (SIGNATURE.matcher(lines.get(0)).matches()) {
      signature = lines.get(0);
      lines.remove(0);
      dedent(lines);
    }

    String redirect = findRedirect(lines);
    if (redirect != null) {
      // TODO: support redirects
    }

    return new NumpyDocString(signature, lines);
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

  private static void dedent(@NotNull List<String> lines) {
    String margin = null;
    for (String line : lines) {
      Matcher matcher = ANY_INDENT.matcher(line);
      if (matcher.find() && matcher.groupCount() != 0) {
        String indent = matcher.group(1);
        if (margin == null || (margin.startsWith(indent) && margin.length() != indent.length())) {
          // update margin
          margin = indent;
        } else if (!indent.startsWith(margin)) {
          // lines have no common margin
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
  private static <T> List<T> copyOfRange(@NotNull List<T> src, int start, int end) {
    List<T> dest = new ArrayList<T>();
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

  @Nullable
  private static String findRedirect(@NotNull List<String> lines) {
    for (String line : lines) {
      Matcher matcher = REDIRECT.matcher(line);
      if (matcher.matches() && matcher.groupCount() > 0) {
        return matcher.group(1);
      }
    }
    return null;
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
