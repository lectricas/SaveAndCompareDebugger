// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.template.sandbox.dto;

import com.intellij.xdebugger.frame.XValueNode;
import com.intellij.xdebugger.frame.XValuePlace;
import com.intellij.xdebugger.frame.presentation.XRegularValuePresentation;
import com.sun.jdi.Type;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.template.sandbox.dto.SElement;

import static com.intellij.openapi.editor.colors.CodeInsightColors.ERRORS_ATTRIBUTES;

public class SPrimitive extends SElement {
  public String value;
  Type type;
  String name;

//  operation : repalce /object1/object2/primitive1 {before  : 666, after : 333}

  public SPrimitive(String value, Type type, String name) {
    super(name);
    this.value = value;
    this.type = type;
    this.name = name;
  }

  @Override
  public String toString() {
    return value + ":" + type.name();
  }

  @Override
  public void computePresentation(@NotNull XValueNode node, @NotNull XValuePlace place) {
    var presentation = new XRegularValuePresentation(value, type.name(), ",") {
      @Override
      public void renderValue(@NotNull XValueTextRenderer renderer) {
        switch (whatChanged) {
          case TEST:
            renderer.renderValue(value);
            break;
          default:
            renderer.renderValue(value, ERRORS_ATTRIBUTES);
        }
      }
    };
    node.setPresentation(null, presentation, false);
  }

  @Override
  Type getType() {
    return type;
  }
}
