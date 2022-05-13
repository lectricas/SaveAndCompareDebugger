// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.template.sandbox.dto;


import com.intellij.xdebugger.frame.XNamedValue;
import com.sun.jdi.Type;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.template.sandbox.Operation;

public abstract class SElement extends XNamedValue {

  public Operation whatChanged = Operation.TEST;

  protected SElement(@NotNull String name) {
    super(name);
  }

  abstract Type getType();
}
