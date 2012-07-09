package com.jetbrains.pyscicomp;

import com.intellij.codeInspection.*;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiPolyVariantReference;
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
                            new AddTypeInformationFix(NumpyTypeProvider.getQualifiedName(function, referenceExpression), parameters));
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
      new AddTypeInformationDialog(project, myFunction, myParameters).show();
    }
  }

  @Nls
  @NotNull
  @Override
  public String getDisplayName() {
    return "Unknown Return Type Inspection";
  }
}
