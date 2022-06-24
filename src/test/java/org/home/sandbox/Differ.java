package org.home.sandbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.flipkart.zjsonpatch.*;

import java.util.List;

public class Differ {
    public static void main(String[] args) throws JsonProcessingException {
        ObjectMapper jackson = new ObjectMapper();
        JsonNode beforeNode = jackson.readTree("{\"menu\": {\n" +
                "\"popup\": {\n" +
                "  \"id\": \"file\",\n" +
                "    \"menuitem\": [\n" +
                "      {\"one\": \"New\"},\n" +
                "      {\"two\": \"Open\"},\n" +
                "      {\"three\": \"Close\"}\n" +
                "    ]\n" +
                "  },\n" +
                "  \"value22\": \"File\"\n" +
                "  \n" +
                "}}");
        JsonNode afterNode = jackson.readTree("{\"menu\": {\n" +
                "\"popup\": {\n" +
                "  \"id\": \"file\",\n" +
                "  \"id2\": \"file2\",\n" +
                "    \"menuitem\": [\n" +
                "      {\"one\": \"New\"},\n" +
                "      {\"two\": \"Open\"},\n" +
                "      {\"three\": \"Close\"}\n" +
                "    ]\n" +
                "  },\n" +
                "  \"value22\": \"File\"\n" +
                "  \n" +
                "}}");

        ArrayNode patchNode = (ArrayNode) JsonDiff.asJson(beforeNode, afterNode);

        String diff = patchNode.toString();
        JsonPatch.applyInPlace(patchNode, beforeNode);
        System.out.println(diff);
    }
}
