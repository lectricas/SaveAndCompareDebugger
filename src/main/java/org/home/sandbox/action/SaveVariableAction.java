// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.home.sandbox.action;

import com.intellij.debugger.engine.DebugProcessImpl;
import com.intellij.debugger.engine.JavaDebugProcess;
import com.intellij.debugger.engine.JavaValue;
import com.intellij.debugger.engine.events.DebuggerCommandImpl;
import com.intellij.debugger.memory.action.DebuggerTreeAction;
import com.intellij.debugger.ui.impl.watch.ValueDescriptorImpl;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.xdebugger.frame.XValue;
import com.intellij.xdebugger.impl.ui.tree.nodes.XValueNodeImpl;
import com.sun.jdi.Value;
import org.home.sandbox.SaveVariableService;
import org.home.sandbox.datatransfer.SElement;
import org.home.sandbox.util.Mappers;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class SaveVariableAction extends DebuggerTreeAction {

    @Override
    protected void perform(XValueNodeImpl node, @NotNull String nodeName, AnActionEvent e) {
        Project project = e.getProject();
        DebugProcessImpl debugProcess = JavaDebugProcess.getCurrentDebugProcess(project);
        SaveVariableService saveVariableService = ApplicationManager.getApplication().getService(SaveVariableService.class);
        debugProcess.getManagerThread().schedule(new DebuggerCommandImpl() {

            @Override
            protected void action() {
                XValue xValue = node.getValueContainer();
                ValueDescriptorImpl valueDescriptor = ((JavaValue) xValue).getDescriptor();
                Value v = valueDescriptor.getValue();
                Map<String, SElement> visited = new HashMap<>();
                SElement current = Mappers.fromValueToElem(v, nodeName, visited);
                saveVariableService.saveVariable(current);
            }
        });
    }
}

