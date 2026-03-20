package classes.model;

import classes.controller.GameMaster;
import classes.moves.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Concrete AI player implementing R3.2 and R3.3 via the Template Method hooks.
 *
 * resolveConstraint() — implements R3.3 constraints (checked before value moves)
 * pickBestValueMove() — implements R3.2 value-based selection via the Visitor pattern
 */
public class AIPlayer extends RuleBasedAIPlayer {

    public AIPlayer(int id) {
        super(id);
    }

    // -------------------------------------------------------------------------
    // Hook 1: R3.3 constraints — checked before any value-added actions
    // -------------------------------------------------------------------------

    @Override
    protected PlayerAction resolveConstraint(GameMaster game) {
        if (hand.totalCards() > 7) {
            PlayerAction spend = findAnyBuildAction(game);
            if (spend != null) return spend;
        }

        if (hasGapToClose(game)) {
            PlayerAction road = findRoadAction(game);
            if (road != null) return road;
        }

        if (longestRoadThreatened(game)) {
            PlayerAction road = findRoadAction(game);
            if (road != null) return road;
        }

        return null;
    }

    // -------------------------------------------------------------------------
    // Hook 2: R3.2 value evaluation — uses Visitor pattern (ValueEvaluator)
    // -------------------------------------------------------------------------

    @Override
    protected PlayerAction pickBestValueMove(GameMaster game) {
        List<ScoredAction> candidates = new ArrayList<>();
        ValueEvaluator evaluator = new ValueEvaluator(hand);

        for (PlayerAction a : getCandidateCities(game)) {
            candidates.add(new ScoredAction(a, a.accept(evaluator)));
        }

        for (PlayerAction a : getCandidateSettlements(game)) {
            candidates.add(new ScoredAction(a, a.accept(evaluator)));
        }

        for (PlayerAction a : getCandidateRoads(game)) {
            candidates.add(new ScoredAction(a, a.accept(evaluator)));
        }

        if (candidates.isEmpty()) {
            return new PassAction(this);
        }

        double max = candidates.stream()
                .mapToDouble(s -> s.value)
                .max()
                .orElse(0);

        List<PlayerAction> best = new ArrayList<>();
        for (ScoredAction s : candidates) {
            if (s.value == max) best.add(s.action);
        }

        return pickRandom(best);
    }

    // -------------------------------------------------------------------------
    // R3.3 helpers
    // -------------------------------------------------------------------------

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

    private boolean isWithinTwoSteps(Road r1, Road r2) {
        Vertex[] ends1 = {r1.getStart(), r1.getEnd()};
        Vertex[] ends2 = {r2.getStart(), r2.getEnd()};
        for (Vertex a : ends1) {
            for (Vertex b : ends2) {
                if (a == b) continue;
                if (a.getAdjacentVertices().contains(b)) return true;
                for (Vertex mid : a.getAdjacentVertices()) {
                    if (mid.getAdjacentVertices().contains(b)) return true;
                }
            }
        }
        return false;
    }

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

    private int computeLongestRoad() {
        return roadsBuilt.size();
    }

    private int computeLongestRoadFor(Player p) {
        return p.getRoadsBuilt().size();
    }

    private PlayerAction findAnyBuildAction(GameMaster game) {
        List<PlayerAction> cities = getCandidateCities(game);
        if (!cities.isEmpty()) return pickRandom(cities);
        List<PlayerAction> settlements = getCandidateSettlements(game);
        if (!settlements.isEmpty()) return pickRandom(settlements);
        List<PlayerAction> roads = getCandidateRoads(game);
        if (!roads.isEmpty()) return pickRandom(roads);
        return null;
    }

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