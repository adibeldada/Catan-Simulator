package CatanSimulatorDomainModel.catanUML.moves;

import CatanSimulatorDomainModel.catanUML.model.*;
import CatanSimulatorDomainModel.catanUML.controller.GameMaster;
/**
 * Represents the action of building a settlement.
 * 
 * Cost: 1 wood, 1 brick, 1 wheat, 1 sheep
 * Effect: Creates a settlement at a vertex
 * Victory Points: +1
 * 
 * The settlement is added to both the vertex and the player's collection.
 */
public class BuildSettlementMove extends Move {
    private Vertex location;

    /**
     * Constructs a BuildSettlementMove.
     * 
     * @param player The player building the settlement
     * @param location The vertex where the settlement will be placed
     */
    public BuildSettlementMove(Player player, Vertex location) {
        super(player);
        this.location = location;
    }

    /**
     * Executes the settlement building action.
     * 
     * Steps:
     * 1. Deduct resources (1 wood, 1 brick, 1 wheat, 1 sheep)
     * 2. Create the settlement object
     * 3. Place settlement on vertex
     * 4. Add settlement to player's collection
     * 5. Award 1 victory point
     * 6. Log the action
     */
    @Override
    public void execute(GameMaster game) {
        // Deduct resources
        player.spendResources(Cost.settlementCost());

        // Create and place settlement
        Settlement settlement = new Settlement(player);
        settlement.placeOn(location);
        player.addBuilding(settlement);

        // Award victory points
        player.addVictoryPoints(settlement.getVictoryPoints());
        
        // Log the action (R1.7)
        game.logAction(player, describe());
    }

    /**
     * Returns a description of this move for logging.
     * 
     * @return Human-readable description
     */
    @Override
    public String describe() {
        return String.format("Built settlement at vertex %d", location.getId());
    }

    // Getter
    public Vertex getLocation() { return location; }
}