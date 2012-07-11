package com.jetbrains.pyscicomp.documentation;

import com.intellij.lang.documentation.AbstractDocumentationProvider;
import com.intellij.psi.PsiElement;
import com.jetbrains.pyscicomp.Utils;
import com.jetbrains.python.psi.PyFunction;

public class NumpyDocumentationProvider extends AbstractDocumentationProvider {

  @Override
  public String generateDoc(PsiElement element, PsiElement originalElement) {
    if (element instanceof PyFunction) {
      PyFunction function = (PyFunction) element;
      if (Utils.isNumpyFunction(function, null)) {
        String docStringValue = function.getDocStringValue();
        NumpyDocString docString = NumpyDocString.parse(docStringValue);
        NumpyDocumentationBuilder builder = new NumpyDocumentationBuilder();
        for (DocStringParameter parameter : docString.getParameters()) {
          builder.addParameter(parameter);
        }
        for (DocStringParameter parameter : docString.getReturns()) {
          builder.addReturn(parameter);
        }
        return builder.build();
      }
    }
    return null;
  }
}
