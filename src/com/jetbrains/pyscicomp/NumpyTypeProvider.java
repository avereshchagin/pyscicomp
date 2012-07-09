package com.jetbrains.pyscicomp;

import com.intellij.openapi.util.text.StringUtil;
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

import java.util.ArrayList;
import java.util.List;

public class NumpyTypeProvider extends PyTypeProviderBase {

  @Nullable
  @Override
  public PyType getReturnType(PyFunction function, @Nullable PyQualifiedExpression callSite, TypeEvalContext context) {
    if (function.isValid()) {
      String qualifiedName = getQualifiedName(function, callSite);
      if (qualifiedName.startsWith("numpy.")) {
        NumpyNamesService namesService = NumpyNamesService.getInstance();
        String returnType = namesService.functionsToReturnTypes.get(qualifiedName);
        if (returnType != null) {
          System.out.println("Function: " + qualifiedName);
          System.out.println("Return type: " + returnType);
          System.out.println();

          return PyTypeParser.getTypeByName(function, returnType);
        }
      }
    }
    return null;
  }

  @NotNull
  public static String getQualifiedName(@NotNull PyFunction function, @Nullable PsiElement callSite) {
    assert function.isValid();
    List<String> result = new ArrayList<String>();
    PyClass containingClass = function.getContainingClass();
    VirtualFile virtualFile = function.getContainingFile().getVirtualFile();
    if (virtualFile != null) {
      String module = ResolveImportUtil.findShortestImportableName(callSite != null ? callSite : function, virtualFile);
      if (module != null) {
        result.add(module);
      }
    }
    if (containingClass != null) {
      result.add(containingClass.getName());
    }
    result.add(function.getName());
    return StringUtil.join(result, ".");
  }
}
