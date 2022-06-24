// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.home.sandbox.ui;

import com.intellij.openapi.project.Project;
import com.intellij.ui.tree.TreePathUtil;
import com.intellij.util.ui.UIUtil;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider;
import com.intellij.xdebugger.impl.frame.XValueMarkers;
import com.intellij.xdebugger.impl.ui.tree.XDebuggerTree;
import com.intellij.xdebugger.impl.ui.tree.nodes.XValueContainerNode;
import org.home.sandbox.datatransfer.PatchedObject;
import org.home.sandbox.datatransfer.SElement;
import org.home.sandbox.operation.OperationType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.tree.TreePath;
import java.awt.*;

public class ColorfulTree extends XDebuggerTree {
    public ColorfulTree(@NotNull Project project,
                        @NotNull XDebuggerEditorsProvider editorsProvider,
                        @Nullable XSourcePosition sourcePosition,
                        @NotNull String popupActionGroupId,
                        @Nullable XValueMarkers<?, ?> valueMarkers) {
        super(project, editorsProvider, sourcePosition, popupActionGroupId, valueMarkers);
    }

    @Override
    public boolean isFileColorsEnabled() {
        return true;
    }

    private Color add = new Color(159, 175, 54, 60);
    private Color remove = new Color(243, 119, 119, 60);
    private Color change = new Color(58, 76, 145, 60);

    @Override
    public @Nullable Color getFileColorForPath(@NotNull TreePath path) {
        final Color background = UIUtil.getTreeSelectionBackground(true);
        if (TreePathUtil.toTreeNode(path) instanceof XValueContainerNode) {
            Object o = ((XValueContainerNode) TreePathUtil.toTreeNode(path)).getValueContainer();
            if (o instanceof SElement) {
                if (((SElement) o).whatChanged == OperationType.ADD) {
                    return add;
                } else if (((SElement) o).whatChanged == OperationType.REMOVE) {
                    return remove;
                } else {
                    return null;
                }
            } else if (o instanceof PatchedObject) {
                if (((PatchedObject) o).operationType == OperationType.NOTHING) {
                    return null;
                } else {
                    return change;
                }
            } else {
                return Color.RED;
            }
        } else {
            return Color.RED;
        }
    }
}
