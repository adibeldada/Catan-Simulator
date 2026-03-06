package classes.model;

import classes.enums.ResourceType;
import classes.controller.GameMaster;
import classes.moves.*;
import java.util.ArrayList;
import java.util.List;

public class AIPlayer extends Player {

    public AIPlayer(int id) {
        super(id);
    }
    // taketurn
    @Override
    public void takeTurn(GameMaster game) {
        // Roll at the start of every turn
        game.rollAndDistribute(this);

        boolean turnActive = true;
        int safetyBreak = 0;

        // Loop allows multiple actions per turn (e.g., Road + Settlement)
        while (turnActive && safetyBreak < 10) {
            safetyBreak++;
            boolean mustBuild = hand.totalCards() > 7;
            PlayerAction action = decideMove(game, mustBuild);

            if (action instanceof PassAction) {
                // If we are forced to build but can't, log it and end turn
                if (mustBuild && safetyBreak == 1) {
                    action.execute(game);
                }
                turnActive = false; 
            } else if (action != null) {
                action.execute(game);
                // After an action, the loop continues to see if we can build more
            }
        }
    }

    @Override
    protected PlayerAction decideMove(GameMaster game, boolean mustBuild) {
        // 1. Priority: Cities (2 VP)
        if (canAfford(Cost.cityCost())) {
            List<PlayerAction> cities = getCandidateCities(game);
            if (!cities.isEmpty()) return pickRandom(cities);
        }

        // 2. Priority: Settlements (1 VP)
        if (canAfford(Cost.settlementCost())) {
            List<PlayerAction> settlements = getCandidateSettlements(game);
            if (!settlements.isEmpty()) return pickRandom(settlements);
        }

        // 3. Priority: Roads (Expansion)
        if (canAfford(Cost.roadCost())) {
            List<PlayerAction> roads = getCandidateRoads(game);
            if (!roads.isEmpty()) return pickRandom(roads);
        }

        // 4. NEW: Emergency 4:1 Bank Trade (Prevents Resource Deadlock)
        // If we can't build anything, see if we have 4 of one resource to trade for a missing one
        PlayerAction tradeAction = checkBankTrade();
        if (tradeAction != null) return tradeAction;

        return new PassAction(this);
    }

    private PlayerAction checkBankTrade() {
        // Logic: Find a resource we have >= 4 of, and a resource we have 0 of that we need
        ResourceType surplus = null;
        if (hand.getWood() >= 4) surplus = ResourceType.WOOD;
        else if (hand.getBrick() >= 4) surplus = ResourceType.BRICK;
        else if (hand.getWheat() >= 4) surplus = ResourceType.WHEAT;
        else if (hand.getSheep() >= 4) surplus = ResourceType.SHEEP;
        else if (hand.getOre() >= 4) surplus = ResourceType.ORE;

        if (surplus != null) {
            // Pick a resource we are missing for a settlement
            ResourceType wanted = null;
            if (hand.getWood() == 0) wanted = ResourceType.WOOD;
            else if (hand.getBrick() == 0) wanted = ResourceType.BRICK;
            else if (hand.getWheat() == 0) wanted = ResourceType.WHEAT;
            else if (hand.getSheep() == 0) wanted = ResourceType.SHEEP;

            if (wanted != null) {

                hand.remove(surplus, 4);
                hand.add(wanted, 1);
                // Return null so the loop runs again and tries to build with the new resource
                return null; 
            }
        }
        return null;
    }
}