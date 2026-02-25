package classes.model;

import classes.enums.ResourceType;
import classes.controller.GameMaster;
import classes.moves.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Represents a player in the game.
 * Players manage resources, make decisions, and track their buildings and roads.
 */
public class Player {

    private int id;
    private ResourceHand hand;
    private int victoryPoints;
    private List<Road> roadsBuilt;
    private List<Buildings> buildingsBuilt;
    private Random random;

    public Player(int id) {
        this.id = id;
        this.hand = new ResourceHand();
        this.victoryPoints = 0;
        this.roadsBuilt = new ArrayList<>();
        this.buildingsBuilt = new ArrayList<>();
        this.random = new Random();
    }

    public void takeTurn(GameMaster game) {
        boolean buildingActed = false;
        
        // R1.8: If player has more than 7 cards, must try to build
        while (hand.totalCards() > 7) {
            PlayerAction move = decideMove(game, true);
            if (move instanceof PassAction) {
                if (!buildingActed) {
                    move.execute(game);
                }
                return;
            } else {
                move.execute(game);
                buildingActed = true;
            }
        }

        if (!buildingActed) {
            decideMove(game, false).execute(game);
        }
    }

    /**
     * Refactored to meet Cognitive Complexity requirements (Score: ~8)
     */
    private PlayerAction decideMove(GameMaster game, boolean mustBuild) {
        // 1. Priority: Cities (2 VP) - Immediate return if found
        if (canAfford(Cost.cityCost())) {
            List<PlayerAction> cities = getCandidateCities(game);
            if (!cities.isEmpty()) return pickRandom(cities);
        }

        // 2. Priority: Settlements (1 VP) - Immediate return if found
        if (canAfford(Cost.settlementCost())) {
            List<PlayerAction> settlements = getCandidateSettlements(game);
            if (!settlements.isEmpty()) return pickRandom(settlements);
        }

        // 3. Priority: Roads (Expansion)
        List<PlayerAction> roads = new ArrayList<>();
        if (canAfford(Cost.roadCost())) {
            roads = getCandidateRoads(game);
        }

        // Final decision logic
        if (roads.isEmpty()) {
            return new PassAction(this);
        }

        // 30% chance to save resources if not forced to build
        if (!mustBuild && random.nextDouble() < 0.3) {
            return new PassAction(this);
        }

        return pickRandom(roads);
    }

    private List<PlayerAction> getCandidateCities(GameMaster game) {
        List<PlayerAction> moves = new ArrayList<>();
        for (Buildings b : buildingsBuilt) {
            if (b instanceof Settlement && game.getRuleValidator().canBuildCity(this, b.getLocation())) {
                moves.add(new BuildCityAction(this, b.getLocation()));
            }
        }
        return moves;
    }

    private List<PlayerAction> getCandidateSettlements(GameMaster game) {
        List<PlayerAction> moves = new ArrayList<>();
        for (Vertex v : game.getBoard().getVertices()) {
            if (game.getRuleValidator().canBuildSettlement(this, v)) {
                moves.add(new BuildSettlementAction(this, v));
            }
        }
        return moves;
    }

    private List<PlayerAction> getCandidateRoads(GameMaster game) {
        List<PlayerAction> moves = new ArrayList<>();
        for (Vertex v1 : game.getBoard().getVertices()) {
            for (Vertex v2 : v1.getAdjacentVertices()) {
                if (game.getRuleValidator().canBuildRoad(this, v1, v2)) {
                    moves.add(new BuildRoadAction(this, v1, v2));
                }
            }
        }
        return moves;
    }

    private PlayerAction pickRandom(List<PlayerAction> moves) {
        return moves.get(random.nextInt(moves.size()));
    }

    public void collectResource(ResourceType resource, int amount) {
        hand.add(resource, amount);
    }

    public boolean canAfford(Cost cost) {
        return hand.hasEnough(cost);
    }

    public void spendResources(Cost cost) {
        hand.remove(ResourceType.WOOD, cost.getWood());
        hand.remove(ResourceType.BRICK, cost.getBrick());
        hand.remove(ResourceType.WHEAT, cost.getWheat());
        hand.remove(ResourceType.SHEEP, cost.getSheep());
        hand.remove(ResourceType.ORE, cost.getOre());
    }

    public void addVictoryPoints(int amount) {
        victoryPoints += amount;
    }

    public void addRoad(Road road) {
        roadsBuilt.add(road);
    }

    public void addBuilding(Buildings building) {
        buildingsBuilt.add(building);
    }

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