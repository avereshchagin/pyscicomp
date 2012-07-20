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
package com.jetbrains.pyscicomp.codeInsight;

import com.intellij.codeInspection.LocalInspectionToolSession;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.pyscicomp.codeInsight.types.FunctionTypeInformation;
import com.jetbrains.pyscicomp.codeInsight.types.ParameterTypeInformation;
import com.jetbrains.pyscicomp.codeInsight.types.TypeInformationCache;
import com.jetbrains.python.inspections.PyInspection;
import com.jetbrains.python.inspections.PyInspectionVisitor;
import com.jetbrains.python.psi.Callable;
import com.jetbrains.python.psi.PyCallExpression;
import com.jetbrains.python.psi.PyFunction;
import com.jetbrains.python.psi.PyStringLiteralExpression;
import com.jetbrains.python.psi.resolve.PyResolveContext;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public class PermissibleArgumentCheckInspection extends PyInspection {

  @NotNull
  @Override
  public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder,
                                        boolean isOnTheFly,
                                        @NotNull LocalInspectionToolSession session) {
    return new Visitor(holder, session);
  }

  public static class Visitor extends PyInspectionVisitor {

    public Visitor(@Nullable ProblemsHolder holder, @NotNull LocalInspectionToolSession session) {
      super(holder, session);
    }

    private void checkArguments(@NotNull PyCallExpression callExpression) {
      Callable calleeFunction = callExpression.resolveCalleeFunction(PyResolveContext.defaultContext());
      if (calleeFunction instanceof PyFunction) {
        String functionName = Utils.getQualifiedName((PyFunction) calleeFunction, callExpression);
        FunctionTypeInformation typeInformation = TypeInformationCache.getInstance().getFunction(functionName);
        if (typeInformation != null) {
          List<ParameterTypeInformation> parameters = typeInformation.getParameters();
          for (int i = 0; i < parameters.size(); i++) {
            ParameterTypeInformation parameter = parameters.get(i);
            Set<String> values = parameter.getPermissibleValues();
            if (!values.isEmpty()) {
              PyStringLiteralExpression passedString = callExpression.getArgument(i, parameter.getName(),
                                                                                  PyStringLiteralExpression.class);
              if (passedString != null) {
                if (!containsIgnoreCase(values, passedString.getStringValue())) {
                  registerProblem(passedString, "Argument must be one of " + values);
                }
              }
            }
          }
        }
      }
    }

    private boolean containsIgnoreCase(@NotNull Collection<String> collection, @NotNull String value) {
      for (String element : collection) {
        if (value.equalsIgnoreCase(element)) {
          return true;
        }
      }
      return false;
    }

    @Override
    public void visitPyCallExpression(PyCallExpression node) {
      checkArguments(node);
    }
  }


  @Nls
  @NotNull
  @Override
  public String getDisplayName() {
    return "Permissible Argument Check Inspection";
  }
}
