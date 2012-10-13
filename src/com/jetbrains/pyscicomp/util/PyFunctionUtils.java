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
package com.jetbrains.pyscicomp.util;

import com.intellij.psi.PsiElement;
import com.jetbrains.python.psi.*;
import com.jetbrains.python.psi.resolve.PyResolveContext;
import org.jetbrains.annotations.Nullable;

public class PyFunctionUtils {

  // Static usage only
  private PyFunctionUtils() {
  }

  @Nullable
  public static PyFunction getCalleeFunction(@Nullable PyCallExpression callExpression) {
    if (callExpression != null) {
      Callable calleeFunction = callExpression.resolveCalleeFunction(PyResolveContext.defaultContext());
      if (calleeFunction instanceof PyFunction) {
        return (PyFunction) calleeFunction;
      }
    }
    return null;
  }

  @Nullable
  public static PyFunction extractCalleeFunction(PsiElement element) {
    final PyFunction[] function = {null};

    element.accept(new PyElementVisitor() {

      @Override
      public void visitPyTargetExpression(PyTargetExpression node) {
        PyExpression assignedValue = node.findAssignedValue();
        if (assignedValue != null) {
          assignedValue.accept(this);
        }
      }

      @Override
      public void visitPyReferenceExpression(PyReferenceExpression node) {
        PsiElement resolvedElement = node.followAssignmentsChain(PyResolveContext.noImplicits()).getElement();
        if (resolvedElement != null) {
          resolvedElement.accept(this);
        }
      }

      @Override
      public void visitPyFunction(PyFunction node) {
        function[0] = node;
      }
    });

    return function[0];
  }
}
