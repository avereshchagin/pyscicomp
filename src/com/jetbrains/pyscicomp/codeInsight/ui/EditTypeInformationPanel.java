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

import com.intellij.ui.EditorTextField;
import com.jetbrains.pyscicomp.codeInsight.types.FunctionTypeInfo;
import com.jetbrains.python.PythonFileType;

import javax.swing.*;
import java.awt.*;

public class EditTypeInformationPanel extends JPanel {

  private final FunctionWrapper myFunction;

  public EditTypeInformationPanel(FunctionWrapper function) {
    super(new GridBagLayout());
    myFunction = function;

    createComponents();
  }

  private void createComponents() {
    GridBagConstraints constraints = new GridBagConstraints();
    constraints.insets = new Insets(5, 5, 5, 5);

    constraints.gridx = 0;
    constraints.gridy = 0;
    constraints.gridwidth = 2;
    constraints.anchor = GridBagConstraints.LINE_START;
    add(new JLabel(myFunction.toString()), constraints);

    constraints.gridwidth = 1;

    constraints.gridx = 0;
    constraints.gridy = 1;
    constraints.anchor = GridBagConstraints.LINE_END;
    add(new JLabel("<Return type>"), constraints);

    constraints.gridx = 1;
    constraints.gridy = 1;
    constraints.anchor = GridBagConstraints.LINE_START;

    EditorTextField editorTextField = new EditorTextField("", null, PythonFileType.INSTANCE);

    add(editorTextField, constraints);
    editorTextField.setPreferredSize(new Dimension(250, 25));

    int y = 2;
    for (FunctionTypeInfo.Parameter parameter : myFunction.getTypeInfo().parameters) {
      constraints.gridx = 0;
      constraints.gridy = y;
      constraints.gridwidth = 2;
      add(new EditParameterPanel(parameter), constraints);
      y++;
    }
  }
}
