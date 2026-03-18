package classes.model;

import classes.controller.GameMaster;
import classes.moves.*;
import java.util.List;

/**
 * Abstract AI player implementing the Template Method design pattern (Task 2).
 *
 * The template method takeTurn() defines the fixed algorithm skeleton:
 *   1. Roll and distribute resources
 *   2. Resolve constraints (R3.3) before value-added actions
 *   3. Evaluate candidate moves by value (R3.2) and execute the best one
 *   4. Repeat until no moves remain, then pass
 *
 * Subclasses override the hook methods to provide specific constraint
 * checks and move evaluation logic without changing the skeleton.
 */
public abstract class RuleBasedAIPlayer extends Player {

    protected RuleBasedAIPlayer(int id) {
        super(id);
    }

    /**
     * THE TEMPLATE METHOD — final so subclasses cannot change the skeleton.
     * Defines the invariant turn structure for all rule-based AI players.
     */
    @Override
    public final void takeTurn(GameMaster game) {
        game.rollAndDistribute(this);

        int safety = 0;
        while (safety++ < 10) {
            // R3.3: constraints come first
            PlayerAction constraint = resolveConstraint(game);
            if (constraint != null) {
                constraint.execute(game);
                continue;
            }

            // R3.2: pick highest-value move
            PlayerAction best = pickBestValueMove(game);
            if (best == null || best instanceof PassAction) {
                new PassAction(this).execute(game);
                return;
            }
            best.execute(game);
        }
        new PassAction(this).execute(game);
    }

    /**
     * Hook 1 — R3.3 constraints.
     * Return a forced action if a constraint applies, null otherwise.
     * Subclasses override this to implement specific constraint logic.
     */
    protected abstract PlayerAction resolveConstraint(GameMaster game);

    /**
     * Hook 2 — R3.2 value evaluation.
     * Return the highest-value action, or PassAction if none available.
     * Subclasses override this to implement specific evaluation logic.
     */
    protected abstract PlayerAction pickBestValueMove(GameMaster game);

    /**
     * Delegates to pickBestValueMove for compatibility with Player abstract method.
     */
    @Override
    protected PlayerAction decideMove(GameMaster game, boolean mustBuild) {
        return pickBestValueMove(game);
    }
}