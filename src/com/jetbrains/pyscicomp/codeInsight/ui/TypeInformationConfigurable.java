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
import com.jetbrains.pyscicomp.codeInsight.types.FunctionTypeInformation;
import com.jetbrains.pyscicomp.codeInsight.types.TypeInformationCache;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.util.ArrayList;
import java.util.List;

public class TypeInformationConfigurable implements Configurable {

  private boolean myModified = false;

  @Nullable
  private EditTypeInformationPanel myEditTypeInformationPanel = null;

  private final List<FunctionTypeInformation> pendingModifications = new ArrayList<FunctionTypeInformation>();

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
    final JBList list = new JBList(TypeInformationCache.getInstance().getAsList());

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
    splitter.setSecondComponent(new JPanel());

    list.addListSelectionListener(new ListSelectionListener() {
      @Override
      public void valueChanged(ListSelectionEvent e) {
        Object obj = list.getSelectedValue();
        if (obj instanceof FunctionTypeInformation) {
          checkModification();
          myEditTypeInformationPanel = new EditTypeInformationPanel(null, (FunctionTypeInformation) obj);
          splitter.setSecondComponent(myEditTypeInformationPanel);
        }
      }
    });

    return splitter;
  }

  @Override
  public boolean isModified() {
    checkModification();
    return myModified;
  }

  @Override
  public void apply() throws ConfigurationException {
    checkModification();
    TypeInformationCache cache = TypeInformationCache.getInstance();
    for (FunctionTypeInformation modification : pendingModifications) {
      cache.putFunction(modification, false);
    }
    cache.save();
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

  private void checkModification() {
    if (myEditTypeInformationPanel != null && myEditTypeInformationPanel.isModified()) {
      myModified = true;
      pendingModifications.add(myEditTypeInformationPanel.getEditResult());
      myEditTypeInformationPanel = null;
    }
  }
}
