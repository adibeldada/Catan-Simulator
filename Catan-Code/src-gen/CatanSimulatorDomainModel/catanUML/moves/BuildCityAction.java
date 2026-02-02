package CatanSimulatorDomainModel.catanUML.moves;

import CatanSimulatorDomainModel.catanUML.model.*;
import CatanSimulatorDomainModel.catanUML.controller.GameMaster;
/**
 * Represents the action of building a city (upgrading a settlement).
 * 
 * Cost: 2 wheat, 3 ore
 * Effect: Replaces an existing settlement with a city
 * Victory Points: +1 (total 2, since settlement already gave 1)
 * 
 * R1.6: Cities must replace existing settlements.
 */
public class BuildCityAction extends PlayerAction {
    private Vertex location;

    /**
     * Constructs a BuildCityMove.
     * 
     * @param player The player building the city
     * @param location The vertex where the city will be placed (must have a settlement)
     */
    public BuildCityAction(Player player, Vertex location) {
        super(player);
        this.location = location;
    }

    /**
     * Executes the city building action.
     * 
     * Steps:
     * 1. Deduct resources (2 wheat, 3 ore)
     * 2. Get the existing settlement
     * 3. Create the city object
     * 4. Replace settlement with city on the vertex
     * 5. Update player's building collection
     * 6. Award 1 additional victory point (city=2, settlement was 1)
     * 7. Log the action
     */
    @Override
    public void execute(GameMaster game) {
        // Deduct resources
        player.spendResources(Cost.cityCost());

        // Get the old settlement
        Buildings oldBuilding = location.getBuilding();
        
        // Create and place city
        City city = new City(player);
        location.setBuilding(city);
        city.placeOn(location);

        // Remove old settlement from player's buildings and add city
        player.getBuildingsBuilt().remove(oldBuilding);
        player.addBuilding(city);

        // Award additional victory point (city gives 2, settlement gave 1)
        player.addVictoryPoints(1);
        
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
        return String.format("Upgraded settlement to city at vertex %d", location.getId());
    }

    // Getter
    public Vertex getLocation() { return location; }
}