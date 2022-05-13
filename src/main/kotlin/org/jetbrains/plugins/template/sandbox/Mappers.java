// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.template.sandbox;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.sun.jdi.Field;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.StringReference;
import com.sun.jdi.Value;
import org.jetbrains.plugins.template.sandbox.dto.SElement;
import org.jetbrains.plugins.template.sandbox.dto.SObject;
import org.jetbrains.plugins.template.sandbox.dto.SPair;
import org.jetbrains.plugins.template.sandbox.dto.SPrimitive;

import javax.swing.tree.TreeNode;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Mappers {

  private Mappers() { }

  //public static JsonElement toJsonElement(Value value, String name) {
  //  JsonObject object = new JsonObject();
  //  if (value instanceof StringReference) {
  //    return new JsonPrimitive(((StringReference)value).value());
  //  }
  //  else if (value instanceof ObjectReference) {
  //    List<Field> fileds = ((ObjectReference)value).referenceType().allFields();
  //    for (Field f : fileds) {
  //      JsonElement elem = toJsonElement(((ObjectReference)value).getValue(f), f.name());
  //      object.add(f.name(), elem);
  //    }
  //    return object;
  //  }
  //  else {
  //    return new JsonPrimitive(value.toString());
  //  }
  //}

  public static JsonNode toJsonElement(Value value, String name) {
    if (value instanceof StringReference) {
      return new TextNode(((StringReference)value).value());
    }
    else if (value instanceof ObjectReference) {
      List<Field> fileds = ((ObjectReference)value).referenceType().allFields();
      Map<String, JsonNode> kids = new HashMap<>();
      for (Field f : fileds) {
        JsonNode elem = toJsonElement(((ObjectReference)value).getValue(f), f.name());
        kids.put(f.name(), elem);
      }
      return new ObjectNode(JsonNodeFactory.instance, kids);
    }
    else {
      return new TextNode(value.toString());
    }
  }

  public static JsonNode toJsonElement(SElement value, String name) {
    if (value instanceof SPrimitive) {
      return new TextNode(((SPrimitive)value).value);
    }
    else if (value instanceof SObject) {
      Set<Map.Entry<String, SElement>> fileds = ((SObject)value).fields.entrySet();
      Map<String, JsonNode> kids = new HashMap<>();
      for (Map.Entry<String, SElement> entry : fileds) {
        JsonNode elem = toJsonElement(entry.getValue(), entry.getKey());
        kids.put(entry.getKey(), elem);
      }
      return new ObjectNode(JsonNodeFactory.instance, kids);
    }
    else {
      throw new IllegalStateException("Not implemented");
    }
  }

  //public static JsonElement toJsonElement(SElement value, String name) {
  //
  //  if (value instanceof SPrimitive) {
  //    return new JsonPrimitive(value.getName());
  //  }
  //  else if (value instanceof SObject) {
  //    JsonObject object = new JsonObject();
  //    Set<Map.Entry<String, SElement>> fileds = ((SObject)value).fields.entrySet();
  //    for (Map.Entry<String, SElement> entry : fileds) {
  //      JsonElement elem = toJsonElement(entry.getValue(), entry.getKey());
  //      object.add(entry.getKey(), elem);
  //    }
  //    return object;
  //  }
  //  else {
  //    throw new IllegalStateException("Not implemented");
  //  }
  //}

  public static SElement toElement(Value value, String name) {
    if (value instanceof StringReference) {
      return new SPrimitive(((StringReference)value).value(), value.type(), name);
    }
    else if (value instanceof ObjectReference) {
      SObject o = new SObject(value.type(), name);
      List<Field> fileds = ((ObjectReference)value).referenceType().allFields();
      for (Field f : fileds) {
        SElement elem = toElement(((ObjectReference)value).getValue(f), f.name());
        o.fields.put(f.name(), elem);
      }
      return o;
    }
    else {
      if (value == null) {
        // TODO: 21.04.2022 check null for non initialized
        throw new NullPointerException();
      }
      else {
        return new SPrimitive(value.toString(), value.type(), name);
      }
    }
  }

  public static void markNodesWithDiff(ArrayNode node, SElement element) {
    var i = node.elements();
    while (i.hasNext()) {
      markNodesWithObject((ObjectNode)i.next(), element);
    }
  }

  public static void markNodesWithObject(ObjectNode node, SElement element) {
    Operation operation = Operation.fromRfcName(((TextNode)node.get(Constants.OP)).textValue());
    List<String> path = parse(((TextNode)node.get(Constants.PATH)).textValue());

    try {
      SElement where = evaluate(path, element);
      where.whatChanged = operation;
    }
    catch (IllegalStateException e) {
      throw new IllegalStateException("Hello");
    }
  }

  public static void markNodesWithDiff(ArrayNode node, SElement saved, SElement current) {
    var i = node.elements();
    while (i.hasNext()) {
      mergeItems((ObjectNode)i.next(), saved, current);
    }
  }

  public static void mergeItems(ObjectNode node, SElement saved, SElement current) {
    // TODO: 06.05.2022 no objects only variables
    Operation operation = Operation.fromRfcName(((TextNode)node.get(Constants.OP)).textValue());
    List<String> path = parse(((TextNode)node.get(Constants.PATH)).textValue());
    List<String> pathToParent = path.subList(0, path.size() - 1);

    try {
      SPair parent = evaluate(pathToParent, saved, current);
      switch (operation) {
        case ADD:
          String addedVariableName = path.get(path.size() - 1);
          ((SObject)parent.fst()).fields.put(addedVariableName, ((SObject)parent.snd()).fields.get(addedVariableName));
          ((SObject)parent.fst()).fields.get(addedVariableName).whatChanged = operation;
          break;
        case REMOVE:
          String removedVariableName = path.get(path.size() - 1);
          ((SObject)parent.fst()).fields.get(removedVariableName).whatChanged = operation;
          break;
        case REPLACE:
          String replacedVariableName = path.get(path.size() - 1);
          ((SObject)parent.fst()).fields.put(replacedVariableName, ((SObject)parent.snd()).fields.get(replacedVariableName));
          ((SObject)parent.fst()).fields.get(replacedVariableName).whatChanged = operation;
          break;
        case MOVE:
          List<String> pathFrom = parse(((TextNode)node.get(Constants.FROM)).textValue());
          List<String> pathTo = parse(((TextNode)node.get(Constants.PATH)).textValue());
          String from = pathFrom.get(path.size() - 1);
          String to = pathTo.get(path.size() - 1);
          ((SObject)parent.fst()).fields.put(to, ((SObject)parent.snd()).fields.get(to));
          ((SObject)parent.fst()).fields.remove(from);
          ((SObject)parent.fst()).fields.get(to).whatChanged = operation;
          break;
        case COPY:
          throw new IllegalStateException("can't be");
        case TEST:
          break;
      }
    }
    catch (IllegalStateException e) {
      throw new IllegalStateException("Hello");
    }
  }

  public static List<String> parse(String path) throws IllegalArgumentException {
    StringBuilder reftoken = null;
    List<String> result = new ArrayList<String>();

    for (int i = 0; i < path.length(); ++i) {
      char c = path.charAt(i);

      // Require leading slash
      if (i == 0) {
        if (c != '/') throw new IllegalArgumentException("Missing leading slash");
        reftoken = new StringBuilder();
        continue;
      }

      switch (c) {
        // Escape sequences
        case '~':
          switch (path.charAt(++i)) {
            case '0':
              reftoken.append('~');
              break;
            case '1':
              reftoken.append('/');
              break;
            default:
              throw new IllegalArgumentException("Invalid escape sequence ~" + path.charAt(i) + " at index " + i);
          }
          break;

        // New reftoken
        case '/':
          result.add(reftoken.toString());
          reftoken.setLength(0);
          break;

        default:
          reftoken.append(c);
          break;
      }
    }

    if (reftoken == null) {
      throw new IllegalStateException("Not implemented");
    }

    result.add(reftoken.toString());
    return result;
  }

  public static SPair evaluate(List<String> tokens, SElement saved, SElement current) {
    SElement currentSaved = saved;
    SElement currentCurrent = current;

    for (int idx = 0; idx < tokens.size(); ++idx) {
      final String token = tokens.get(idx);

      //if (current.isArray()) {
      //  current = current.get(getArrayIndex(token));
      //}
      if (currentSaved instanceof SObject) {
        currentSaved = ((SObject)currentSaved).fields.get(token);
        currentCurrent = ((SObject)currentCurrent).fields.get(token);
      }
      else {
        throw new IllegalStateException("Not an array");
      }
    }

    return new SPair(currentSaved, currentCurrent);
  }

  public static SElement evaluate(List<String> tokens, SElement saved) {
    SElement currentSaved = saved;
    //SElement currentCurrent = current;

    for (int idx = 0; idx < tokens.size(); ++idx) {
      final String token = tokens.get(idx);

      //if (current.isArray()) {
      //  current = current.get(getArrayIndex(token));
      //}
      if (currentSaved instanceof SObject) {
        currentSaved = ((SObject)currentSaved).fields.get(token);
        //currentCurrent = ((SObject)currentCurrent).fields.get(token);
      }
      else {
        throw new IllegalStateException("Not an array");
      }
    }
    return currentSaved;
  }

  public int getArrayIndex(String decodedToken) {
    final Pattern VALID_ARRAY_IND = Pattern.compile("-|0|(?:[1-9][0-9]*)");
    Matcher matcher = VALID_ARRAY_IND.matcher(decodedToken);
    if (matcher.matches()) {
      return matcher.group().equals("-") ? Integer.MIN_VALUE : Integer.parseInt(matcher.group());
    }
    throw new IllegalStateException("Not an array");
  }
}
