package com.jetbrains.pyscicomp;

import com.intellij.codeInspection.LocalInspectionToolSession;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiPolyVariantReference;
import com.jetbrains.python.inspections.PyInspection;
import com.jetbrains.python.inspections.PyInspectionVisitor;
import com.jetbrains.python.psi.*;
import com.jetbrains.python.psi.types.PyType;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    public void visitPyAssignmentStatement(PyAssignmentStatement node) {
      PyExpression assignedValue = node.getAssignedValue();
      if (assignedValue != null) {
        assignedValue.accept(new PyElementVisitor() {
          @Override
          public void visitPyCallExpression(PyCallExpression node) {
            PyExpression callee = node.getCallee();
            if (callee instanceof PyReferenceExpression) {
              PyReferenceExpression referenceExpression = (PyReferenceExpression) callee;
              PsiPolyVariantReference reference = referenceExpression.getReference();
              PsiElement resolved = reference.resolve();
              if (resolved instanceof Callable) {
                PyType type = ((Callable) resolved).getReturnType(myTypeEvalContext, referenceExpression);
                if (type == null) {
                  final String msg = String.format("Unknown return type");
                  final ProblemHighlightType highlightType = ProblemHighlightType.GENERIC_ERROR_OR_WARNING;
                  registerProblem(node, msg, highlightType, null);
                }
              }
            }
          }
        });
      }
    }
  }

  @Nls
  @NotNull
  @Override
  public String getDisplayName() {
    return "Unknown Return Type Inspection";
  }
}
