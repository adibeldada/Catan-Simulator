package CatanSimulatorDomainModel.catanUML.model;

import CatanSimulatorDomainModel.catanUML.enums.ResourceType;
import CatanSimulatorDomainModel.catanUML.controller.GameMaster;
import CatanSimulatorDomainModel.catanUML.moves.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Represents a player in the game.
 * * Players manage resources, make decisions, and track their buildings and roads.
 * This implementation uses random decision-making to simulate AI behavior.
 * * R1.8: Players with more than 7 cards must attempt to build.
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
     * * @param id Unique identifier for this player (1-4)
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
     * * @param game The GameMaster controlling the game
     */
    public void takeTurn(GameMaster game) {
        boolean buildingActed = false; // Tracks if a building action occurred (R1.7/R1.8)
        
        // R1.8: If player has more than 7 cards, must try to build until they are under the limit
        while (hand.totalCards() > 7) {
            PlayerAction move = decideMove(game, true);
            
            if (move instanceof PassAction) {
                // If the player is forced to pass while holding >7 cards, we MUST execute the 
                // move to ensure the action is logged to the console (R1.7).
                if (!buildingActed) {
                    move.execute(game);
                }
                return; // End turn
            } else {
                move.execute(game);
                buildingActed = true;
            }
        }
        
        // If the player did not build anything to satisfy R1.8 (or was already under 7),
        // take a standard turn to ensure at least one action log entry is generated (R1.7).
        if (!buildingActed) {
            decideMove(game, false).execute(game);
        }
    }

    private PlayerAction decideMove(GameMaster game, boolean mustBuild) {
        List<PlayerAction> possibleMoves = new ArrayList<>();

        // 1. Priority: Cities (2 VP)
        if (canAfford(Cost.cityCost())) {
            for (Buildings b : buildingsBuilt) {
                if (b instanceof Settlement && game.getRuleValidator().canBuildCity(this, b.getLocation())) {
                    possibleMoves.add(new BuildCityAction(this, b.getLocation()));
                }
            }
        }
        // If we found cities and "must build", don't even look at roads yet to save resources
        if (!possibleMoves.isEmpty()) return possibleMoves.get(random.nextInt(possibleMoves.size()));

        // 2. Priority: Settlements (1 VP)
        if (canAfford(Cost.settlementCost())) {
            for (Vertex v : game.getBoard().getVertices()) {
                if (game.getRuleValidator().canBuildSettlement(this, v)) {
                    possibleMoves.add(new BuildSettlementAction(this, v));
                }
            }
        }
        if (!possibleMoves.isEmpty()) return possibleMoves.get(random.nextInt(possibleMoves.size()));

        // 3. Priority: Roads (Expansion)
        if (canAfford(Cost.roadCost())) {
            for (Vertex v1 : game.getBoard().getVertices()) {
                for (Vertex v2 : v1.getAdjacentVertices()) {
                    if (game.getRuleValidator().canBuildRoad(this, v1, v2)) {
                        possibleMoves.add(new BuildRoadAction(this, v1, v2));
                    }
                }
            }
        }

        // Final decision
        if (possibleMoves.isEmpty()) {
            return new PassAction(this);
        }

        // If not forced to build, 30% chance to save resources for a bigger building
        if (!mustBuild && random.nextDouble() < 0.3) {
            return new PassAction(this);
        }

        return possibleMoves.get(random.nextInt(possibleMoves.size()));
    }

    /**
     * Collects a resource when a tile produces.
     * * @param resource The type of resource to collect
     * @param amount The amount to collect
     */
    public void collectResource(ResourceType resource, int amount) {
        hand.add(resource, amount);
    }

    /**
     * Checks if the player can afford a given cost.
     * * @param cost The cost to check
     * @return true if player has enough resources
     */
    public boolean canAfford(Cost cost) {
        return hand.hasEnough(cost);
    }

    /**
     * Spends resources according to the cost.
     * Used when building structures.
     * * @param cost The cost to spend
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
     * * @param amount The amount of victory points to add
     */
    public void addVictoryPoints(int amount) {
        victoryPoints += amount;
    }

    /**
     * Adds a road to the player's collection.
     * * @param road The road to add
     */
    public void addRoad(Road road) {
        roadsBuilt.add(road);
    }

    /**
     * Adds a building to the player's collection.
     * * @param building The building to add
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