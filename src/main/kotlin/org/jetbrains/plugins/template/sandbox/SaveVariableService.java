// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.template.sandbox;


import com.intellij.openapi.components.Service;
import org.jetbrains.plugins.template.sandbox.dto.SElement;

import java.util.HashMap;
import java.util.Map;

@Service
public final class SaveVariableService {

  private SElement currentVariable = null;

  public void saveVariable(SElement element) {
    currentVariable = element;
  }

  public SElement getVariable() {
    return currentVariable;
  }
}