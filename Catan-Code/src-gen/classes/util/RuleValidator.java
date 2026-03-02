package classes.util;

import classes.model.*;

/**
 * Validates game rules and ensures invariants are maintained.
 * R1.6: Enforces key game invariants including:
 * - Roads must be connected to existing roads or settlements
 * - Cities must replace existing settlements
 * - Distance between settlements must be at least 2 vertices
 * - Players must have sufficient resources for actions
 * Used by GameMaster and Players before executing any moves.
 */
public class RuleValidator {

    private Board board;

    /**
     * Constructs a RuleValidator for the given board.
     * @param board The game board to validate against
     */
    public RuleValidator(Board board) {
        this.board = board;
    }

    /**
     * Checks if a player can build a road between two vertices.
     * R1.6: Roads must be connected to existing roads or settlements.
     * Added resource check: 1 Wood, 1 Brick.
     */
    public boolean canBuildRoad(Player player, Vertex start, Vertex end) {
        // NEW: Check if player has resources
        if (!player.canAfford(Cost.roadCost())) {
            return false;
        }

        // Check if vertices are adjacent
        if (!start.getAdjacentVertices().contains(end)) {
            return false;
        }

        // Check if road already exists
        for (Road road : board.getRoads()) {
            if (road.connects(start, end)) {
                return false;
            }
        }

        // Check if connected to player's existing structure
        boolean connected = false;

        // Connected if either vertex is owned by player
        if (start.getOwner() == player || end.getOwner() == player) {
            connected = true;
        }

        // Connected if player has a road touching either vertex
        if (!connected) {
            for (Road road : player.getRoadsBuilt()) {
                if (road.getStart() == start || road.getEnd() == start ||
                    road.getStart() == end || road.getEnd() == end) {
                    connected = true;
                    break;
                }
            }
        }

        // First road doesn't need to be connected (Setup Phase)
        if (player.getRoadsBuilt().isEmpty() && player.getBuildingsBuilt().isEmpty()) {
            connected = true;
        }

        return connected;
    }

    /**
     * Checks if a player can build a settlement at a vertex.
     * Added resource check: 1 Wood, 1 Brick, 1 Wheat, 1 Sheep.
     */
    public boolean canBuildSettlement(Player player, Vertex location) {
        // NEW: Check if player has resources
        if (!player.canAfford(Cost.settlementCost())) {
            return false;
        }

        // Check if vertex can accommodate a building
        if (!location.canBuild(player)) {
            return false;
        }

        // Check distance rule
        if (!respectsDistanceRule(location)) {
            return false;
        }

        // Check if connected to player's road (unless first settlement)
        if (!player.getRoadsBuilt().isEmpty() || !player.getBuildingsBuilt().isEmpty()) {
            boolean connected = false;
            for (Road road : player.getRoadsBuilt()) {
                if (road.getStart() == location || road.getEnd() == location) {
                    connected = true;
                    break;
                }
            }
            if (!connected) {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks if a player can build a city at a vertex.
     * R1.6: Must replace an existing settlement.
     * Added resource check: 2 Wheat, 3 Ore.
     */
    public boolean canBuildCity(Player player, Vertex location) {
        // NEW: Check if player has resources
        if (!player.canAfford(Cost.cityCost())) {
            return false;
        }

        if (location == null) {
            return false;
        }

        Buildings building = location.getBuilding(); //
        
        // Check if location has a settlement
        if (!(building instanceof Settlement)) {
            return false;
        }

        // Settlement must belong to the player
        return building.getOwner() == player;
    }

    /**
     * Checks if the distance rule is respected at a location.
     */
    public boolean respectsDistanceRule(Vertex location) {
        for (Vertex adjacent : location.getAdjacentVertices()) { //
            if (adjacent.isOccupied()) { //
                return false;
            }
        }
        return true;
    }

    /**
     * Sets the board for this validator.
     */
    public void setBoard(Board board) {
        this.board = board;
    }
}