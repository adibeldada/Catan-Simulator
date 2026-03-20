package classes.moves;

import classes.model.Player;
import classes.controller.GameMaster;

/**
 * Abstract base class for player moves/actions.
 *
 * Implements the Command design pattern for player actions.
 * Each concrete action (road, settlement, city, pass) extends this class
 * and provides both execute() and undo() implementations, enabling
 * undo/redo functionality (R3.1).
 *
 * The execute() method performs the action and updates game state.
 * The undo()    method reverses the action and restores prior game state.
 * The describe() method provides a human-readable description for logging.
 */
public abstract class PlayerAction {
    protected Player player;

    /**
     * Constructs a PlayerAction for the specified player.
     *
     * @param player The player making this move
     */
    protected PlayerAction(Player player) {
        this.player = player;
    }

    /**
     * Executes this action in the context of the game.
     * Must be implemented by all concrete action types.
     *
     * This method should:
     * 1. Deduct resources (if applicable)
     * 2. Update game state (place building/road)
     * 3. Award victory points (if applicable)
     * 4. Log the action
     *
     * @param game The GameMaster controlling the game
     */
    public abstract void execute(GameMaster game);

    /**
     * Undoes this action, restoring game state to before execute() was called.
     * Must be implemented by all concrete action types (R3.1).
     *
     * This method should reverse every change made by execute():
     * 1. Refund resources (if applicable)
     * 2. Remove building/road from board and player collections
     * 3. Deduct victory points (if applicable)
     * 4. Log the undo
     *
     * @param game The GameMaster controlling the game
     */
    public abstract void undo(GameMaster game);

    /**
     * Returns a human-readable description of this action.
     * Used for logging.
     *
     * @return Description of the action
     */
    public abstract String describe();
    
    /**
     * Accepts a visitor for value evaluation (R3.2).
     *
     * @param visitor The visitor evaluating this action
     * @return The evaluated value of this action
     */
    public abstract double accept(ActionVisitor visitor);

    /**
     * Gets the player making this action.
     *
     * @return The player
     */
    public Player getPlayer() {
        return player;
    }
}