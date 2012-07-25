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

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.colors.ex.DefaultColorSchemesManager;
import com.intellij.openapi.editor.colors.impl.DefaultColorsScheme;
import com.intellij.openapi.editor.event.DocumentAdapter;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.ui.EditorTextField;
import com.jetbrains.pyscicomp.codeInsight.types.ParameterTypeInformation;
import com.jetbrains.python.PythonFileType;
import com.jetbrains.python.lexer.PythonEditorHighlighter;

import javax.swing.*;
import java.awt.*;

public class EditParameterPanel extends JPanel {

  private static final int LABEL_WIDTH = 100;
  private static final int TEXT_FIELD_WIDTH = 250;
  private static final int HEIGHT = 25;

  private ParameterTypeInformation myParameter;
  private EditorTextField myEditorTextField;
  private boolean myModified = false;

  public EditParameterPanel(ParameterTypeInformation parameter) {
    super(new GridBagLayout());
    myParameter = parameter;

    initComponents();
  }

  private void initComponents() {
    GridBagConstraints constraints = new GridBagConstraints();
    constraints.insets = new Insets(0, 0, 0, 0);
    constraints.gridx = 0;
    constraints.gridy = 0;
    constraints.gridwidth = 1;

    constraints.anchor = GridBagConstraints.LINE_END;
    JLabel label = new JLabel(myParameter.getName());
    label.setPreferredSize(new Dimension(LABEL_WIDTH, HEIGHT));
    add(label, constraints);

    constraints.gridx = 1;
    constraints.anchor = GridBagConstraints.LINE_START;
    myEditorTextField = new EditorTextField(myParameter.getType(), null, PythonFileType.INSTANCE);
    myEditorTextField.setPreferredSize(new Dimension(TEXT_FIELD_WIDTH, HEIGHT));
    myEditorTextField.addDocumentListener(new DocumentAdapter() {
      @Override
      public void documentChanged(DocumentEvent e) {
        myModified = true;
      }
    });

    Editor editor = myEditorTextField.getEditor();
    if (editor instanceof EditorEx) {
      ((EditorEx) editor)
        .setHighlighter(new PythonEditorHighlighter(new DefaultColorsScheme(DefaultColorSchemesManager.getInstance()), null, null));
    }
    add(myEditorTextField, constraints);
  }

  public ParameterTypeInformation getEditResult() {
    return new ParameterTypeInformation(myParameter.getName(), myEditorTextField.getText());
  }

  public boolean isModified() {
    return myModified;
  }
}
