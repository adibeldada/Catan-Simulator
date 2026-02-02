package moves;

import model.Player;
import controimport CatanSimulatorDomainModel.catanUML.CatanSimulatorDimport CatanSimulatorDomainModel.catanUML.del.caimport CatanSimulatorDomainModel.catanUML.CatanSimulatorDimport CatanSimulatorDomainModel.caimport CatanSimulatorDomainModel.catanUML.del.caimport CatanSimulatorDomainModel.catanUML.meMaster;

/*import CatanSimulatorDomainModel.caimport CatanSimulatorDomainModel.catanUML.epresents passing without taking an action.
 * 
 * Cost: None
 * Effect: Player takes no action this turn
 * 
 * This move is selected when:
 * - Player cannot afford any buildings
 * - Player chooses not to build (random decision)
 * - No valid moves are available
 */
public class PassMove extends Move {

    /**
     * Constructs a PassMove.
     * 
     * @param player The player passing their turn
     */
    public PassMove(Player player) {
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