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

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.ui.Splitter;
import com.intellij.ui.AnActionButton;
import com.intellij.ui.AnActionButtonRunnable;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBList;
import com.jetbrains.pyscicomp.codeInsight.types.FunctionTypeInfo;
import com.jetbrains.pyscicomp.codeInsight.types.PredefinedTypeInformationService;
import org.jetbrains.annotations.Nls;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TypeInformationConfigurable implements Configurable {

  private final List<FunctionWrapper> myFunctions = new ArrayList<FunctionWrapper>();

  public TypeInformationConfigurable() {
    PredefinedTypeInformationService service = PredefinedTypeInformationService.getInstance();
    for (Map.Entry<String, FunctionTypeInfo> entry : service.functions.entrySet()) {
      myFunctions.add(new FunctionWrapper(entry));
    }
  }

  @Nls
  @Override
  public String getDisplayName() {
    return "User-Defined Type Information";
  }

  @Override
  public String getHelpTopic() {
    return null;
  }

  @Override
  public JComponent createComponent() {
    final JBList list = new JBList(myFunctions);

    list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    ToolbarDecorator decorator = ToolbarDecorator.createDecorator(list).disableUpDownActions()
      .setEditAction(new AnActionButtonRunnable() {
        @Override
        public void run(AnActionButton button) {
          edit();
        }
      })
      .setRemoveAction(new AnActionButtonRunnable() {
        @Override
        public void run(AnActionButton button) {
          remove();
        }
      });



    final Splitter splitter = new Splitter(true);
    splitter.setFirstComponent(decorator.createPanel());

    list.addListSelectionListener(new ListSelectionListener() {
      @Override
      public void valueChanged(ListSelectionEvent e) {
        Object obj = list.getSelectedValue();
        if (obj instanceof FunctionWrapper) {
          splitter.setSecondComponent(new EditTypeInformationPanel((FunctionWrapper) obj));
        }
      }
    });

    return splitter;
  }

  @Override
  public boolean isModified() {
    return false;
  }

  @Override
  public void apply() throws ConfigurationException {

  }

  @Override
  public void reset() {

  }

  @Override
  public void disposeUIResources() {

  }

  private void edit() {

  }

  private void remove() {

  }
}
