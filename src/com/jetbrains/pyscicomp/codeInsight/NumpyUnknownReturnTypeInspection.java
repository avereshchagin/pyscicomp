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
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiPolyVariantReference;
import com.jetbrains.pyscicomp.codeInsight.ui.EditTypeInformationDialog;
import com.jetbrains.python.inspections.PyInspection;
import com.jetbrains.python.inspections.PyInspectionVisitor;
import com.jetbrains.python.psi.*;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class NumpyUnknownReturnTypeInspection extends PyInspection {

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

    @Override
    public void visitPyCallExpression(PyCallExpression node) {
      PyExpression callee = node.getCallee();
      if (callee instanceof PyReferenceExpression) {
        PyReferenceExpression referenceExpression = (PyReferenceExpression) callee;
        PsiPolyVariantReference reference = referenceExpression.getReference();
        PsiElement resolved = reference.resolve();
        if (resolved instanceof PyFunction) {
          PyFunction function = (PyFunction) resolved;
          if (function.isValid() && function.getReturnType(myTypeEvalContext, referenceExpression) == null) {
            List<String> parameters = new ArrayList<String>();
            for (PyParameter parameter : function.getParameterList().getParameters()) {
              parameters.add(parameter.getName());
            }
            registerProblem(node, "Unknown return type",
                            new AddTypeInformationFix(Utils.getQualifiedName(function, referenceExpression), parameters));
          }
        }
      }
    }
  }

  private static class AddTypeInformationFix implements LocalQuickFix {

    private final String myFunction;
    private final List<String> myParameters;

    public AddTypeInformationFix(@NotNull String function, @NotNull List<String> parameters) {
      myFunction = function;
      myParameters = parameters;
    }

    @NotNull
    @Override
    public String getName() {
      return "Add type information for function...";
    }

    @NotNull
    @Override
    public String getFamilyName() {
      return "";
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
      new EditTypeInformationDialog(project, myFunction, myParameters).show();
    }
  }

  @Nls
  @NotNull
  @Override
  public String getDisplayName() {
    return "Unknown Return Type Inspection";
  }
}
