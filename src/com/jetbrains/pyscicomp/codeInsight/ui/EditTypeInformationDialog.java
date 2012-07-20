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
import com.jetbrains.pyscicomp.codeInsight.types.FunctionTypeInformation;
import com.jetbrains.pyscicomp.codeInsight.types.TypeInformationCache;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class EditTypeInformationDialog extends DialogWrapper {

  private static final String TITLE = "Edit Type Information";

  private final FunctionTypeInformation myFunction;
  private EditTypeInformationPanel myEditTypeInformationPanel;

  public EditTypeInformationDialog(@Nullable Project project, @NotNull FunctionTypeInformation typeInformation) {
    super(project, true);
    myFunction = typeInformation;

    init();
  }

  protected void init() {
    super.init();
    setTitle(TITLE);
  }

  @Override
  protected JComponent createCenterPanel() {
    myEditTypeInformationPanel = new EditTypeInformationPanel(myFunction);
    return myEditTypeInformationPanel;
  }

  @Override
  protected void doOKAction() {
    FunctionTypeInformation typeInformation = myEditTypeInformationPanel.getEditResult();
    TypeInformationCache cache = TypeInformationCache.getInstance();
    cache.putFunction(typeInformation, true);

    super.doOKAction();
  }
}
