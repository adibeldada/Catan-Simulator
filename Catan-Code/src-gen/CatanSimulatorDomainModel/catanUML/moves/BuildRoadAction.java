package CatanSimulatorDomainModel.catanUML.moves;

import CatanSimulatorDomainModel.catanUML.model.*;
import CatanSimulatorDomainModel.catanUML.controller.GameMaster;

/*import CatanSimulatorDomainModel.catanUML.epresents the action of building a road.
 * 
 * Cost: 1 wood, 1 brick
 * Effect: Creates a road connecting two vertices
 * 
 * The road is added to both the board and the player's collection.
 */
public class BuildRoadMove extends Move {
    private Vertex start;
    private Vertex end;

    /**
     * Constructs a BuildRoadMove.
     * 
     * @param player The player building the road
     * @param start The starting vertex
     * @param end The ending vertex
     */
    public BuildRoadMove(Player player, Vertex start, Vertex end) {
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
     * 3. Add road to board
     * 4. Add road to player's collection
     * 5. Log the action
     */
    @Override
    public void execute(GameMaster game) {
        // Deduct resources
        player.spendResources(Cost.roadCost());

        // Create and place road
        Road road = new Road(player, start, end);
        game.getBoard().placeRoad(road);
        player.addRoad(road);
        
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
        return String.format("Built road between vertices %d and %d", 
                           start.getId(), end.getId());
    }

    // Getters
    public Vertex getStart() { return start; }
    public Vertex getEnd() { return end; }
}