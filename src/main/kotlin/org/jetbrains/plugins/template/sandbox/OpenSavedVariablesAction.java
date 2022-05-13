// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.template.sandbox;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.flipkart.zjsonpatch.JsonDiff;
import com.intellij.debugger.engine.DebugProcessImpl;
import com.intellij.debugger.engine.JavaDebugProcess;
import com.intellij.debugger.engine.JavaValue;
import com.intellij.debugger.engine.events.DebuggerCommandImpl;
import com.intellij.debugger.memory.action.DebuggerTreeAction;
import com.intellij.debugger.streams.sandbox.dto.SElement;
import com.intellij.debugger.ui.impl.watch.ValueDescriptorImpl;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider;
import com.intellij.xdebugger.frame.XValue;
import com.intellij.xdebugger.impl.ui.DebuggerUIUtil;
import com.intellij.xdebugger.impl.ui.tree.XInspectDialog;
import com.intellij.xdebugger.impl.ui.tree.nodes.XValueNodeImpl;
import com.sun.jdi.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.template.sandbox.dto.SElement;

public class OpenSavedVariablesAction extends DebuggerTreeAction {

  SaveVariableService saveVariableService = ApplicationManager.getApplication().getService(SaveVariableService.class);

  @Override
  protected void perform(XValueNodeImpl node, @NotNull String nodeName, AnActionEvent e) {
    DebugProcessImpl debugProcess = JavaDebugProcess.getCurrentDebugProcess(e);
    var session = debugProcess.getSession().getXDebugSession();
    final XDebuggerEditorsProvider editorsProvider = session.getDebugProcess().getEditorsProvider();
    debugProcess.getManagerThread().schedule(new DebuggerCommandImpl() {

      @Override
      protected void action() {
        XValue xValue = node.getValueContainer();
        ValueDescriptorImpl valueDescriptor = ((JavaValue)xValue).getDescriptor();
        Value v = valueDescriptor.getValue();
        JsonNode current = Mappers.toJsonElement(v, nodeName);

        SElement element = saveVariableService.getVariable();
        JsonNode saved = Mappers.toJsonElement(element, nodeName);

        JsonNode patchNode = JsonDiff.asJson(saved, current);
        //JsonPatch.apply()

        Mappers.markNodesWithDiff((ArrayNode)patchNode, element, Mappers.toElement(v, nodeName));
        ApplicationManager.getApplication().invokeLater(() -> {
          XInspectDialog dialog = new XInspectDialog(
            session.getProject(),
            editorsProvider,
            null,
            nodeName,
            element,
            null,
            DebuggerUIUtil.getSession(e),
            true
          );
          dialog.show();
        });
      }
    });
  }

  @Override
  protected boolean isEnabled(@NotNull XValueNodeImpl node, @NotNull AnActionEvent e) {
    return saveVariableService.getVariable() != null;
  }
}
