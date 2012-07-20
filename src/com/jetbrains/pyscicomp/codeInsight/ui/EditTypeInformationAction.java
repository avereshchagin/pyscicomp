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
package com.jetbrains.pyscicomp.codeInsight.ui;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ex.util.EditorUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.jetbrains.pyscicomp.codeInsight.types.FunctionTypeInformation;
import com.jetbrains.python.psi.PyFunction;

public class EditTypeInformationAction extends AnAction {

  @Override
  public void actionPerformed(AnActionEvent e) {
    Project project = PlatformDataKeys.PROJECT.getData(e.getDataContext());
    Editor editor = PlatformDataKeys.EDITOR.getData(e.getDataContext());
    if (project != null && editor != null) {
      int offset = editor.getCaretModel().getOffset();
      if (!EditorUtil.inVirtualSpace(editor, editor.getCaretModel().getLogicalPosition())) {
        Document document = editor.getDocument();
        PsiFile file = PsiDocumentManager.getInstance(project).getPsiFile(document);
        if (file != null) {
          PsiReference referenceAt = file.findReferenceAt(offset);
          if (referenceAt != null) {
            PsiElement resolved = referenceAt.resolve();
            if (resolved instanceof PyFunction) {
              FunctionTypeInformation typeInformation =
                FunctionTypeInformation.fromPyFunction((PyFunction) resolved, referenceAt.getElement());
              new EditTypeInformationDialog(project, typeInformation).show();
            }
          }
        }
      }
    }
  }
}
