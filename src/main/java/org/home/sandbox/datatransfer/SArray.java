package org.home.sandbox.datatransfer;

import com.intellij.xdebugger.frame.*;
import com.sun.jdi.Type;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SArray extends SElement{
    public List<SElement> fields = new ArrayList<>();
    String name;

    public SArray(Type type, String name) {
        super(name);
        this.name = name; // todo for future use
    }

    public SArray(String name, List<SElement> fields) {
        super(name);
        this.name = name;
        this.fields = fields;
    }

    public SArray(@NotNull String name) {
        super(name);
        this.name = name;
    }

    @Override
    public String toString() {
        return fields.stream()
                .map(XNamedValue::toString)
                .collect(Collectors.joining(",", "{", "}")) + ":";
    }

    @Override
    public void computePresentation(@NotNull XValueNode node, @NotNull XValuePlace place) {
        node.setPresentation(null, "", "", true);
    }

    @Override
    public void computeChildren(@NotNull XCompositeNode node) {
        final XValueChildrenList children = new XValueChildrenList();
        for (final SElement value : fields) {
            children.add(value);
        }

        node.addChildren(children, true);
    }
}
