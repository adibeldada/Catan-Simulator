package classes.model;

import classes.controller.GameMaster;
import classes.moves.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Concrete AI player implementing R3.2 and R3.3 via the Template Method hooks.
 *
 * resolveConstraint() — implements R3.3 constraints (checked before value moves)
 * pickBestValueMove() — implements R3.2 value-based selection
 */
public class AIPlayer extends RuleBasedAIPlayer {

    // R3.2 value constants
    private static final double VALUE_VP       = 1.0;
    private static final double VALUE_BUILD    = 0.8;
    private static final double VALUE_LOW_HAND = 0.5;

    public AIPlayer(int id) {
        super(id);
    }

    // -------------------------------------------------------------------------
    // Hook 1: R3.3 constraints — checked before any value-added actions
    // -------------------------------------------------------------------------

    @Override
    protected PlayerAction resolveConstraint(GameMaster game) {
        // R3.3a: more than 7 cards — must spend them by building something
        if (hand.totalCards() > 7) {
            PlayerAction spend = findAnyBuildAction(game);
            if (spend != null) return spend;
        }

        // R3.3b: two road segments at most 2 units apart — try to connect them
        if (hasGapToClose(game)) {
            PlayerAction road = findRoadAction(game);
            if (road != null) return road;
        }

        // R3.3c: another player's longest road is at most 1 shorter — buy a road
        if (longestRoadThreatened(game)) {
            PlayerAction road = findRoadAction(game);
            if (road != null) return road;
        }

        return null;
    }

    // -------------------------------------------------------------------------
    // Hook 2: R3.2 value evaluation
    // -------------------------------------------------------------------------

    @Override
    protected PlayerAction pickBestValueMove(GameMaster game) {
        List<ScoredAction> candidates = new ArrayList<>();

        // Cities earn a VP → value 1.0
        for (PlayerAction a : getCandidateCities(game)) {
            candidates.add(new ScoredAction(a, VALUE_VP));
        }

        // Settlements earn a VP → value 1.0
        for (PlayerAction a : getCandidateSettlements(game)) {
            candidates.add(new ScoredAction(a, VALUE_VP));
        }

        // Roads do not earn a VP → value 0.8 normally, 0.5 if hand drops below 5
        for (PlayerAction a : getCandidateRoads(game)) {
            // Road costs 1 wood + 1 brick = 2 cards
            double val = (hand.totalCards() - 2 < 5) ? VALUE_LOW_HAND : VALUE_BUILD;
            candidates.add(new ScoredAction(a, val));
        }

        if (candidates.isEmpty()) {
            return new PassAction(this);
        }

        // Find the maximum value
        double max = candidates.stream()
                .mapToDouble(s -> s.value)
                .max()
                .orElse(0);

        // Collect all actions tied at max value
        List<PlayerAction> best = new ArrayList<>();
        for (ScoredAction s : candidates) {
            if (s.value == max) best.add(s.action);
        }

        // R3.2: tie → pick random
        return pickRandom(best);
    }

    // -------------------------------------------------------------------------
    // R3.3 helpers
    // -------------------------------------------------------------------------

    /**
     * R3.3b: returns true if this player has two road segments
     * whose endpoints are at most 2 vertex hops apart.
     */
    private boolean hasGapToClose(GameMaster game) {
        if (roadsBuilt.size() < 2) return false;
        for (Road r1 : roadsBuilt) {
            for (Road r2 : roadsBuilt) {
                if (r1 == r2) continue;
                if (isWithinTwoSteps(r1, r2)) return true;
            }
        }
        return false;
    }

    /**
     * Returns true if any endpoint of r1 is within 2 vertex hops of any endpoint of r2.
     */
    private boolean isWithinTwoSteps(Road r1, Road r2) {
        Vertex[] ends1 = {r1.getStart(), r1.getEnd()};
        Vertex[] ends2 = {r2.getStart(), r2.getEnd()};
        for (Vertex a : ends1) {
            for (Vertex b : ends2) {
                if (a == b) continue;
                // 1 step: directly adjacent
                if (a.getAdjacentVertices().contains(b)) return true;
                // 2 steps: share a common neighbour
                for (Vertex mid : a.getAdjacentVertices()) {
                    if (mid.getAdjacentVertices().contains(b)) return true;
                }
            }
        }
        return false;
    }

    /**
     * R3.3c: returns true if any other player's longest road is
     * at most 1 shorter than this player's longest road.
     */
    private boolean longestRoadThreatened(GameMaster game) {
        int myLongest = computeLongestRoad();
        if (myLongest == 0) return false;
        for (Player p : game.getPlayers()) {
            if (p == this) continue;
            int theirLongest = computeLongestRoadFor(p);
            if (theirLongest >= myLongest - 1) return true;
        }
        return false;
    }

    /**
     * Simple longest-road estimate for this player: number of roads built.
     */
    private int computeLongestRoad() {
        return roadsBuilt.size();
    }

    /**
     * Simple longest-road estimate for another player.
     */
    private int computeLongestRoadFor(Player p) {
        return p.getRoadsBuilt().size();
    }

    /**
     * Try to build something — city first, then settlement, then road.
     * Used when R3.3a forces the player to spend cards.
     */
    private PlayerAction findAnyBuildAction(GameMaster game) {
        List<PlayerAction> cities = getCandidateCities(game);
        if (!cities.isEmpty()) return pickRandom(cities);
        List<PlayerAction> settlements = getCandidateSettlements(game);
        if (!settlements.isEmpty()) return pickRandom(settlements);
        List<PlayerAction> roads = getCandidateRoads(game);
        if (!roads.isEmpty()) return pickRandom(roads);
        return null;
    }

    /**
     * Find a valid road to build (used by R3.3b and R3.3c).
     */
    private PlayerAction findRoadAction(GameMaster game) {
        List<PlayerAction> roads = getCandidateRoads(game);
        return roads.isEmpty() ? null : pickRandom(roads);
    }

    // -------------------------------------------------------------------------
    // Inner helper class
    // -------------------------------------------------------------------------

    private static class ScoredAction {
        final PlayerAction action;
        final double value;

        ScoredAction(PlayerAction action, double value) {
            this.action = action;
            this.value = value;
        }
    }
}