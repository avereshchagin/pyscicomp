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
import com.intellij.ui.AnActionButton;
import com.intellij.ui.AnActionButtonRunnable;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBList;
import com.intellij.util.containers.hash.HashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.awt.*;
import java.util.*;
import java.util.List;

public class EditPermissibleValuesDialog extends DialogWrapper {

  private static final String TITLE = "Edit Permissible Values";

  private final PermissibleValuesListModel myListModel;
  private final Project myProject;

  private static class PermissibleValuesListModel implements ListModel {

    private final List<String> myValues;
    private final Set<ListDataListener> myListeners = new HashSet<ListDataListener>();

    private PermissibleValuesListModel(Collection<String> values) {
      myValues = new ArrayList<String>(values);
    }

    @Override
    public int getSize() {
      return myValues.size();
    }

    @Override
    public Object getElementAt(int index) {
      return myValues.get(index);
    }

    @Override
    public void addListDataListener(ListDataListener l) {
      myListeners.add(l);
    }

    @Override
    public void removeListDataListener(ListDataListener l) {
      myListeners.remove(l);
    }

    private void addValue(String value) {
      myValues.add(value);
      for (ListDataListener listener : myListeners) {
        listener.intervalAdded(new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, myValues.size() - 1, myValues.size() - 1));
      }
    }

    private void removeValue(int index) {
      if (index >= 0 && index < myValues.size()) {
        myValues.remove(index);
        for (ListDataListener listener : myListeners) {
          listener.intervalRemoved(new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, index, index));
        }
      }
    }

    private void setValue(int index, String value) {
      if (index >= 0 && index < myValues.size()) {
        myValues.set(index, value);
        for (ListDataListener listener : myListeners) {
          listener.contentsChanged(new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, index, index));
        }
      }
    }

    @NotNull
    private String getValue(int index) {
      if (index >= 0 && index < myValues.size()) {
        return myValues.get(index);
      }
      return "";
    }

    @NotNull
    private List<String> getAllValues() {
      return Collections.unmodifiableList(myValues);
    }
  }

  public EditPermissibleValuesDialog(@Nullable Project project, @NotNull Collection<String> values) {
    super(project, true);
    myListModel = new PermissibleValuesListModel(values);
    myProject = project;

    init();
  }

  protected void init() {
    super.init();
    setTitle(TITLE);
  }

  @Override
  protected JComponent createCenterPanel() {
    final JBList list = new JBList(myListModel);
    list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    ToolbarDecorator decorator = ToolbarDecorator.createDecorator(list).disableUpDownActions()
      .setEditAction(new AnActionButtonRunnable() {
        @Override
        public void run(AnActionButton button) {
          int index = list.getSelectedIndex();
          EditStringValueDialog dialog = new EditStringValueDialog(myProject, "Edit Value", myListModel.getValue(index));
          dialog.show();
          if (dialog.isOK()) {
            myListModel.setValue(index, dialog.getEditResult());
          }
        }
      })
      .setRemoveAction(new AnActionButtonRunnable() {
        @Override
        public void run(AnActionButton button) {
          int index = list.getSelectedIndex();
          myListModel.removeValue(index);
        }
      })
      .setAddAction(new AnActionButtonRunnable() {
        @Override
        public void run(AnActionButton button) {
          EditStringValueDialog dialog = new EditStringValueDialog(myProject, "Add Value", "");
          dialog.show();
          if (dialog.isOK()) {
            myListModel.addValue(dialog.getEditResult());
          }
        }
      });
    decorator.setPreferredSize(new Dimension(150, 250));
    return decorator.createPanel();
  }

  @NotNull
  public Collection<String> getEditResult() {
    return myListModel.getAllValues();
  }
}
