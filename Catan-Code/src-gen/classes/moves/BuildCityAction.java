package classes.moves;

import classes.model.*;
import classes.controller.GameMaster;

/**
 * Represents the action of building a city (upgrading a settlement).
 *
 * Cost: 2 wheat, 3 ore
 * Effect: Replaces an existing settlement with a city
 * Victory Points: +1 (city=2, settlement was already 1)
 *
 * Command pattern (R3.1): execute() upgrades to city;
 * undo() restores the original settlement and refunds resources.
 */
public class BuildCityAction extends PlayerAction {
    private Vertex location;
    /** The city object placed during execute(), needed for undo(). */
    private City placedCity;
    /** The settlement that was replaced, restored during undo(). */
    private Settlement replacedSettlement;

    /**
     * Constructs a BuildCityAction.
     *
     * @param player   The player building the city
     * @param location The vertex with an existing settlement to upgrade
     */
    public BuildCityAction(Player player, Vertex location) {
        super(player);
        this.location = location;
    }

    /**
     * Executes the city building action.
     *
     * Steps:
     * 1. Save the existing settlement for potential undo
     * 2. Deduct resources (2 wheat, 3 ore)
     * 3. Create and place the city
     * 4. Update player's building collection
     * 5. Award 1 additional victory point
     * 6. Log the action
     */
    @Override
    public void execute(GameMaster game) {
        // Save the existing settlement so undo() can restore it
        replacedSettlement = (Settlement) location.getBuilding();

        player.spendResources(Cost.cityCost());

        placedCity = new City(player);
        placedCity.placeOn(location);

        player.getBuildingsBuilt().remove(replacedSettlement);
        player.addBuilding(placedCity);
        player.addVictoryPoints(1); // net gain: city=2, settlement was 1

        game.logAction(player, describe());
    }

    /**
     * Undoes the city building action (R3.1).
     *
     * Reverses every change made by execute():
     * 1. Remove the city from the vertex and player's collection
     * 2. Restore the original settlement on the vertex and in the collection
     * 3. Deduct 1 victory point
     * 4. Refund resources (2 wheat, 3 ore)
     * 5. Log the undo
     */
    @Override
    public void undo(GameMaster game) {
        // Swap city back to the saved settlement
        location.setBuilding(replacedSettlement);

        player.getBuildingsBuilt().remove(placedCity);
        player.addBuilding(replacedSettlement);

        // Reverse the net VP gain
        player.addVictoryPoints(-1);

        // Refund resources
        player.collectResource(classes.enums.ResourceType.WHEAT, 2);
        player.collectResource(classes.enums.ResourceType.ORE,   3);

        game.logAction(player, "Undid: " + describe());
    }
    
    @Override
    public double accept(ActionVisitor visitor) {
        return visitor.visit(this);
    }

    @Override
    public String describe() {
        return String.format("Upgraded settlement to city at vertex %d", location.getId());
    }

    public Vertex getLocation() { return location; }
}