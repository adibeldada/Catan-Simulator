package classes.model;

import classes.controller.GameMaster;
import classes.moves.*;
import java.util.List;

/**
 * AI Player that follows the priorities: City > Settlement > Road.
 */
public class AIPlayer extends Player {

    public AIPlayer(int id) {
        super(id);
    }

    @Override
    public void takeTurn(GameMaster game) {
        // R2.5: GameMaster handles the roll (including 7/Robber logic)
        game.rollAndDistribute(this);

        boolean turnActive = true;
        int safetyBreak = 0;

        // Loop allows multiple actions per turn as per standard AI behavior
        while (turnActive && safetyBreak < 10) {
            safetyBreak++;
            
            // Note: R2.5 logic for discarding >7 cards is handled in GameMaster, 
            boolean mustBuild = hand.totalCards() > 7;
            PlayerAction action = decideMove(game, mustBuild);

            if (action instanceof PassAction) {
                if (mustBuild && safetyBreak == 1) {
                    action.execute(game);
                }
                turnActive = false; 
            } else if (action != null) {
                action.execute(game);
            }
        }
    }

    @Override
    protected PlayerAction decideMove(GameMaster game, boolean mustBuild) {
        // Priority 1: Cities
        if (canAfford(Cost.cityCost())) {
            List<PlayerAction> cities = getCandidateCities(game);
            if (!cities.isEmpty()) return pickRandom(cities);
        }

        // Priority 2: Settlements
        if (canAfford(Cost.settlementCost())) {
            List<PlayerAction> settlements = getCandidateSettlements(game);
            if (!settlements.isEmpty()) return pickRandom(settlements);
        }

        // Priority 3: Roads
        if (canAfford(Cost.roadCost())) {
            List<PlayerAction> roads = getCandidateRoads(game);
            if (!roads.isEmpty()) return pickRandom(roads);
        }

        // No more valid moves or can't afford anything else
        return new PassAction(this);
    }
}