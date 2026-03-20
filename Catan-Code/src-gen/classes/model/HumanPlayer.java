package classes.model;

import classes.controller.GameMaster;
import classes.enums.ResourceType;
import classes.moves.*;
import java.util.Scanner;
import java.util.logging.Logger;

/**
 * Represents a human-controlled player.
 *
 * R3.1: ALL actions (roll, build, pass) are routed through
 * GameMaster.executeAction() so they are recorded by the CommandManager
 * and can be undone or redone within the same turn.
 */
public class HumanPlayer extends Player {
    private static final Logger LOGGER = Logger.getLogger(HumanPlayer.class.getName());
    private final Scanner scanner;
    private boolean hasRolled;

    public HumanPlayer(int id) {
        super(id);
        this.scanner = new Scanner(System.in);
    }

    @Override
    public void takeTurn(GameMaster game) {
        LOGGER.info(() -> String.format("%n--- IT IS YOUR TURN (Player %d) ---", id));
        LOGGER.info("Commands: roll | build settlement <id> | build road <id1>,<id2> | build city <id> | undo | redo | list | go");

        this.hasRolled = false;
        boolean turnActive = true;

        while (turnActive) {
            PlayerAction action = decideMove(game, false);
            if (action instanceof PassAction) {
                game.executeAction(action);
                // After logging the pass, ask for confirmation
                LOGGER.info("Turn ended. Type 'undo' to take it back, or press Enter to confirm:");
                String confirm = scanner.nextLine().trim();
                if (confirm.equalsIgnoreCase("undo")) {
                    game.undoLastAction();
                    hasRolled = true; // roll is still on the stack
                } else {
                    turnActive = false;
                }
            } else if (action != null) {
                game.executeAction(action);
                if (action instanceof RollAction) {
                    hasRolled = true;
                }
            }
        }
    }

    @Override
    protected PlayerAction decideMove(GameMaster game, boolean mustBuild) {
        LOGGER.info("> ");
        String input = scanner.nextLine();
        String[] cmd = classes.util.CommandParser.parse(input);

        if (cmd == null || cmd.length == 0) {
            LOGGER.warning("Unknown command format.");
            return null;
        }

        String actionType = cmd[0];

        // Handle meta commands that don't produce actions
        if (handleMetaCommands(actionType, game)) {
            return null;
        }

        // ROLL returns a RollAction — no prior roll required
        if ("ROLL".equals(actionType)) {
            if (hasRolled) {
                LOGGER.warning("Error: Already rolled.");
                return null;
            }
            return new RollAction(this);
        }

        // Everything else requires a prior roll
        if (!hasRolled && !"GO".equals(actionType)) {
            LOGGER.warning("Error: Must 'roll' first.");
            return null;
        }

        return parseGameAction(actionType, cmd, game);
    }

    /**
     * Handles LIST, UNDO, REDO — commands that consume input but produce no action.
     * Returns true if the command was consumed here.
     */
    private boolean handleMetaCommands(String action, GameMaster game) {
        if ("LIST".equals(action)) {
            LOGGER.info(() -> "Your hand: " + getHand().toString());
            return true;
        }
        if ("UNDO".equals(action)) {
            boolean undid = game.undoLastAction();
            // The roll is always the first action pushed, so the stack being
            // empty after an undo means the roll was just undone.
            if (undid && !game.getCommandManager().canUndo()) {
                hasRolled = false;
            }
            return true;
        }
        if ("REDO".equals(action)) {
            boolean redid = game.redoLastAction();
            if (redid) {
                // If the top of the undo stack is now a RollAction,
                // the roll was just redone — mark hasRolled accordingly.
                PlayerAction top = game.getCommandManager().peekUndo();
                if (top instanceof RollAction) {
                    hasRolled = true;
                }
            }
            return true;
        }
        return false;
    }

    private PlayerAction parseGameAction(String action, String[] cmd, GameMaster game) {
        switch (action) {
            case "GO":
                if (hasRolled) {
                    return new PassAction(this);
                } else {
                    LOGGER.warning("Error: You cannot end your turn without rolling first!");
                    return null;
                }
            case "SETTLE":
                return handleSettle(cmd, game);
            case "ROAD":
                return handleRoad(cmd, game);
            case "CITY":
                return handleCity(cmd, game);
            default:
                break;
        }
        return null;
    }

    private PlayerAction handleSettle(String[] cmd, GameMaster game) {
        Vertex v = game.getBoard().getVertex(Integer.parseInt(cmd[1]));
        if (game.getRuleValidator().canBuildSettlement(this, v)) {
            return new BuildSettlementAction(this, v);
        }
        LOGGER.warning("Cannot build settlement there.");
        return null;
    }

    private PlayerAction handleRoad(String[] cmd, GameMaster game) {
        Vertex v1 = game.getBoard().getVertex(Integer.parseInt(cmd[1]));
        Vertex v2 = game.getBoard().getVertex(Integer.parseInt(cmd[2]));
        if (game.getRuleValidator().canBuildRoad(this, v1, v2)) {
            return new BuildRoadAction(this, v1, v2);
        }
        LOGGER.warning("Cannot build road there.");
        return null;
    }

    private PlayerAction handleCity(String[] cmd, GameMaster game) {
        Vertex v = game.getBoard().getVertex(Integer.parseInt(cmd[1]));
        if (game.getRuleValidator().canBuildCity(this, v)) {
            return new BuildCityAction(this, v);
        }
        LOGGER.warning("Cannot build city there.");
        return null;
    }

    public void discardHalf() {
        int toDiscard = getHand().totalCards() / 2;
        LOGGER.info(() -> "ROBBER ALERT! You must discard " + toDiscard + " cards.");
        LOGGER.info(() -> "Your current hand: " + getHand().toString());

        int discarded = 0;
        while (discarded < toDiscard) {
            final int step = discarded + 1;
            LOGGER.info(() -> String.format("Enter resource type to discard (%d/%d): ", step, toDiscard));
            String resStr = scanner.nextLine().toUpperCase().trim();
            try {
                ResourceType res = ResourceType.valueOf(resStr);
                if (getHand().getCount(res) > 0) {
                    getHand().remove(res, 1);
                    LOGGER.info(() -> "Discarded 1 " + res + ". Hand: " + getHand().toString());
                    discarded++;
                } else {
                    LOGGER.warning(() -> "Error: You don't have any " + res + ". Try again.");
                }
            } catch (IllegalArgumentException e) {
                LOGGER.warning("Error: Invalid resource name. Enter WOOD, BRICK, SHEEP, WHEAT, or ORE.");
            }
        }
    }
}