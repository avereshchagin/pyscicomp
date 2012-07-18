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
import com.intellij.openapi.util.Pair;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ArrayUtil;
import com.intellij.util.PlatformIcons;
import com.intellij.util.ProcessingContext;
import com.jetbrains.pyscicomp.codeInsight.Utils;
import com.jetbrains.pyscicomp.codeInsight.types.FunctionTypeInfo;
import com.jetbrains.pyscicomp.codeInsight.types.PredefinedTypeInformationService;
import com.jetbrains.python.psi.*;
import com.jetbrains.python.psi.resolve.PyResolveContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class PermissibleArgumentCompletionContributor extends CompletionContributor {

  private static void addSuggestion(CompletionResultSet resultSet, String element) {
    LookupElementBuilder builder = LookupElementBuilder.create(element).withIcon(PlatformIcons.PARAMETER_ICON);
    resultSet.addElement(PrioritizedLookupElement.withPriority(builder, 1.0));
  }

  private static void suggestVariantsForOrderedArgument(String functionName, int index, CompletionResultSet resultSet) {
    List<FunctionTypeInfo.Parameter> parameters = PredefinedTypeInformationService.getParameters(functionName);

    if (parameters != null && index >= 0 && index < parameters.size()) {
      Set<String> permissibleValues = parameters.get(index).permissibleValues;
      if (permissibleValues != null) {
        for (String value : permissibleValues) {
          addSuggestion(resultSet, "'" + value + "'");
        }
      }
    }
  }

  private static void suggestVariantsForNamedArgument(@NotNull String functionName,
                                                      @Nullable String keyword,
                                                      CompletionResultSet resultSet) {
    List<FunctionTypeInfo.Parameter> parameters = PredefinedTypeInformationService.getParameters(functionName);

    if (parameters != null) {
      for (FunctionTypeInfo.Parameter parameter : parameters) {
        if (parameter.name.equals(keyword)) {
          Set<String> permissibleValues = parameter.permissibleValues;
          if (permissibleValues != null) {
            for (String value : permissibleValues) {
              addSuggestion(resultSet, "'" + value + "'");
            }
          }
        }
      }
    }
  }

  private static void suggestVariantsForAllNamedArguments(String functionName, CompletionResultSet resultSet) {
    Map<Pair<Integer, String>, Set<String>> permissibleArguments =
      PredefinedTypeInformationService.getPermissibleArguments(functionName);

    for (Map.Entry<Pair<Integer, String>, Set<String>> entry : permissibleArguments.entrySet()) {
      String keywordParameter = entry.getKey().second;
      for (String value : entry.getValue()) {
        addSuggestion(resultSet, keywordParameter + "='" + value + "'");
      }
    }
  }

  private static void addCompletionsForNamelessArgument(PsiElement element, CompletionResultSet resultSet) {
    PyCallExpression callExpression = PsiTreeUtil.getParentOfType(element, PyCallExpression.class);
    if (callExpression != null) {
      Callable calleeFunction = callExpression.resolveCalleeFunction(PyResolveContext.defaultContext());
      if (calleeFunction instanceof PyFunction) {
        PyFunction function = (PyFunction) calleeFunction;
        String functionName = Utils.getQualifiedName(function, callExpression);

        // Determine for which argument completion is called and show hints for this argument
        int editingArgumentIndex = ArrayUtil.indexOf(callExpression.getArguments(), element.getParent());
        if (editingArgumentIndex != -1) {
          suggestVariantsForOrderedArgument(functionName, editingArgumentIndex, resultSet);
        }

        // Anyway show hints for arguments passed by keywords
        suggestVariantsForAllNamedArguments(functionName, resultSet);
      }
    }
  }

  private static void addCompletionsForNamedArgument(PsiElement element, CompletionResultSet resultSet) {
    PyKeywordArgument keywordArgument = PsiTreeUtil.getParentOfType(element, PyKeywordArgument.class);
    PyCallExpression callExpression = PsiTreeUtil.getParentOfType(keywordArgument, PyCallExpression.class);

    String functionName = Utils.getQualifiedNameOfCalleeFunction(callExpression);
    if (functionName != null && keywordArgument != null) {
      suggestVariantsForNamedArgument(functionName, keywordArgument.getKeyword(), resultSet);
    }
  }

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
