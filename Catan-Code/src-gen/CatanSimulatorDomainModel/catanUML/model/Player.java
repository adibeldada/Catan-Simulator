package CatanSimulatorDomainModel.catanUML.model;

import CatanSimulatorDomainModel.catanUML.enums.ResourceType;
import CatanSimulatorDomainModel.catanUML.controller.GameMaster;
import CatanSimulatorDomainModel.catanUML.moves.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
/**
 * Represents a player in the game.
 * 
 * Players manage resources, make decisions, and track their buildings and roads.
 * This implementation uses random decision-making to simulate AI behavior.
 * 
 * R1.8: Players with more than 7 cards must attempt to build.
 */
public class Player {
    private int id;
    private ResourceHand hand;
    private int victoryPoints;
    private List<Road> roadsBuilt;
    private List<Buildings> buildingsBuilt;
    private Random random;

    /**
     * Constructs a Player with the specified ID.
     * 
     * @param id Unique identifier for this player (1-4)
     */
    public Player(int id) {
        this.id = id;
        this.hand = new ResourceHand();
        this.victoryPoints = 0;
        this.roadsBuilt = new ArrayList<>();
        this.buildingsBuilt = new ArrayList<>();
        this.random = new Random();
    }

    /**
     * Takes a turn in the game.
     * Randomly decides what action to take based on available resources.
     * 
     * @param game The GameMaster controlling the game
     */
    public void takeTurn(GameMaster game) {
        // R1.8: If player has more than 7 cards, must try to build
        boolean mustBuild = hand.totalCards() > 7;
        
        // Try to build something if possible
        PlayerAction move = decideMove(game, mustBuild);
        move.execute(game);
    }

    /**
     * Decides what move to make based on resources and game state.
     * Uses random selection from valid moves.
     * 
     * Priority order:
     * 1. Build city (highest VP value)
     * 2. Build settlement (VP gain)
     * 3. Build road (network expansion)
     * 4. Pass (if can't or choose not to build)
     * 
     * @param game The game state
     * @param mustBuild Whether player must attempt to build (>7 cards)
     * @return The selected move
     */
    private PlayerAction decideMove(GameMaster game, boolean mustBuild) {
        List<PlayerAction> possibleMoves = new ArrayList<>();

        // Try to build a city (highest priority - 2 VP)
        if (canAfford(Cost.cityCost())) {
            for (Buildings building : buildingsBuilt) {
                if (building instanceof Settlement) {
                    Vertex location = building.getLocation();
                    if (game.getRuleValidator().canBuildCity(this, location)) {
                        possibleMoves.add(new BuildCityAction(this, location));
                    }
                }
            }
        }

        // Try to build a settlement (1 VP)
        if (canAfford(Cost.settlementCost())) {
            for (Vertex vertex : game.getBoard().getVertices()) {
                if (game.getRuleValidator().canBuildSettlement(this, vertex)) {
                    possibleMoves.add(new BuildSettlementAction(this, vertex));
                }
            }
        }

        // Try to build a road (network expansion)
        if (canAfford(Cost.roadCost())) {
            for (Vertex v1 : game.getBoard().getVertices()) {
                for (Vertex v2 : v1.getAdjacentVertices()) {
                    if (game.getRuleValidator().canBuildRoad(this, v1, v2)) {
                        possibleMoves.add(new BuildRoadAction(this, v1, v2));
                    }
                }
            }
        }

        // If must build but can't, or choose to pass
        if (possibleMoves.isEmpty() || (!mustBuild && random.nextDouble() < 0.3)) {
            return new PassAction(this);
        }

        // Randomly select from possible moves
        return possibleMoves.get(random.nextInt(possibleMoves.size()));
    }

    /**
     * Collects a resource when a tile produces.
     * 
     * @param resource The type of resource to collect
     * @param amount The amount to collect
     */
    public void collectResource(ResourceType resource, int amount) {
        hand.add(resource, amount);
    }

    /**
     * Checks if the player can afford a given cost.
     * 
     * @param cost The cost to check
     * @return true if player has enough resources
     */
    public boolean canAfford(Cost cost) {
        return hand.hasEnough(cost);
    }

    /**
     * Spends resources according to the cost.
     * Used when building structures.
     * 
     * @param cost The cost to spend
     */
    public void spendResources(Cost cost) {
        hand.remove(ResourceType.WOOD, cost.getWood());
        hand.remove(ResourceType.BRICK, cost.getBrick());
        hand.remove(ResourceType.WHEAT, cost.getWheat());
        hand.remove(ResourceType.SHEEP, cost.getSheep());
        hand.remove(ResourceType.ORE, cost.getOre());
    }

    /**
     * Adds victory points to the player's total.
     * 
     * @param amount The amount of victory points to add
     */
    public void addVictoryPoints(int amount) {
        victoryPoints += amount;
    }

    /**
     * Adds a road to the player's collection.
     * 
     * @param road The road to add
     */
    public void addRoad(Road road) {
        roadsBuilt.add(road);
    }

    /**
     * Adds a building to the player's collection.
     * 
     * @param building The building to add
     */
    public void addBuilding(Buildings building) {
        buildingsBuilt.add(building);
    }

    // Getters
    public int getId() { return id; }
    public ResourceHand getHand() { return hand; }
    public int getVictoryPoints() { return victoryPoints; }
    public List<Road> getRoadsBuilt() { return roadsBuilt; }
    public List<Buildings> getBuildingsBuilt() { return buildingsBuilt; }

    @Override
    public String toString() {
        return String.format("Player %d (VP:%d, Cards:%d)", id, victoryPoints, hand.totalCards());
    }
}