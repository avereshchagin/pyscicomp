package com.jetbrains.pyscicomp;

import com.jetbrains.python.psi.PyFunction;
import com.jetbrains.python.psi.PyQualifiedExpression;
import com.jetbrains.python.psi.impl.PyQualifiedName;
import com.jetbrains.python.psi.resolve.ResolveImportUtil;
import com.jetbrains.python.psi.types.PyType;
import com.jetbrains.python.psi.types.PyTypeProviderBase;
import com.jetbrains.python.psi.types.TypeEvalContext;
import org.jetbrains.annotations.Nullable;

public class NumpyTypeProvider extends PyTypeProviderBase {

  @Override
  public PyType getReturnType(PyFunction function, @Nullable PyQualifiedExpression callSite, TypeEvalContext context) {
    System.out.println("Function: " + function.getName());
    if (function.getContainingClass() != null) {
      System.out.println("Class: " + function.getContainingClass().getName());
      System.out.println("Qualified class name: " + function.getContainingClass().getQualifiedName());
    }

    PyQualifiedName name = ResolveImportUtil.findCanonicalImportPath(function, null);
    System.out.println(name);

    System.out.println();
    //ResolveImportUtil.findShortestImportableName(function)
    return null;
  }
}
