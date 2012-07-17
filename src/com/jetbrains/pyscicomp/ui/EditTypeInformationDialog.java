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
package com.jetbrains.pyscicomp.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.jetbrains.pyscicomp.types.FunctionTypeInfo;
import com.jetbrains.pyscicomp.types.PredefinedTypeInformationService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class EditTypeInformationDialog extends DialogWrapper {

  private static final int TEXT_FIELD_WIDTH = 15;
  private static final String TITLE = "Edit Type Information";
  private static final String FUNCTION_LABEL = "<html>Function: ";
  private static final String RETURN_TYPE_LABEL = "Return type: ";

  private final String myFunctionName;
  private final List<String> myParameters;
  private final JTextField myReturnTypeTextField = new JTextField(TEXT_FIELD_WIDTH);
  private final Map<String, JTextField> myParametersToTextFields = new HashMap<String, JTextField>();

  public EditTypeInformationDialog(@Nullable Project project, @NotNull String functionName, @NotNull List<String> parameters) {
    super(project, true);
    myFunctionName = functionName;
    myParameters = parameters;
    init();
  }

  protected void init() {
    super.init();
    setTitle(TITLE);
  }

  @Override
  protected JComponent createCenterPanel() {
    JPanel rootPanel = new JPanel(new GridBagLayout());
    GridBagConstraints constraints = new GridBagConstraints();
    constraints.insets = new Insets(5, 5, 5, 5);

    constraints.gridx = 0;
    constraints.gridy = 0;
    constraints.gridwidth = 2;
    constraints.anchor = GridBagConstraints.LINE_START;
    rootPanel.add(new JLabel(FUNCTION_LABEL + "<b>" + myFunctionName + "</b>"), constraints);

    constraints.gridwidth = 1;

    constraints.gridx = 0;
    constraints.gridy = 1;
    constraints.anchor = GridBagConstraints.LINE_END;
    rootPanel.add(new JLabel(RETURN_TYPE_LABEL), constraints);

    constraints.gridx = 1;
    constraints.gridy = 1;
    constraints.anchor = GridBagConstraints.LINE_START;
    rootPanel.add(myReturnTypeTextField, constraints);

    int y = 2;
    for (String parameter : myParameters) {
      JTextField textField = new JTextField(TEXT_FIELD_WIDTH);

      constraints.gridx = 0;
      constraints.gridy = y;
      constraints.anchor = GridBagConstraints.LINE_END;
      rootPanel.add(new JLabel(parameter + ":"), constraints);

      constraints.gridx = 1;
      constraints.gridy = y;
      constraints.anchor = GridBagConstraints.LINE_START;
      rootPanel.add(textField, constraints);

      myParametersToTextFields.put(parameter, textField);
      y++;
    }

    obtainFieldsFromDatabase();

    return rootPanel;
  }

  private void obtainFieldsFromDatabase() {
    String obtainedReturnType = PredefinedTypeInformationService.getReturnType(myFunctionName);
    if (obtainedReturnType != null) {
      myReturnTypeTextField.setText(obtainedReturnType);
    }

    List<FunctionTypeInfo.Parameter> obtainedParameters = PredefinedTypeInformationService.getParameters(myFunctionName);
    if (obtainedParameters != null) {
      for (FunctionTypeInfo.Parameter parameter : obtainedParameters) {
        myParametersToTextFields.get(parameter.name).setText(parameter.type.name);
      }
    }
  }

  @Override
  protected void doOKAction() {
    PredefinedTypeInformationService.setReturnType(myFunctionName, myReturnTypeTextField.getText());

    List<FunctionTypeInfo.Parameter> parameters = new ArrayList<FunctionTypeInfo.Parameter>();
    for (String parameterName : myParameters) {
      String parameterType = myParametersToTextFields.get(parameterName).getText();
      parameters.add(new FunctionTypeInfo.Parameter(parameterName, new FunctionTypeInfo.Type(parameterType), false,
                                                    Collections.<String>emptySet()));
    }
    PredefinedTypeInformationService.setParameters(myFunctionName, parameters);

    super.doOKAction();
  }
}
