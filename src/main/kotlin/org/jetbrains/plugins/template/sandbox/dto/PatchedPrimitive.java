package org.jetbrains.plugins.template.sandbox.dto;

import com.google.gson.JsonObject;
import com.intellij.xdebugger.frame.XCompositeNode;
import com.intellij.xdebugger.frame.XValue;
import com.intellij.xdebugger.frame.XValueNode;
import com.intellij.xdebugger.frame.XValuePlace;
import org.jetbrains.annotations.NotNull;

public class PatchedPrimitive extends XValue {

    public PatchedPrimitive(SPrimitive oldElem, SPrimitive newElem, JsonObject patch) {

    }

    @Override
    public void computePresentation(@NotNull XValueNode node, @NotNull XValuePlace place) {

    }

    @Override
    public void computeChildren(@NotNull XCompositeNode node) {

        super.computeChildren(node);
    }
}
