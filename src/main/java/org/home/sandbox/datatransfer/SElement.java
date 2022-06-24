// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.home.sandbox.datatransfer;


import com.intellij.xdebugger.frame.XNamedValue;
import org.home.sandbox.operation.OperationType;
import org.jetbrains.annotations.NotNull;

public abstract class SElement extends XNamedValue {

  public OperationType whatChanged = OperationType.NOTHING;

  protected SElement(@NotNull String name) {
    super(name);
  }
}
