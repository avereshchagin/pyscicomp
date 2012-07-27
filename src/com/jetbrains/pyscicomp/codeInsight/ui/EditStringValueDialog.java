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

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class EditStringValueDialog extends DialogWrapper {

  private final String myTitle;
  private final JTextField myTextField;

  public EditStringValueDialog(@Nullable Project project, @NotNull String title, @NotNull String currentValue) {
    super(project, true);
    myTitle = title;
    myTextField = new JTextField(currentValue);
    init();
  }

  protected void init() {
    super.init();
    setTitle(myTitle);
    myTextField.setPreferredSize(new Dimension(200, 25));
  }

  @Override
  protected JComponent createCenterPanel() {
    return myTextField;
  }

  @Override
  public JComponent getPreferredFocusedComponent() {
    return myTextField;
  }

  public String getEditResult() {
    return myTextField.getText();
  }
}
