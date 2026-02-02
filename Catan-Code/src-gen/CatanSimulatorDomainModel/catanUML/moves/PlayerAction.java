package CatanSimulatorDomainModel.catanUML.moves;

import CatanSimulatorDomainModel.catanUML.model.Player;
import CatanSimulatorDomainModel.catanUML.controller.GameMaster;
/**
 * Abstract base class for player moves/actions.
 * 
 * Represents the Command pattern for player actions.
 * Each concrete move type (road, settlement, city, pass) extends this class.
 * 
 * The execute() method performs the action and updates game state.
 * The describe() method provides a human-readable description for logging.
 */
public abstract class PlayerAction {
    protected Player player;

    /**
     * Constructs a Move for the specified player.
     * 
     * @param player The player making this move
     */
    public PlayerAction(Player player) {
        this.player = player;
    }

    /**
     * Executes this move in the context of the game.
     * Must be implemented by concrete move types.
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
     * Returns a human-readable description of this move.
     * Used for logging in the format: [Round] / [Player]: [Action]
     * 
     * @return Description of the move
     */
    public abstract String describe();

    /**
     * Gets the player making this move.
     * 
     * @return The player
     */
    public Player getPlayer() {
        return player;
    }
}