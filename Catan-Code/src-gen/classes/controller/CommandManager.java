package classes.controller;
import classes.moves.PlayerAction;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.logging.Logger;

public class CommandManager {
    private static final Logger LOGGER = Logger.getLogger(CommandManager.class.getName());
    private final Deque<PlayerAction> undoStack = new ArrayDeque<>();
    private final Deque<PlayerAction> redoStack = new ArrayDeque<>();

    public void executeCommand(PlayerAction action, GameMaster game) {
        action.execute(game);
        undoStack.push(action);
        redoStack.clear();
    }

    public boolean undo(GameMaster game) {
        if (undoStack.isEmpty()) {
            LOGGER.warning("Nothing to undo.");
            return false;
        }
        PlayerAction action = undoStack.pop();
        action.undo(game);
        redoStack.push(action);
        LOGGER.info(() -> "Undid: " + action.describe());
        return true;
    }

    public boolean redo(GameMaster game) {
        if (redoStack.isEmpty()) {
            LOGGER.warning("Nothing to redo.");
            return false;
        }
        PlayerAction action = redoStack.pop();
        action.execute(game);
        undoStack.push(action);
        LOGGER.info(() -> "Redid: " + action.describe());
        return true;
    }

    public void clearHistory() {
        undoStack.clear();
        redoStack.clear();
    }

    public boolean canUndo() { return !undoStack.isEmpty(); }
    public boolean canRedo() { return !redoStack.isEmpty(); }

    /** @return the top action on the undo stack without removing it, or null if empty */
    public PlayerAction peekUndo() {
        return undoStack.isEmpty() ? null : undoStack.peek();
    }
}