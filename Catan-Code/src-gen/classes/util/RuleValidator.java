package classes.util;

import classes.model.*;

/**
 * Validates game rules and ensures invariants are maintained.
 * R1.6: Enforces key game invariants including resource costs and connectivity.
 */
public class RuleValidator {

    private Board board;

    public RuleValidator(Board board) {
        this.board = board;
    }

    /**
     * Checks if a player can build a road between two vertices.
     * Rules: Must afford it, vertices must be adjacent, road must not exist, 
     * and must connect to player's structure without being blocked by an opponent.
     */
    public boolean canBuildRoad(Player player, Vertex start, Vertex end) {
        // 1. Check resource cost (1 Wood, 1 Brick)
        if (!player.canAfford(Cost.roadCost())) {
            return false;
        }

        // 2. Check if vertices are actually adjacent
        if (!start.getAdjacentVertices().contains(end)) {
            return false;
        }

        // 3. Check if a road already exists here
        for (Road road : board.getRoads()) {
            if (road.connects(start, end)) {
                return false;
            }
        }

        // 4. Connectivity: Road must touch a vertex owned by the player 
        // OR a vertex touching their road that isn't occupied by an opponent.
        return isConnectedForRoad(player, start) || isConnectedForRoad(player, end);
    }

    /**
     * Helper to check if a player can start a road from a specific vertex.
     */
    private boolean isConnectedForRoad(Player player, Vertex v) {
        // Valid if you own a building here
        if (v.getOwner() == player) {
            return true;
        }
        
        // Path is blocked if an opponent has a building here
        if (v.isOccupied()) {
            return false;
        }

        // Otherwise, check if any of your existing roads touch this vertex
        for (Road road : player.getRoadsBuilt()) {
            if (road.getStart() == v || road.getEnd() == v) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if a player can build a settlement at a vertex.
     * Rules: Must afford it, vertex must be unoccupied, distance rule (2 vertices) 
     * must be met, and it must connect to your road.
     */
    public boolean canBuildSettlement(Player player, Vertex location) {
        // 1. Check resource cost (1 Wood, 1 Brick, 1 Wheat, 1 Sheep)
        if (!player.canAfford(Cost.settlementCost())) {
            return false;
        }

        // 2. Vertex handles occupation and basic distance rule check
        if (!location.canBuild(player)) {
            return false;
        }

        // 3. Distance Rule: Check neighbors' neighbors to ensure at least 2 steps
        if (!respectsDistanceRule(location)) {
            return false;
        }

        // 4. Connectivity: During gameplay, settlement must be on one of your roads
        for (Road road : player.getRoadsBuilt()) {
            if (road.getStart() == location || road.getEnd() == location) {
                return true;
            }
        }

        // Special case: If player has no buildings (Turn 1 of Setup), they can build anywhere
        return player.getBuildingsBuilt().isEmpty();
    }

    /**
     * Checks if a player can build a city at a vertex.
     * Rules: Must afford it (2 Wheat, 3 Ore) and replace your own settlement.
     */
    public boolean canBuildCity(Player player, Vertex location) {
        if (!player.canAfford(Cost.cityCost())) {
            return false;
        }

        if (location == null || !location.isOccupied()) {
            return false;
        }

        Buildings building = location.getBuilding();
        return (building instanceof Settlement) && (building.getOwner() == player);
    }

    /**
     * Ensures no adjacent vertices have buildings (Distance Rule).
     */
    public boolean respectsDistanceRule(Vertex location) {
        for (Vertex adjacent : location.getAdjacentVertices()) {
            if (adjacent.isOccupied()) {
                return false;
            }
        }
        return true;
    }

    public void setBoard(Board board) {
        this.board = board;
    }
}