// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.template.sandbox.dto;

public class SPair {

  private SElement fst;
  private SElement snd;

  public SPair(SElement fst, SElement snd) {
    this.fst = fst;
    this.snd = snd;
  }

  public SElement fst() {
    return fst;
  }

  public SElement snd() {
    return snd;
  }
}
