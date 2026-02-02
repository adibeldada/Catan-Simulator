package CatanSimulatorDomainModel.catanUML.moves;

import CatanSimulatorDomainModel.catanUML.model.Player;
import CatanSimulatorDomainModel.catanUML.controller.GameMaster;
/*import CatanSimulatorDomainModel.catanUML.epresents passing without taking an action.
 * 
 * Cost: None
 * Effect: Player takes no action this turn
 * 
 * This move is selected when:
 * - Player cannot afford any buildings
 * - Player chooses not to build (random decision)
 * - No valid moves are available
 */
public class PassAction extends PlayerAction {

    /**
     * Constructs a PassMove.
     * 
     * @param player The player passing their turn
     */
    public PassAction(Player player) {
        super(player);
    }

    /**
     * Executes the pass action.
     * No game state changes occur.
     * Only logs that the player passed.
     */
    @Override
    public void execute(GameMaster game) {
        // No action taken
        game.logAction(player, describe());
    }

    /**
     * Returns a description of this move for logging.
     * 
     * @return Human-readable description
     */
    @Override
    public String describe() {
        return "Passed turn";
    }
}