package CatanSimulatorDomainModel.catanUML.util;

import CatanSimulatorDomainModel.catanUML.model.*;
/**
 * Validates game rules and ensures invariants are maintained.
 * 
 * R1.6: Enforces key game invariants including:
 * - Roads must be connected to existing roads or settlements
 * - Cities must replace existing settlements
 * - Distance between settlements must be at least 2 vertices
 * 
 * Used by GameMaster before executing any moves.
 */
public class RuleValidator {
    private Board board;

    /**
     * Constructs a RuleValidator for the given board.
     * 
     * @param board The game board to validate against
     */
    public RuleValidator(Board board) {
        this.board = board;
    }

    /**
     * Checks if a player can build a road between two vertices.
     * 
     * R1.6: Roads must be connected to existing roads or settlements.
     * 
     * Validation rules:
     * 1. Vertices must be adjacent
     * 2. Road must not already exist
     * 3. Must connect to player's existing structure (road or building)
     * 4. Exception: First road doesn't need connection
     * 
     * @param player The player attempting to build
     * @param start The starting vertex
     * @param end The ending vertex
     * @return true if the road can be built
     */
    public boolean canBuildRoad(Player player, Vertex start, Vertex end) {
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

        // First road doesn't need to be connected
        if (player.getRoadsBuilt().isEmpty() && player.getBuildingsBuilt().isEmpty()) {
            connected = true;
        }

        return connected;
    }

    /**
     * Checks if a player can build a settlement at a vertex.
     * 
     * R1.6: Must respect distance rule (2 vertices away from other settlements).
     * 
     * Validation rules:
     * 1. Vertex must be unoccupied
     * 2. Adjacent vertices must be empty (distance rule)
     * 3. Must connect to player's road (unless first settlement)
     * 
     * @param player The player attempting to build
     * @param location The vertex where the settlement would be placed
     * @return true if the settlement can be built
     */
    public boolean canBuildSettlement(Player player, Vertex location) {
        // Check if vertex can accommodate a building
        if (!location.canBuild(player)) {
            return false;
        }

        // Check distance rule (implemented in Vertex.canBuild())
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
     * 
     * R1.6: Must replace an existing settlement.
     * 
     * Validation rules:
     * 1. A settlement must exist at this location
     * 2. The settlement must belong to the player
     * 
     * @param player The player attempting to build
     * @param location The vertex where the city would be placed
     * @return true if the city can be built
     */
    public boolean canBuildCity(Player player, Vertex location) {
        // Must have a settlement at this location
        Building building = location.getBuilding();
        if (building == null || !(building instanceof Settlement)) {
            return false;
        }

        // Settlement must belong to the player
        if (building.getOwner() != player) {
            return false;
        }

        return true;
    }

    /**
     * Checks if the distance rule is respected at a location.
     * 
     * R1.6: Adjacent vertices must be empty.
     * 
     * @param location The vertex to check
     * @return true if distance rule is respected
     */
    public boolean respectsDistanceRule(Vertex location) {
        for (Vertex adjacent : location.getAdjacentVertices()) {
            if (adjacent.isOccupied()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Sets the board for this validator.
     * Used if the board changes during the game.
     * 
     * @param board The new board to validate against
     */
    public void setBoard(Board board) {
        this.board = board;
    }
}