package classes.moves;

import classes.model.*;
import classes.controller.GameMaster;

/**
 * Represents the action of building a settlement.
 *
 * Cost: 1 wood, 1 brick, 1 wheat, 1 sheep
 * Effect: Creates a settlement at a vertex
 * Victory Points: +1
 *
 * Command pattern (R3.1): execute() places the settlement;
 * undo() removes it and refunds resources.
 */
public class BuildSettlementAction extends PlayerAction {
    private Vertex location;
    /** The settlement object created during execute(), needed for undo(). */
    private Settlement placedSettlement;

    /**
     * Constructs a BuildSettlementAction.
     *
     * @param player   The player building the settlement
     * @param location The vertex where the settlement will be placed
     */
    public BuildSettlementAction(Player player, Vertex location) {
        super(player);
        this.location = location;
    }

    /**
     * Executes the settlement building action.
     *
     * Steps:
     * 1. Deduct resources (1 wood, 1 brick, 1 wheat, 1 sheep)
     * 2. Create and place the settlement on the vertex
     * 3. Add settlement to player's collection
     * 4. Award 1 victory point
     * 5. Log the action
     */
    @Override
    public void execute(GameMaster game) {
        player.spendResources(Cost.settlementCost());

        placedSettlement = new Settlement(player);
        placedSettlement.placeOn(location);
        player.addBuilding(placedSettlement);
        player.addVictoryPoints(placedSettlement.getVictoryPoints());

        game.logAction(player, describe());
    }
    
    @Override
    public double accept(ActionVisitor visitor) {
        return visitor.visit(this);
    }

    /**
     * Undoes the settlement building action (R3.1).
     *
     * Reverses every change made by execute():
     * 1. Remove the settlement from the vertex
     * 2. Remove settlement from player's collection
     * 3. Deduct 1 victory point
     * 4. Refund resources (1 wood, 1 brick, 1 wheat, 1 sheep)
     * 5. Log the undo
     */
    @Override
    public void undo(GameMaster game) {
        // Remove the building from the vertex
        location.setBuilding(null);

        // Remove from player's collection
        player.getBuildingsBuilt().remove(placedSettlement);

        // Reverse the victory point award
        player.addVictoryPoints(-placedSettlement.getVictoryPoints());

        // Refund resources
        player.collectResource(classes.enums.ResourceType.WOOD,  1);
        player.collectResource(classes.enums.ResourceType.BRICK, 1);
        player.collectResource(classes.enums.ResourceType.WHEAT, 1);
        player.collectResource(classes.enums.ResourceType.SHEEP, 1);

        game.logAction(player, "Undid: " + describe());
    }

    @Override
    public String describe() {
        return String.format("Built settlement at vertex %d", location.getId());
    }

    public Vertex getLocation() { return location; }
}