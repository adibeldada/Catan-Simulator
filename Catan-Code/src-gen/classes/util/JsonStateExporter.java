package classes.util;

import classes.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;

public class JsonStateExporter {

    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Maps Java vertex IDs (0–53) to catanatron node IDs (0–53).
     * Derived by direct visual comparison of both board numbering systems.
     *
     * Left side = Java vertex ID, Right side = catanatron node ID:
     *  5->0, 0->1, 1->2, 2->3, 3->4, 4->5,
     *  6->6, 7->7, 8->8, 9->9, 10->10, 11->11, 12->12,
     *  15->13, 13->14, 14->15,
     *  18->16, 16->17, 17->18,
     *  21->19, 19->20, 20->21,
     *  22->22 ... 34->34,
     *  35->36, 36->37, 37->35,
     *  38->39, 39->38,
     *  40->41, 41->42, 42->40,
     *  43->44, 44->43,
     *  45->45, 46->47, 47->46,
     *  48->48 ... 53->53
     */
    private static final int[] JAVA_TO_CAT = {
        //  0   1   2   3   4   5   6   7   8   9
            1,  2,  3,  4,  5,  0,  6,  7,  8,  9,
        // 10  11  12  13  14  15  16  17  18  19
           10, 11, 12, 14, 15, 13, 17, 18, 16, 20,
        // 20  21  22  23  24  25  26  27  28  29
           21, 19, 22, 23, 24, 25, 26, 27, 28, 29,
        // 30  31  32  33  34  35  36  37  38  39
           30, 31, 32, 33, 34, 36, 37, 35, 39, 38,
        // 40  41  42  43  44  45  46  47  48  49
           41, 42, 40, 44, 43, 45, 47, 46, 48, 49,
        // 50  51  52  53
           50, 51, 52, 53
    };

    private static int toCatNode(int javaVertexId) {
        if (javaVertexId < 0 || javaVertexId >= JAVA_TO_CAT.length) {
            throw new IllegalArgumentException("Invalid java vertex ID: " + javaVertexId);
        }
        return JAVA_TO_CAT[javaVertexId];
    }

    public static void exportState(Board board, String filePath) {
        ObjectNode root = mapper.createObjectNode();
        ArrayNode buildingsNode = root.putArray("buildings");
        ArrayNode roadsNode = root.putArray("roads");
        
        root.put("robber", board.getRobber().getCurrentTile().getId());

        // Buildings first so the visualizer processes them before roads
        for (Vertex v : board.getVertices()) {
            if (v.isOccupied()) {
                ObjectNode bNode = mapper.createObjectNode();
                bNode.put("node", toCatNode(v.getId()));
                bNode.put("owner", getPlayerColor(v.getOwner().getId()));
                String type = (v.getBuilding() instanceof City) ? "CITY" : "SETTLEMENT";
                bNode.put("type", type);
                buildingsNode.add(bNode);
            }
        }

        // Roads second
        for (Road r : board.getRoads()) {
            ObjectNode rNode = mapper.createObjectNode();
            rNode.put("a", toCatNode(r.getStart().getId()));
            rNode.put("b", toCatNode(r.getEnd().getId()));
            rNode.put("owner", getPlayerColor(r.getOwner().getId()));
            roadsNode.add(rNode);
        }

        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(filePath), root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getPlayerColor(int playerId) {
        return switch (playerId) {
            case 1 -> "RED";
            case 2 -> "BLUE";
            case 3 -> "WHITE";
            case 4 -> "ORANGE";
            default -> "GRAY";
        };
    }
}