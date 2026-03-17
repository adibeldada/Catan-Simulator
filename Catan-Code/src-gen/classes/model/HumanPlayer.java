package classes.model;

import classes.controller.GameMaster;
import classes.enums.ResourceType;
import classes.moves.*;
import java.util.Scanner;
import java.util.logging.Logger;

/**
 * Represents a human-controlled player.
 * Uses console input to execute moves and enforces turn-based rules.
 *
 * R3.1: Build actions are routed through GameMaster.executeAction() so they
 * are recorded by the CommandManager. The human player can type "undo" or
 * "redo" at any point in their turn to reverse or reapply a build action.
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
                turnActive = false;
            } else if (action != null) {
                // R3.1: Route through CommandManager so the action is recorded
                game.executeAction(action);
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

        // Handle system / meta commands (no roll required)
        if (handleSystemCommands(actionType, game)) {
            return null;
        }

        // Roll must happen before build commands
        if (!hasRolled && !"GO".equals(actionType)) {
            LOGGER.warning("Error: Must 'roll' first.");
            return null;
        }

        return parseGameAction(actionType, cmd, game);
    }

    /**
     * Handles commands that don't produce a PlayerAction object.
     * Returns true if the command was consumed here.
     */
    private boolean handleSystemCommands(String action, GameMaster game) {
        if ("LIST".equals(action)) {
            LOGGER.info(() -> "Your hand: " + getHand().toString());
            return true;
        }
        if ("ROLL".equals(action)) {
            if (hasRolled) {
                LOGGER.warning("Error: Already rolled.");
            } else {
                game.rollAndDistribute(this);
                hasRolled = true;
            }
            return true;
        }
        // R3.1: Undo / Redo are handled here so they never enter the action pipeline
        if ("UNDO".equals(action)) {
            if (!hasRolled) {
                LOGGER.warning("Error: Must roll before you can undo a build action.");
            } else {
                game.undoLastAction();
            }
            return true;
        }
        if ("REDO".equals(action)) {
            if (!hasRolled) {
                LOGGER.warning("Error: Must roll before you can redo a build action.");
            } else {
                game.redoLastAction();
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