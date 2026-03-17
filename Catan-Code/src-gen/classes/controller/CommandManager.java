package classes.controller;

import classes.moves.PlayerAction;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.logging.Logger;

/**
 * Manages the undo/redo history of executed player actions.
 *
 * This is the Invoker in the Command design pattern (R3.1).
 * It maintains two stacks:
 *   - undoStack: actions that have been executed and can be undone
 *   - redoStack: actions that have been undone and can be re-executed
 *
 * When a new action is executed via executeCommand(), the redo stack is
 * cleared because the history has branched — you cannot redo after a new move.
 */
public class CommandManager {
    private static final Logger LOGGER = Logger.getLogger(CommandManager.class.getName());

    private final Deque<PlayerAction> undoStack = new ArrayDeque<>();
    private final Deque<PlayerAction> redoStack = new ArrayDeque<>();

    /**
     * Executes an action, records it on the undo stack, and clears the redo stack.
     *
     * @param action The action to execute
     * @param game   The current game context
     */
    public void executeCommand(PlayerAction action, GameMaster game) {
        action.execute(game);
        undoStack.push(action);
        redoStack.clear(); // New action branches history; redo is no longer valid
    }

    /**
     * Undoes the most recently executed action.
     * Moves it from the undo stack onto the redo stack.
     *
     * @param game The current game context
     * @return true if an action was undone, false if there is nothing to undo
     */
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

    /**
     * Re-executes the most recently undone action.
     * Moves it from the redo stack back onto the undo stack.
     *
     * @param game The current game context
     * @return true if an action was redone, false if there is nothing to redo
     */
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

    /**
     * Clears both stacks. Called at the start of each new turn to prevent
     * undo/redo across turn boundaries.
     */
    public void clearHistory() {
        undoStack.clear();
        redoStack.clear();
    }

    /** @return true if there is at least one action that can be undone */
    public boolean canUndo() { return !undoStack.isEmpty(); }

    /** @return true if there is at least one action that can be redone */
    public boolean canRedo() { return !redoStack.isEmpty(); }
}