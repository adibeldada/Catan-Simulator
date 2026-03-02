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
public class AIPlayer extends Player {

	public AIPlayer(int id) {
		super(id);
		// TODO Auto-generated constructor stub
	}
	
	@Override
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
	
	@Override
	protected PlayerAction decideMove(GameMaster game, boolean mustBuild) {
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
   
}