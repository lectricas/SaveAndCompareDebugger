// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.home.sandbox.operation;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public enum OperationType {
    ADD("add"),
    REMOVE("remove"),
    REPLACE("replace"),
    MOVE("move"),
    MOVE_OLD("move_old"),
    COPY("copy"),
    NOTHING("nothing");

    private final static Map<String, OperationType> OPS = createImmutableMap();

    private static Map<String, OperationType> createImmutableMap() {
        Map<String, OperationType> map = new HashMap<String, OperationType>();
        map.put(ADD.rfcName, ADD);
        map.put(REMOVE.rfcName, REMOVE);
        map.put(REPLACE.rfcName, REPLACE);
        map.put(MOVE.rfcName, MOVE);
        map.put(COPY.rfcName, COPY);
        map.put(NOTHING.rfcName, NOTHING);
        return Collections.unmodifiableMap(map);
    }

    private String rfcName;

    public boolean isPrevious = false;

    OperationType(String rfcName) {
        this.rfcName = rfcName;
    }

    public static OperationType fromRfcName(String rfcName) throws IllegalArgumentException {
        if (rfcName == null) throw new IllegalArgumentException("rfcName cannot be null");
        OperationType op = OPS.get(rfcName.toLowerCase());
        if (op == null) throw new IllegalArgumentException("unknown / unsupported operation " + rfcName);
        return op;
    }

    public String rfcName() {
        return this.rfcName;
    }


}