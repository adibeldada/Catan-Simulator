package classes.moves;

import classes.controller.GameMaster;
import classes.enums.ResourceType;
import classes.model.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents the action of rolling the dice and distributing resources.
 *
 * Command pattern (R3.1): execute() rolls and distributes;
 * undo() reverses all resource changes, robber movement, and stolen cards.
 */
public class RollAction extends PlayerAction {

    /** Snapshot of every player's resource counts before the roll. */
    private final Map<Player, int[]> resourcesBefore = new HashMap<>();

    /** The robber's tile before the roll (in case of a 7). */
    private Tile robberTileBefore;

    /** Tracks whether the snapshot has been taken yet. */
    private boolean snapshotTaken = false;

    public RollAction(Player player) {
        super(player);
    }

    @Override
    public void execute(GameMaster game) {
        // Only snapshot on the very first execute.
        // On redo, we reuse the same snapshot so undo always
        // restores back to the original pre-roll state.
        if (!snapshotTaken) {
            for (Player p : game.getPlayers()) {
                ResourceHand h = p.getHand();
                resourcesBefore.put(p, new int[]{
                    h.getWood(), h.getBrick(), h.getWheat(), h.getSheep(), h.getOre()
                });
            }
            robberTileBefore = game.getBoard().getRobber().getCurrentTile();
            snapshotTaken = true;
        }

        game.rollAndDistribute(player);
    }

    @Override
    public void undo(GameMaster game) {
        // Restore every player's hand to the pre-roll snapshot
        for (Player p : game.getPlayers()) {
            int[] before = resourcesBefore.get(p);
            if (before == null) continue;
            ResourceHand h = p.getHand();
            h.set(ResourceType.WOOD,  before[0]);
            h.set(ResourceType.BRICK, before[1]);
            h.set(ResourceType.WHEAT, before[2]);
            h.set(ResourceType.SHEEP, before[3]);
            h.set(ResourceType.ORE,   before[4]);
        }
        // Restore the robber to where it was before the roll
        game.getBoard().getRobber().moveTo(robberTileBefore);
        game.logAction(player, "Undid: " + describe());
    }
    
    @Override
    public double accept(ActionVisitor visitor) {
        return visitor.visit(this);
    }

    @Override
    public String describe() {
        return "Rolled dice";
    }
}