package org.home.sandbox.util;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.apache.commons.collections4.Trie;
import org.apache.commons.collections4.trie.PatriciaTrie;
import org.apache.commons.io.FileUtils;
import org.home.sandbox.operation.OperationType;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class PathUtil {
    public static Trie<String, OperationType> makeTree(ArrayNode arrayNode, String root) {
        Trie<String, OperationType> trie = new PatriciaTrie<>();

        var i = arrayNode.elements();
        while (i.hasNext()) {
            makePath((ObjectNode) i.next(), trie,root);
        }
        return trie;
    }

    public static void makePath(ObjectNode node, Trie<String, OperationType> root, String rootname) {
        OperationType operationType = OperationType.fromRfcName(((TextNode) node.get(Constants.OP)).textValue());
        String path = rootname + ((TextNode) node.get(Constants.PATH)).textValue();
        switch (operationType) {
            case ADD: // add new filed
                root.put(path, OperationType.ADD);
                break;
            case REMOVE: // remove field
                root.put(path, OperationType.REMOVE);
                break;
            case REPLACE: {// change value
                root.put(path, OperationType.ADD);
                break;
            }
            case MOVE: {// new variableName(or path) with same value
                root.put(path, OperationType.ADD);
                String pathFrom = ((TextNode) node.get(Constants.FROM)).textValue();
                root.put(pathFrom, OperationType.REMOVE);
                break;
            }
            case COPY: { //added same value as before, but new variableName
                List<String> pathFrom2 = parse(((TextNode) node.get(Constants.FROM)).textValue());
                root.put(path, OperationType.ADD);
                throw new IllegalStateException("Not implemented");
            }

            case NOTHING:
                break;
        }
    }

    public static String readFile(String filename) throws IOException {
        File projectDir = new File(".");
        var projectDirPath = Paths.get(projectDir.getAbsolutePath());
        File fileToAdd = projectDirPath.resolve(filename).toFile();
        return FileUtils.readFileToString(fileToAdd, Charset.defaultCharset());
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
}
