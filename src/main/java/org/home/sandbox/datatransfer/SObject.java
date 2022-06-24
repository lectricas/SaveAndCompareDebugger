// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.home.sandbox.datatransfer;

import com.intellij.xdebugger.frame.XCompositeNode;
import com.intellij.xdebugger.frame.XValueChildrenList;
import com.intellij.xdebugger.frame.XValueNode;
import com.intellij.xdebugger.frame.XValuePlace;
import com.sun.jdi.Type;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class SObject extends SElement {
  public Map<String, SElement> fields = new LinkedHashMap<>();
  String name;

  public SObject(Type type, String name) {
    super(name);
    this.name = name; // todo for future use
  }

  public SObject(String name, Map<String, SElement> fields) {
    super(name);
    this.name = name;
    this.fields = fields;
  }

  public SObject(@NotNull String name) {
    super(name);
    this.name = name;
  }

  @Override
  public String toString() {
    return fields.entrySet().stream()
             .map(entry -> entry.getKey() + "=" + entry.getValue())
             .collect(Collectors.joining(",", "{", "}")) + ":";
  }

  @Override
  public void computePresentation(@NotNull XValueNode node, @NotNull XValuePlace place) {
    node.setPresentation(null, "", "", true);
  }

  @Override
  public void computeChildren(@NotNull XCompositeNode node) {
    final XValueChildrenList children = new XValueChildrenList();
    for (final SElement value : fields.values()) {
      children.add(value);
    }

    node.addChildren(children, true);
  }
}
