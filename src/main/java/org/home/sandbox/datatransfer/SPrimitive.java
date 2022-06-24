// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.home.sandbox.datatransfer;

import com.intellij.xdebugger.frame.XValueNode;
import com.intellij.xdebugger.frame.XValuePlace;
import com.intellij.xdebugger.frame.presentation.XKeywordValuePresentation;
import com.sun.jdi.Type;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class SPrimitive extends SElement {
    public String value;
    Type type;
    String name;

    public SPrimitive(String value, Type type, String name) {
        super(name);
        this.value = value;
        this.type = type;
        this.name = name;
    }

    public SPrimitive(String name, String value) {
        super(name);
        this.value = value;
        this.name = name;
    }

    @Override
    public void computePresentation(@NotNull XValueNode node, @NotNull XValuePlace place) {
        var presentation = new XKeywordValuePresentation(value) {
            @Override
            public void renderValue(@NotNull XValueTextRenderer renderer) {
                renderer.renderValue(value);
            }
        };
        node.setPresentation(null, presentation, false);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SPrimitive that = (SPrimitive) o;
        return Objects.equals(value, that.value) && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, name);
    }
}
