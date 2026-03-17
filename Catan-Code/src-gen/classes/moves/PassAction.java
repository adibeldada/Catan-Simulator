package classes.moves;

import classes.model.Player;
import classes.controller.GameMaster;

/**
 * Represents passing without taking an action.
 *
 * Cost:   None
 * Effect: Player takes no action this turn
 *
 * Command pattern (R3.1): undo() is a no-op because passing changes no game state.
 */
public class PassAction extends PlayerAction {

    public PassAction(Player player) {
        super(player);
    }

    @Override
    public void execute(GameMaster game) {
        game.logAction(player, describe());
    }

    /**
     * Undoing a pass is a no-op — it changes no game state.
     */
    @Override
    public void undo(GameMaster game) {
        // Nothing to reverse for a pass
    }

    @Override
    public String describe() {
        return "Passed turn";
    }
}