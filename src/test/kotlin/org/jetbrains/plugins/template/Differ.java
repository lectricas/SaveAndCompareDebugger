import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.flipkart.zjsonpatch.*;

public class Differ {
    public static void main(String[] args) throws JsonProcessingException {
        ObjectMapper jackson = new ObjectMapper();
        JsonNode beforeNode = jackson.readTree("{\"menu\": {\n" +
                "\"popup\": {\n" +
                "    \"menuitem\": [\n" +
                "      {\"one\": \"New\"},\n" +
                "      {\"two\": \"Open\"},\n" +
                "      {\"three\": \"Close\"}\n" +
                "    ]\n" +
                "  },\n" +
                "  \"id\": \"file\",\n" +
                "  \"value22\": \"File\"\n" +
                "  \n" +
                "}}");
        JsonNode afterNode = jackson.readTree("{\"menu\": {\n" +
                "  \"id\": \"file\",\n" +
                "  \"value22\": \"File\",\n" +
                "  \"popup\": {\n" +
                "    \"menuitem\": [\n" +
                "      {\"one\": \"1New\"},\n" +
                "      {\"two\": \"Open\"},\n" +
                "      {\"three\": \"Close\"}\n" +
                "    ]\n" +
                "  }\n" +
                "}}");
        JsonNode patchNode = JsonDiff.asJson(beforeNode, afterNode);
        String diff = patchNode.toString();
        JsonPatch.applyInPlace(patchNode, beforeNode);
        System.out.println(diff);

//        JsonPointer path = JsonPointer.parse(getPatchAttr(jsonNode, Constants.PATH).textValue());
    }

//    public JsonNode evaluate(final JsonNode document) throws JsonPointerEvaluationException {
//        JsonNode current = document;
//
//        for (int idx = 0; idx < tokens.length; ++idx) {
//            final JsonPointer.RefToken token = tokens[idx];
//
//            if (current.isArray()) {
//                if (!token.isArrayIndex())
//                    error(idx, "Can't reference field \"" + token.getField() + "\" on array", document);
//                if (token.getIndex() == LAST_INDEX || token.getIndex() >= current.size())
//                    error(idx, "Array index " + token.toString() + " is out of bounds", document);
//                current = current.get(token.getIndex());
//            }
//            else if (current.isObject()) {
//                if (!current.has(token.getField()))
//                    error(idx,"Missing field \"" + token.getField() + "\"", document);
//                current = current.get(token.getField());
//            }
//            else
//                error(idx, "Can't reference past scalar value", document);
//        }
//
//        return current;
//    }
}
