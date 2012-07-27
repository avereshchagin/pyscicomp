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
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBList;
import com.intellij.util.containers.hash.HashSet;
import com.jetbrains.pyscicomp.codeInsight.types.FunctionTypeInformation;
import com.jetbrains.pyscicomp.codeInsight.types.TypeInformationCache;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class TypeInformationConfigurable implements Configurable {

  private boolean myModified = false;

  @Nullable
  private EditTypeInformationPanel myEditTypeInformationPanel = null;

  private final List<FunctionTypeInformation> pendingModifications = new ArrayList<FunctionTypeInformation>();

  private final FunctionsListModel myListModel = new FunctionsListModel();
  private final JTextField myFilterTextField = new JTextField();
  private final JCheckBox mySortCheckBox = new JCheckBox("Exactly matches", false);

  private static class FunctionsListModel implements ListModel {

    private List<FunctionTypeInformation> myDisplayedFunctions;
    private final Set<ListDataListener> myListeners = new HashSet<ListDataListener>();
    private int myOldSize = 0;

    private FunctionsListModel() {
      applyFilter(null, false);
    }

    @Override
    public int getSize() {
      return myDisplayedFunctions.size();
    }

    @Override
    public Object getElementAt(int index) {
      return myDisplayedFunctions.get(index);
    }

    @Override
    public void addListDataListener(ListDataListener l) {
      myListeners.add(l);
    }

    @Override
    public void removeListDataListener(ListDataListener l) {
      myListeners.remove(l);
    }

    private void sort() {
      Collections.sort(myDisplayedFunctions, new Comparator<FunctionTypeInformation>() {
        @Override
        public int compare(FunctionTypeInformation o1, FunctionTypeInformation o2) {
          return o1.getName().compareTo(o2.getName());
        }
      });
    }

    private void applyFilter(@Nullable Pattern filterPattern, boolean exactlyMatches) {
      List<FunctionTypeInformation> functions = TypeInformationCache.getInstance().getAsList();
      if (filterPattern == null) {
        myDisplayedFunctions = functions;
      } else {
        myDisplayedFunctions.clear();
        for (FunctionTypeInformation function : functions) {
          Matcher matcher = filterPattern.matcher(function.getName());
          if ((exactlyMatches && matcher.matches()) || (!exactlyMatches && matcher.find())) {
            myDisplayedFunctions.add(function);
          }
        }
      }
      sort();
      for (ListDataListener listener : myListeners) {
        if (myOldSize > 0) {
          listener.intervalRemoved(new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, 0, myOldSize - 1));
        }
        if (!myDisplayedFunctions.isEmpty()) {
          listener.intervalAdded(new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, 0, myDisplayedFunctions.size() - 1));
        }
      }
      myOldSize = myDisplayedFunctions.size();
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

  private void onFilterChanged() {
    String filterText = myFilterTextField.getText();
    boolean exactlyMatches = mySortCheckBox.isSelected();
    if (filterText.isEmpty()) {
      myListModel.applyFilter(null, exactlyMatches);
    } else {
      try {
        Pattern pattern = Pattern.compile(filterText);
        myFilterTextField.setForeground(Color.BLACK);
        myListModel.applyFilter(pattern, exactlyMatches);
      } catch (PatternSyntaxException ex) {
        myFilterTextField.setForeground(Color.RED);
      }
    }
  }

  private JComponent createControlsPanel() {
    JPanel controlsPanel = new JPanel(new BorderLayout());
    controlsPanel.add(new JLabel("Filter with regular expression: "), BorderLayout.WEST);

    myFilterTextField.getDocument().addDocumentListener(new DocumentAdapter() {
      @Override
      protected void textChanged(DocumentEvent e) {
        onFilterChanged();
      }
    });
    controlsPanel.add(myFilterTextField, BorderLayout.CENTER);

    mySortCheckBox.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        onFilterChanged();
      }
    });
    controlsPanel.add(mySortCheckBox, BorderLayout.EAST);

    return controlsPanel;
  }

  @Override
  public JComponent createComponent() {
    JPanel firstComponent = new JPanel(new BorderLayout());
    firstComponent.add(createControlsPanel(), BorderLayout.NORTH);

    final JBList list = new JBList(myListModel);
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

    firstComponent.add(decorator.createPanel(), BorderLayout.CENTER);

    final Splitter splitter = new Splitter(true);
    splitter.setFirstComponent(firstComponent);
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
