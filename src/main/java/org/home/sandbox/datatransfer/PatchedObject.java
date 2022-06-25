package org.home.sandbox.datatransfer;

import com.intellij.xdebugger.frame.*;
import org.apache.commons.collections4.Trie;
import org.home.sandbox.operation.OperationType;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class PatchedObject extends XNamedValue {

    SElement oldElem;
    SElement newElem;
    Trie<String, OperationType> operations;
    String path;
    public OperationType operationType = OperationType.NOTHING;

    public PatchedObject(SElement oldElem, SElement newElem, Trie<String, OperationType> operations, String name, String path) {
        super(name);
        this.oldElem = oldElem;
        this.newElem = newElem;
        this.operations = operations;
        this.path = path;
    }

    @Override
    public void computePresentation(@NotNull XValueNode node, @NotNull XValuePlace place) {
        boolean noChildren = oldElem instanceof SPrimitive && newElem instanceof SPrimitive;
        node.setPresentation(null, "", "", !noChildren);
    }

    @Override
    public void computeChildren(@NotNull XCompositeNode xNode) {
        final XValueChildrenList children = new XValueChildrenList();
        List<XNamedValue> childrenList = new ArrayList<>();
        compute(childrenList);
        for (XNamedValue xNamedValue : childrenList) {
            children.add(xNamedValue);
        }
        xNode.addChildren(children, true);
    }

    public void compute(List<XNamedValue> children) {
        if (oldElem instanceof SObject && newElem instanceof SObject) {
            computeObjects(children);
        } else if (oldElem instanceof SArray && newElem instanceof SArray) {
            computeArrays(children);
        } else {
            throw new IllegalStateException("cant be");
        }
    }

    public void computeArrays(List<XNamedValue> children) {
        var iterOld = ((SArray) oldElem).fields;
        var iterNew = ((SArray) newElem).fields;

        var iter = operations.prefixMap(path).entrySet();

        for (Map.Entry<String, OperationType> entry : iter) {
            if (entry.getValue() == OperationType.ADD) {
                int index = Integer.parseInt(entry.getKey().substring(path.length()));
                iterNew.get(index).whatChanged = OperationType.ADD;
            }
        }

        children.addAll(iterNew);

        for (Map.Entry<String, OperationType> entry : iter) {
            if (entry.getValue() == OperationType.REMOVE) {
                int index = Integer.parseInt(entry.getKey().substring(path.length()));
                SElement elem = iterOld.get(index);
                elem.whatChanged = OperationType.REMOVE;
                children.add(index, elem);
            }
        }
    }

    public void computeObjects(List<XNamedValue> children) {
        var iterOld = ((SObject) oldElem).fields.entrySet().iterator();
        var iterNew = ((SObject) newElem).fields.entrySet().iterator();
        while (iterOld.hasNext() && iterNew.hasNext()) {
            var oldE = iterOld.next();
            var newE = iterNew.next();
            var oldKey = path + oldE.getKey();
            var newKey = path + newE.getKey();

            if (operations.containsKey(oldKey)) {
                SElement value = oldE.getValue();
                if (operations.get(oldKey).equals(OperationType.REPLACE)) {
                    value.whatChanged = OperationType.REMOVE;
                } else {
                    value.whatChanged = operations.get(oldKey);
                }
                children.add(value);
            }

            if (operations.containsKey(newKey)) {
                SElement value = newE.getValue();
                if (operations.get(newKey).equals(OperationType.REPLACE)) {
                    value.whatChanged = OperationType.ADD;
                } else {
                    value.whatChanged = operations.get(oldKey);
                }
                children.add(value);
            } else {
                if (oldE.getValue() instanceof SPrimitive && newE.getValue() instanceof SPrimitive) {
                    children.add(newE.getValue());
                } else {
                    SElement oldEValue = ((SObject) oldElem).fields.get(newE.getKey());
                    SElement newEValue = newE.getValue();
                    PatchedObject object = new PatchedObject(oldEValue, newEValue, operations, newE.getKey(), newKey + "/");

                    if (!oldKey.equals("root/")) {
                        if (!operations.prefixMap(oldKey).isEmpty()) {
                            object.operationType = OperationType.REMOVE;
                        }
                    }

                    if (!newKey.equals("root/")) {
                        if (!operations.prefixMap(newKey).isEmpty()) {
                            object.operationType = OperationType.ADD;
                        }
                    }
                    children.add(object);
                }
            }
        }

        while (iterOld.hasNext()) {
            var oldE = iterOld.next();
            var oldKey = path + oldE.getKey();
            if (operations.containsKey(oldKey)) {
                SElement value = oldE.getValue();
                value.whatChanged = operations.get(oldKey);
                children.add(value);
            } else {
                System.out.println("WARNING" + oldKey);
//                throw new IllegalStateException("cannot be?");
            }
        }

        while (iterNew.hasNext()) {
            var newE = iterNew.next();
            var newKey = path + newE.getKey();
            if (operations.containsKey(newKey)) {
                SElement value = newE.getValue();
                value.whatChanged = operations.get(newKey);
                children.add(value);
            } else {

                SElement oldEValue = ((SObject) oldElem).fields.get(newE.getKey());
                SElement newEValue = newE.getValue();
                PatchedObject object = new PatchedObject(oldEValue, newEValue, operations, newE.getKey(), newKey + "/");
                children.add(object);
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PatchedObject object = (PatchedObject) o;
        return Objects.equals(path, object.path) && operationType == object.operationType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, operationType);
    }
}