// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.home.sandbox.action;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.flipkart.zjsonpatch.JsonDiff;
import com.intellij.debugger.engine.DebugProcessImpl;
import com.intellij.debugger.engine.JavaDebugProcess;
import com.intellij.debugger.engine.JavaValue;
import com.intellij.debugger.engine.events.DebuggerCommandImpl;
import com.intellij.debugger.memory.action.DebuggerTreeAction;
import com.intellij.debugger.ui.impl.watch.ValueDescriptorImpl;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerManager;
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider;
import com.intellij.xdebugger.frame.XValue;
import com.intellij.xdebugger.impl.ui.tree.nodes.XValueNodeImpl;
import com.sun.jdi.Value;
import com.twelvemonkeys.util.LinkedSet;
import org.apache.commons.collections4.Trie;
import org.home.sandbox.ui.DisplayDiffDialog;
import org.home.sandbox.SaveVariableService;
import org.home.sandbox.datatransfer.PatchedObject;
import org.home.sandbox.datatransfer.SElement;
import org.home.sandbox.operation.OperationType;
import org.home.sandbox.util.Mappers;
import org.home.sandbox.util.PathUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class OpenSavedVariablesAction extends DebuggerTreeAction {

    SaveVariableService saveVariableService = ApplicationManager.getApplication().getService(SaveVariableService.class);

    @Override
    protected void perform(XValueNodeImpl node, @NotNull String nodeName, AnActionEvent e) {

        Project project = e.getProject();
        XDebugSession session = XDebuggerManager.getInstance(project).getCurrentSession();
        DebugProcessImpl debugProcess = JavaDebugProcess.getCurrentDebugProcess(project);
        final XDebuggerEditorsProvider editorsProvider = session.getDebugProcess().getEditorsProvider();
        debugProcess.getManagerThread().schedule(new DebuggerCommandImpl() {

            @Override
            protected void action() {
                XValue xValue = node.getValueContainer();
                ValueDescriptorImpl valueDescriptor = ((JavaValue) xValue).getDescriptor();

                SElement oldElem = saveVariableService.getVariable();
                Value newValue = valueDescriptor.getValue();

                Pair<SElement, SElement> elemPair = makeSElementPair(oldElem, newValue, nodeName);

                var trie = makeTrieFromValues(oldElem, newValue, nodeName);

                PatchedObject o = new PatchedObject(
                        elemPair.first,
                        elemPair.second,
                        trie,
                        nodeName, nodeName + "/");
                ApplicationManager.getApplication().invokeLater(() -> {
                    DisplayDiffDialog dialog = new DisplayDiffDialog(
                            session.getProject(),
                            editorsProvider,
                            null,
                            nodeName,
                            o,
                            null,
                            session,
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

    private Pair<SElement, SElement> makeSElementPair(SElement oldElem, Value newValue, String nodeName) {
        Map<String, SElement> visited = new HashMap<>();
        SElement newElem = Mappers.fromValueToElem(newValue, nodeName, visited);
        return new Pair<>(oldElem, newElem);
    }

    private Trie<String, OperationType> makeTrieFromValues(SElement oldElement, Value newValue, String nodeName) {
        Set<Integer> visited = new LinkedSet<>();
        JsonNode saved = Mappers.fromElemToJson(oldElement, visited);
        visited.clear();
        JsonNode current = Mappers.fromValueToJson(newValue, visited);
        ArrayNode patchNode = (ArrayNode) JsonDiff.asJson(saved, current);
        return PathUtil.makeTree(patchNode, nodeName);
    }
}

