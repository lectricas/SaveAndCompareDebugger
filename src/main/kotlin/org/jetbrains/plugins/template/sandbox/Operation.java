// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.template.sandbox;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * User: gopi.vishwakarma
 * Date: 30/07/14
 */
public enum Operation {
    ADD("add"),
    REMOVE("remove"),
    REPLACE("replace"),
    MOVE("move"),
    COPY("copy"),
    TEST("test");

    private final static Map<String, Operation> OPS = createImmutableMap();

    private static Map<String, Operation> createImmutableMap() {
        Map<String, Operation> map = new HashMap<String, Operation>();
        map.put(ADD.rfcName, ADD);
        map.put(REMOVE.rfcName, REMOVE);
        map.put(REPLACE.rfcName, REPLACE);
        map.put(MOVE.rfcName, MOVE);
        map.put(COPY.rfcName, COPY);
        map.put(TEST.rfcName, TEST);
        return Collections.unmodifiableMap(map);
    }

    private String rfcName;

    public String previousName;

    Operation(String rfcName) {
        this.rfcName = rfcName;
    }

    public static Operation fromRfcName(String rfcName) throws IllegalArgumentException {
        if (rfcName == null) throw new IllegalArgumentException("rfcName cannot be null");
        Operation op = OPS.get(rfcName.toLowerCase());
        if (op == null) throw new IllegalArgumentException("unknown / unsupported operation " + rfcName);
        return op;
    }

    public String rfcName() {
        return this.rfcName;
    }


}