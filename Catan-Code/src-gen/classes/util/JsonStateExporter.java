package classes.util;

import classes.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;

public class JsonStateExporter {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static void exportState(Board board, String filePath) {
        ObjectNode root = mapper.createObjectNode();
        ArrayNode roadsNode = root.putArray("roads");
        ArrayNode buildingsNode = root.putArray("buildings");

        // Export Roads with "a", "b", and "owner" keys
        for (Road r : board.getRoads()) {
            ObjectNode rNode = mapper.createObjectNode();
            rNode.put("a", r.getStart().getId());
            rNode.put("b", r.getEnd().getId());
            rNode.put("owner", getPlayerColor(r.getOwner().getId()));
            roadsNode.add(rNode);
        }

        // Export Buildings into a single list with a "type" field
        for (Vertex v : board.getVertices()) {
            if (v.isOccupied()) {
                ObjectNode bNode = mapper.createObjectNode();
                bNode.put("node", v.getId());
                bNode.put("owner", getPlayerColor(v.getOwner().getId()));
                String type = (v.getBuilding() instanceof City) ? "CITY" : "SETTLEMENT";
                bNode.put("type", type);
                buildingsNode.add(bNode);
            }
        }

        try {
            // Overwrites state.json so the visualizer sees the update
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