package classes.util;

import classes.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for exporting the game state to JSON.
 */
public class JsonStateExporter {

    // Added Logger to comply with SonarQube java:S4507 and match project logging standards
    private static final Logger LOGGER = Logger.getLogger(JsonStateExporter.class.getName());
    private static final ObjectMapper mapper = new ObjectMapper();

    // S1118: Private constructor to prevent instantiation
    private JsonStateExporter() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Maps Java vertex IDs (0–53) to catanatron node IDs (0–53).
     */
    private static final int[] JAVA_TO_CAT = {
            1,  2,  3,  4,  5,  0,  6,  7,  8,  9,
           10, 11, 12, 14, 15, 13, 17, 18, 16, 20,
           21, 19, 22, 23, 24, 25, 26, 27, 28, 29,
           30, 31, 32, 33, 34, 36, 37, 35, 39, 38,
           41, 42, 40, 44, 43, 45, 47, 46, 48, 49,
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

        // Buildings first
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
            // FIXED: Replaced e.printStackTrace() with LOGGER.log to avoid leaking stack trace data
            LOGGER.log(Level.SEVERE, "Failed to export game state to {0}", filePath);
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