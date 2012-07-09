package com.jetbrains.pyscicomp;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class AddTypeInformationDialog extends DialogWrapper {

  private static final int TEXT_FIELD_WIDTH = 15;
  private static final String TITLE = "Add Type Information";
  private static final String FUNCTION_LABEL = "<html>Function: ";
  private static final String RETURN_TYPE_LABEL = "Return type: ";

  private final String myFunctionName;
  private final List<String> myParameters;
  private final JTextField returnTypeTextField = new JTextField(TEXT_FIELD_WIDTH);
  private final Map<String, JTextField> parametersToTextFields = new HashMap<String, JTextField>();

  public AddTypeInformationDialog(@Nullable Project project, @NotNull String functionName, @NotNull List<String> parameters) {
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
    rootPanel.add(returnTypeTextField, constraints);

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

      parametersToTextFields.put(parameter, textField);
      y++;
    }

    return rootPanel;
  }

  @Override
  protected void doOKAction() {
    NumpyNamesService namesService = NumpyNamesService.getInstance();
    namesService.functionsToReturnTypes.put(myFunctionName, returnTypeTextField.getText());

    super.doOKAction();
  }
}
