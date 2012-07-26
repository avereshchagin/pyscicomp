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
import com.intellij.ui.EditorTextField;
import com.jetbrains.pyscicomp.codeInsight.types.FunctionTypeInformation;
import com.jetbrains.pyscicomp.codeInsight.types.ParameterTypeInformation;
import com.jetbrains.python.PythonFileType;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class EditTypeInformationPanel extends JPanel {

  private static final String RETURN_TYPE_LABEL = "Return type:";

  private final FunctionTypeInformation myFunction;

  private EditorTextField myReturnTypeField;
  private final List<EditParameterPanel> myParameterFields = new ArrayList<EditParameterPanel>();
  private boolean myModified = false;

  public EditTypeInformationPanel(FunctionTypeInformation function) {
    super(new GridBagLayout());
    myFunction = function;

    initComponents();
  }

  private void initComponents() {
    GridBagConstraints constraints = new GridBagConstraints();
    constraints.insets = new Insets(5, 5, 5, 5);

    constraints.gridx = 0;
    constraints.gridy = 0;
    constraints.gridwidth = 2;
    constraints.anchor = GridBagConstraints.LINE_START;
    add(new JLabel(myFunction.getName()), constraints);

    constraints.gridwidth = 1;

    constraints.gridx = 0;
    constraints.gridy = 1;
    constraints.anchor = GridBagConstraints.LINE_END;
    add(new JLabel(RETURN_TYPE_LABEL), constraints);

    constraints.gridx = 1;
    constraints.gridy = 1;
    constraints.anchor = GridBagConstraints.LINE_START;

    myReturnTypeField = new EditorTextField(myFunction.getReturnType(), null, PythonFileType.INSTANCE);
    myReturnTypeField.setPreferredSize(new Dimension(250, 25));
    add(myReturnTypeField, constraints);
    myReturnTypeField.addDocumentListener(new DocumentAdapter() {
      @Override
      public void documentChanged(DocumentEvent e) {
        myModified = true;
      }
    });

    int y = 2;
    for (ParameterTypeInformation parameter : myFunction.getParameters()) {
      constraints.gridx = 0;
      constraints.gridy = y;
      constraints.gridwidth = 2;
      EditParameterPanel parameterPanel = new EditParameterPanel(parameter);
      myParameterFields.add(parameterPanel);
      add(parameterPanel, constraints);
      y++;
    }
  }

  public FunctionTypeInformation getEditResult() {
    List<ParameterTypeInformation> parameters = new ArrayList<ParameterTypeInformation>();
    for (EditParameterPanel field : myParameterFields) {
      parameters.add(field.getEditResult());
    }
    return new FunctionTypeInformation(myFunction.getName(), myReturnTypeField.getText(), parameters);
  }

  public boolean isModified() {
    if (myModified) {
      return true;
    }
    for (EditParameterPanel field : myParameterFields) {
      if (field.isModified()) {
        return true;
      }
    }
    return false;
  }
}
