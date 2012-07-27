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

import com.intellij.openapi.editor.event.DocumentAdapter;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.project.Project;
import com.intellij.ui.EditorTextField;
import com.jetbrains.pyscicomp.codeInsight.types.ParameterTypeInformation;
import com.jetbrains.python.PythonFileType;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;

public class EditParameterPanel extends JPanel {

  private static final int LABEL_WIDTH = 100;
  private static final int TEXT_FIELD_WIDTH = 250;
  private static final int HEIGHT = 25;

  private final ParameterTypeInformation myParameter;
  private EditorTextField myEditorTextField;
  private boolean myModified = false;
  private final Project myProject;
  private Collection<String> myPermissibleValues;

  public EditParameterPanel(@Nullable Project project, ParameterTypeInformation parameter) {
    super(new GridBagLayout());
    myParameter = parameter;
    myProject = project;
    myPermissibleValues = parameter.getPermissibleValues();

    initComponents();
  }

  private void initComponents() {
    GridBagConstraints constraints = new GridBagConstraints();
    constraints.insets = new Insets(0, 0, 0, 5);
    constraints.gridx = 0;
    constraints.gridy = 0;
    constraints.gridwidth = 1;

    constraints.anchor = GridBagConstraints.LINE_END;
    JLabel label = new JLabel(myParameter.getName());
    label.setPreferredSize(new Dimension(LABEL_WIDTH, HEIGHT));
    add(label, constraints);

    constraints.gridx = 1;
    constraints.anchor = GridBagConstraints.LINE_START;
    myEditorTextField = new EditorTextField(myParameter.getType(), myProject, PythonFileType.INSTANCE);
    myEditorTextField.setPreferredSize(new Dimension(TEXT_FIELD_WIDTH, HEIGHT));
    myEditorTextField.addDocumentListener(new DocumentAdapter() {
      @Override
      public void documentChanged(DocumentEvent e) {
        myModified = true;
      }
    });
    add(myEditorTextField, constraints);

    constraints.gridx = 2;
    JButton button = new JButton("Edit values...");
    button.setPreferredSize(new Dimension(120, HEIGHT));
    button.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        EditPermissibleValuesDialog dialog = new EditPermissibleValuesDialog(myProject, myPermissibleValues);
        dialog.show();
        if (dialog.isOK()) {
          myPermissibleValues = dialog.getEditResult();
          myModified = true;
        }
      }
    });
    add(button, constraints);

    myEditorTextField.setPreferredSize(new Dimension(TEXT_FIELD_WIDTH, HEIGHT));
  }

  public ParameterTypeInformation getEditResult() {
    return new ParameterTypeInformation(myParameter.getName(), myEditorTextField.getText(), myPermissibleValues);
  }

  public boolean isModified() {
    return myModified;
  }
}
