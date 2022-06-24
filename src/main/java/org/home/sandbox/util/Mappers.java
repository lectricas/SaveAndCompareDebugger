// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.home.sandbox.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.*;
import com.sun.jdi.*;
import org.home.sandbox.datatransfer.*;

import java.util.*;

public class Mappers {

    private Mappers() {
    }

    public static JsonNode fromValueToJson(Value value, Set<Integer> visited) {
        if (value instanceof StringReference) {
            return new TextNode(((StringReference) value).value());
        } else if (value instanceof ArrayReference) {
            int hashcode = System.identityHashCode(value);
            if (visited.contains(hashcode)) {
                return new IntNode(hashcode);
            } else {
                visited.add(hashcode);
                int length = ((ArrayReference) (value)).length();
                List<JsonNode> kids = new ArrayList<>();
                for (int i = 0; i < length; i++) {
                    Value v = ((ArrayReference) (value)).getValue(i);
                    JsonNode elem = fromValueToJson(v, visited);
                    kids.add(elem);
                }
                return new ArrayNode(JsonNodeFactory.instance, kids);
            }
        } else if (value instanceof ObjectReference) {
            int hashcode = System.identityHashCode(value);
            if (visited.contains(hashcode)) {
                return new IntNode(hashcode);
            } else {
                visited.add(hashcode);
                Map<String, JsonNode> kids = new HashMap<>();
                List<Field> fileds = ((ObjectReference) value).referenceType().allFields();
                for (Field f : fileds) {
                    Value v = ((ObjectReference) value).getValue(f);
                    JsonNode elem = fromValueToJson(v, visited);
                    kids.put(f.name(), elem);
                }
                return new ObjectNode(JsonNodeFactory.instance, kids);
            }
        } else {
            if (value == null) {
                return NullNode.getInstance();
            } else {
                return new TextNode(value.toString());
            }
        }
    }

    public static JsonNode fromElemToJson(SElement element, Set<Integer> visited) {
        if (element instanceof SPrimitive) {
            return new TextNode(((SPrimitive) element).value);
        } else if (element instanceof SArray) {
            int hashcode = System.identityHashCode(element);
            if (visited.contains(hashcode)) {
                return new IntNode(hashcode);
            } else {
                visited.add(hashcode);
                List<JsonNode> kids = new ArrayList<>();
                for (SElement field : ((SArray) element).fields) {
                    JsonNode elem = fromElemToJson(field, visited);
                    kids.add(elem);
                }
                return new ArrayNode(JsonNodeFactory.instance, kids);
            }
        } else if (element instanceof SObject) {
            int hashcode = System.identityHashCode(element);
            if (visited.contains(hashcode)) {
                return new IntNode(hashcode);
            } else {
                visited.add(hashcode);
                Map<String, JsonNode> kids = new HashMap<>();
                for (Map.Entry<String, SElement> entry : ((SObject) element).fields.entrySet()) {
                    JsonNode elem = fromElemToJson(entry.getValue(), visited);
                    kids.put(entry.getKey(), elem);
                }
                return new ObjectNode(JsonNodeFactory.instance, kids);
            }
        } else {
            throw new IllegalStateException("not implemented");
        }
    }

    public static SElement fromValueToElem(Value value, String name, Map<String, SElement> visited) {
        if (value instanceof StringReference) {
            return new SPrimitive(((StringReference) value).value(), value.type(), name);
        } else if (value instanceof ArrayReference) {
            String hashcode = Integer.toHexString(System.identityHashCode(value));
            if (visited.containsKey(hashcode)) {
                return visited.get(hashcode);
            } else {
                SArray array = new SArray(name);
                int length = ((ArrayReference) (value)).length();
                List<SElement> kids = new ArrayList<>();
                for (int i = 0; i < length; i++) {
                    Value v = ((ArrayReference) (value)).getValue(i);
                    SElement elem = fromValueToElem(v, String.valueOf(i), visited);
                    kids.add(elem);
                }
                array.fields = kids;
                return array;
            }
        } else if (value instanceof ObjectReference) {
            String hashcode = Integer.toHexString(System.identityHashCode(value));
            if (visited.containsKey(hashcode)) {
                return visited.get(hashcode);
            } else {
                SObject o = new SObject(value.type(), name);
                visited.put(hashcode, o);
                List<Field> fileds = ((ObjectReference) value).referenceType().allFields();
                for (Field f : fileds) {
                    Value v = ((ObjectReference) value).getValue(f);
                    SElement elem = fromValueToElem(v, f.name(), visited);
                    o.fields.put(f.name(), elem);
                }
                return o;
            }
        } else {
            if (value == null) {
                return new SPrimitive("null", "null");
            } else {
                return new SPrimitive(value.toString(), value.type(), name);
            }
        }
    }
}
