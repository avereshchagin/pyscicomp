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
package com.jetbrains.pyscicomp.codeInsight.completion;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ArrayUtil;
import com.intellij.util.PlatformIcons;
import com.intellij.util.ProcessingContext;
import com.jetbrains.pyscicomp.codeInsight.types.FunctionTypeInformation;
import com.jetbrains.pyscicomp.codeInsight.types.ParameterTypeInformation;
import com.jetbrains.pyscicomp.codeInsight.types.TypeInformationCache;
import com.jetbrains.pyscicomp.documentation.DocStringParameter;
import com.jetbrains.pyscicomp.documentation.NumpyDocString;
import com.jetbrains.pyscicomp.util.PyFunctionUtils;
import com.jetbrains.python.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class PermissibleArgumentCompletionContributor extends CompletionContributor {

  private static interface PermissibleArgumentsHandler {

    Set<String> handleTypeInformation(@NotNull FunctionTypeInformation typeInformation);

    Set<String> handleDocString(@NotNull NumpyDocString docString);
  }

  private static void suggestPermissibleArgumentsForFunction(@NotNull PyFunction function,
                                                             @NotNull PsiElement reference,
                                                             CompletionResultSet resultSet,
                                                             @NotNull PermissibleArgumentsHandler handler) {
    Set<String> suggestions = new LinkedHashSet<String>();

    String functionName = PyFunctionUtils.getQualifiedName(function, reference);
    FunctionTypeInformation typeInformation = TypeInformationCache.getInstance().getFunction(functionName);
    if (typeInformation != null) {
      suggestions.addAll(handler.handleTypeInformation(typeInformation));
    }

    NumpyDocString docString = NumpyDocString.forFunction(function, reference);
    if (docString != null) {
      suggestions.addAll(handler.handleDocString(docString));
    }

    for (String suggestion : suggestions) {
      LookupElementBuilder builder = LookupElementBuilder.create(suggestion).withIcon(PlatformIcons.PARAMETER_ICON);
      resultSet.addElement(PrioritizedLookupElement.withPriority(builder, 1.0));
    }
  }

  private static void suggestVariantsForOrderedArgument(@NotNull PyFunction function,
                                                        @NotNull PsiElement reference,
                                                        int index,
                                                        CompletionResultSet resultSet) {
    PyParameter[] parameters = function.getParameterList().getParameters();
    if (index >= 0 && index < parameters.length) {
      PyParameter parameter = parameters[index];
      suggestVariantsForNamedArgument(function, reference, parameter.getName(), resultSet);
    }
  }

  private static void suggestVariantsForNamedArgument(@NotNull PyFunction function,
                                                      @NotNull PsiElement reference,
                                                      final @Nullable String keyword,
                                                      CompletionResultSet resultSet) {
    suggestPermissibleArgumentsForFunction(function, reference, resultSet, new PermissibleArgumentsHandler() {

      private void processResult(Set<String> result, String value) {
        StringBuilder sb = new StringBuilder(value);
        StringUtil.quote(sb, '\'');
        result.add(sb.toString());
      }

      @Override
      public Set<String> handleTypeInformation(@NotNull FunctionTypeInformation typeInformation) {
        if (keyword != null) {
          ParameterTypeInformation parameter = typeInformation.getParameter(keyword);
          if (parameter != null) {
            Set<String> result = new LinkedHashSet<String>();
            for (String value : parameter.getPermissibleValues()) {
              processResult(result, value);
            }
            return result;
          }
        }
        return Collections.emptySet();
      }

      @Override
      public Set<String> handleDocString(@NotNull NumpyDocString docString) {
        Set<String> result = new LinkedHashSet<String>();
        if (keyword != null) {
          DocStringParameter parameter = docString.getNamedParameter(keyword);
          if (parameter != null) {
            String type = parameter.getType();
            for (String value : NumpyDocString.extractPermissibleArgumentsFromNumpyDocType(type)) {
              processResult(result, value);
            }
          }
        }
        return result;
      }
    });
  }

  private static void suggestVariantsForAllNamedArguments(@NotNull PyFunction function,
                                                          @NotNull PsiElement reference,
                                                          CompletionResultSet resultSet) {
    suggestPermissibleArgumentsForFunction(function, reference, resultSet, new PermissibleArgumentsHandler() {

      private void processResult(Set<String> result, String keyword, String value) {
        StringBuilder sb = new StringBuilder(value);
        StringUtil.quote(sb, '\'');
        sb.insert(0, '=');
        sb.insert(0, keyword);
        result.add(sb.toString());
      }

      @Override
      public Set<String> handleTypeInformation(@NotNull FunctionTypeInformation typeInformation) {
        Set<String> result = new LinkedHashSet<String>();
        for (ParameterTypeInformation parameter : typeInformation.getParameters()) {
          Set<String> values = parameter.getPermissibleValues();
          for (String value : values) {
            processResult(result, parameter.getName(), value);
          }
        }
        return result;
      }

      @Override
      public Set<String> handleDocString(@NotNull NumpyDocString docString) {
        Set<String> result = new LinkedHashSet<String>();
        for (DocStringParameter parameter : docString.getParameters()) {
          for (String value : NumpyDocString.extractPermissibleArgumentsFromNumpyDocType(parameter.getType())) {
            processResult(result, parameter.getName(), value);
          }
        }
        return result;
      }
    });
  }

  private static void addCompletionsForNamelessArgument(PsiElement element, CompletionResultSet resultSet) {
    PyCallExpression callExpression = PsiTreeUtil.getParentOfType(element, PyCallExpression.class);
    PyFunction calleeFunction = PyFunctionUtils.getCalleeFunction(callExpression);
    if (callExpression != null && calleeFunction != null) {

      // Determine for which argument completion is called and show hints for this argument
      int editingArgumentIndex = ArrayUtil.indexOf(callExpression.getArguments(), element.getParent());
      if (editingArgumentIndex != -1) {
        suggestVariantsForOrderedArgument(calleeFunction, callExpression, editingArgumentIndex, resultSet);
      }

      // Anyway show hints for arguments passed by keywords
      suggestVariantsForAllNamedArguments(calleeFunction, callExpression, resultSet);
    }
  }

  private static void addCompletionsForNamedArgument(PsiElement element, CompletionResultSet resultSet) {
    PyKeywordArgument keywordArgument = PsiTreeUtil.getParentOfType(element, PyKeywordArgument.class);
    PyCallExpression callExpression = PsiTreeUtil.getParentOfType(keywordArgument, PyCallExpression.class);
    PyFunction calleeFunction = PyFunctionUtils.getCalleeFunction(callExpression);

    if (calleeFunction != null && callExpression != null && keywordArgument != null) {
      suggestVariantsForNamedArgument(calleeFunction, callExpression, keywordArgument.getKeyword(), resultSet);
    }
  }

  @SuppressWarnings("unchecked")
  public PermissibleArgumentCompletionContributor() {
    extend(CompletionType.BASIC,
           PlatformPatterns.psiElement().withParents(PyReferenceExpression.class, PyArgumentList.class, PyCallExpression.class),
           new CompletionProvider<CompletionParameters>() {
             @Override
             protected void addCompletions(@NotNull CompletionParameters parameters,
                                           ProcessingContext context,
                                           @NotNull CompletionResultSet resultSet) {
               addCompletionsForNamelessArgument(parameters.getPosition(), resultSet);
             }
           });

    extend(CompletionType.BASIC,
           PlatformPatterns.psiElement().withParents(PyReferenceExpression.class, PyKeywordArgument.class, PyArgumentList.class,
                                                     PyCallExpression.class),
           new CompletionProvider<CompletionParameters>() {
             @Override
             protected void addCompletions(@NotNull CompletionParameters parameters,
                                           ProcessingContext context,
                                           @NotNull CompletionResultSet resultSet) {
               addCompletionsForNamedArgument(parameters.getPosition(), resultSet);
             }
           });
  }
}
