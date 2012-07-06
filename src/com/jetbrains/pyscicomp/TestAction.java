package com.jetbrains.pyscicomp;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class TestAction extends AnAction {

  @Override
  public void actionPerformed(AnActionEvent e) {
    System.out.println("Action works");
  }
}
