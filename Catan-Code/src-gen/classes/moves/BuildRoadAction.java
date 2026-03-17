package classes.moves;

import classes.model.*;
import classes.controller.GameMaster;

/**
 * Represents the action of building a road.
 *
 * Cost: 1 wood, 1 brick
 * Effect: Creates a road connecting two vertices
 *
 * Command pattern (R3.1): execute() places the road;
 * undo() removes it and refunds resources.
 */
public class BuildRoadAction extends PlayerAction {
    private Vertex start;
    private Vertex end;
    /** The road object created during execute(), needed for undo(). */
    private Road placedRoad;

    /**
     * Constructs a BuildRoadAction.
     *
     * @param player The player building the road
     * @param start  The starting vertex
     * @param end    The ending vertex
     */
    public BuildRoadAction(Player player, Vertex start, Vertex end) {
        super(player);
        this.start = start;
        this.end = end;
    }

    /**
     * Executes the road building action.
     *
     * Steps:
     * 1. Deduct resources (1 wood, 1 brick)
     * 2. Create the road object
     * 3. Add road to board and player's collection
     * 4. Log the action
     */
    @Override
    public void execute(GameMaster game) {
        player.spendResources(Cost.roadCost());

        placedRoad = new Road(player, start, end);
        game.getBoard().placeRoad(placedRoad);
        player.addRoad(placedRoad);

        game.logAction(player, describe());
    }

    /**
     * Undoes the road building action (R3.1).
     *
     * Reverses every change made by execute():
     * 1. Remove the road from the board
     * 2. Remove road from player's collection
     * 3. Refund resources (1 wood, 1 brick)
     * 4. Log the undo
     */
    @Override
    public void undo(GameMaster game) {
        // Remove from the board's road list
        game.getBoard().getRoads().remove(placedRoad);

        // Remove from the player's road list
        player.getRoadsBuilt().remove(placedRoad);

        // Refund resources
        player.collectResource(classes.enums.ResourceType.WOOD,  1);
        player.collectResource(classes.enums.ResourceType.BRICK, 1);

        game.logAction(player, "Undid: " + describe());
    }

    @Override
    public String describe() {
        return String.format("Built road between vertices %d and %d", start.getId(), end.getId());
    }

    public Vertex getStart() { return start; }
    public Vertex getEnd()   { return end; }
}