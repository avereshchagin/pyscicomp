package com.jetbrains.pyscicomp;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyFunction;
import com.jetbrains.python.psi.PyQualifiedExpression;
import com.jetbrains.python.psi.impl.PyQualifiedName;
import com.jetbrains.python.psi.resolve.ResolveImportUtil;
import com.jetbrains.python.psi.types.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NumpyTypeProvider extends PyTypeProviderBase {

  @Override
  public PyType getReturnType(PyFunction function, @Nullable PyQualifiedExpression callSite, TypeEvalContext context) {
    PyQualifiedName qualifiedName = getQualifiedName(function, callSite);
    if (qualifiedName != null && qualifiedName.getComponentCount() > 0 && "numpy".equals(qualifiedName.getComponents().get(0))) {
      NumpyNamesService namesService = NumpyNamesService.getInstance();
      String returnType = namesService.functionsToReturnTypes.get(qualifiedName.toString());
      if (returnType != null) {
        System.out.println("Function: " + qualifiedName);
        System.out.println("Return type: " + returnType);
        System.out.println();

        return PyTypeParser.getTypeByName(function, returnType);
      }
    }
    return null;
  }

  @Nullable
  private static PyQualifiedName getQualifiedName(@NotNull PyFunction function, @Nullable PsiElement callSite) {
    if (!function.isValid()) {
      return null;
    }
    String result = function.getName();
    PyClass containingClass = function.getContainingClass();
    VirtualFile vfile = function.getContainingFile().getVirtualFile();
    if (vfile != null) {
      String module = ResolveImportUtil.findShortestImportableName(callSite != null ? callSite : function, vfile);
      result = String.format("%s.%s%s", module, containingClass != null ? containingClass.getName() + "." : "", result);
    }
    return result != null ? PyQualifiedName.fromDottedString(result) : null;
  }
}
