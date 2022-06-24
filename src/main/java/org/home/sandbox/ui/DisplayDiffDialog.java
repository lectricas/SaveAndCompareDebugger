package org.home.sandbox.ui;

import com.intellij.concurrency.ResultConsumer;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.Pair;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerBundle;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider;
import com.intellij.xdebugger.frame.XValue;
import com.intellij.xdebugger.impl.actions.XDebuggerActions;
import com.intellij.xdebugger.impl.evaluate.quick.common.DebuggerTreeCreator;
import com.intellij.xdebugger.impl.evaluate.quick.common.DebuggerTreeWithHistoryPanel;
import com.intellij.xdebugger.impl.frame.XValueMarkers;
import com.intellij.xdebugger.impl.ui.tree.XDebuggerTree;
import com.intellij.xdebugger.impl.ui.tree.nodes.XValueNodeImpl;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class DisplayDiffDialog extends DialogWrapper {
    private final DebuggerTreeWithHistoryPanel myDebuggerTreePanel;
    private final boolean myRebuildOnSessionEvents;

    public DisplayDiffDialog(@NotNull Project project,
                          XDebuggerEditorsProvider editorsProvider,
                          XSourcePosition sourcePosition,
                          @NotNull String name,
                          @NotNull XValue value,
                          XValueMarkers<?, ?> markers,
                          @Nullable XDebugSession session,
                          boolean rebuildOnSessionEvents) {
        super(project, false);
        myRebuildOnSessionEvents = rebuildOnSessionEvents;

        setTitle(XDebuggerBundle.message("inspect.value.dialog.title", name));
        setModal(false);

        Pair<XValue, String> initialItem = Pair.create(value, name);
        DebuggerTreeCreator creator = new DebuggerTreeCreator<Pair<XValue,String>>() {

            @Override
            public @NotNull String getTitle(@NotNull Pair<XValue,String> descriptor) {
                return "THIS IS THE TITLE";
            }

            @Override
            public void createDescriptorByNode(Object node, ResultConsumer<Pair<XValue,String>> resultConsumer) {
//                if (node instanceof XValueNodeImpl) {
//                    XValueNodeImpl valueNode = (XValueNodeImpl)node;
//                    resultConsumer.onSuccess(Pair.create(valueNode.getValueContainer(), valueNode.getName()));
//                }
                System.out.printf("createDescriptorByNode");
            }

            @Override
            public @NotNull Tree createTree(@NotNull Pair<XValue,String> descriptor) {
                final ColorfulTree tree = new ColorfulTree(project, editorsProvider, sourcePosition, XDebuggerActions.INSPECT_TREE_POPUP_GROUP, markers);
                final XValueNodeImpl root = new XValueNodeImpl(tree, null, descriptor.getSecond(), descriptor.getFirst());
                tree.setRoot(root, false);
                return tree;
            }
        };

        myDebuggerTreePanel = new DebuggerTreeWithHistoryPanel<>(initialItem, creator, project, myDisposable);

        init();
    }

    @Override
    @Nullable
    protected JComponent createCenterPanel() {
        return myDebuggerTreePanel.getMainPanel();
    }

    @Override
    @Nullable
    protected JComponent createSouthPanel() {
        return null;
    }

    @Override
    @NonNls
    protected String getDimensionServiceKey() {
        return "#xdebugger.XInspectDialog";
    }

    @NotNull
    public XDebuggerTree getTree() {
        return myDebuggerTreePanel.getTree();
    }

    @Nullable
    @Override
    public JComponent getPreferredFocusedComponent() {
        return myDebuggerTreePanel.getTree();
    }
}
