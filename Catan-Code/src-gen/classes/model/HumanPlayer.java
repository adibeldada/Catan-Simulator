package classes.model;

import classes.controller.GameMaster;
import classes.enums.ResourceType;
import classes.moves.*;
import java.util.Scanner;
import java.util.logging.Logger;

/**
 * Represents a human-controlled player.
 * Uses console input to execute moves and enforces turn-based rules.
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
        LOGGER.info("Commands: Roll, Build settlement [id], Build road [id1, id2], Build city [id], List, Go");

        this.hasRolled = false;
        boolean turnActive = true;

        while (turnActive) {
            PlayerAction action = decideMove(game, false);
            if (action instanceof PassAction) {
                turnActive = false;
            } else if (action != null) {
                action.execute(game);
            }
        }
    }

    @Override
    protected PlayerAction decideMove(GameMaster game, boolean mustBuild) {
        LOGGER.info("> "); // Using logger for the input prompt
        String input = scanner.nextLine();
        String[] cmd = classes.util.CommandParser.parse(input);

        if (cmd == null || cmd.length == 0) {
            LOGGER.warning("Unknown command format.");
            return null;
        }

        String actionType = cmd[0];

        if (handleSystemCommands(actionType, game)) {
            return null;
        }

        if (!hasRolled && !"GO".equals(actionType)) {
            LOGGER.warning("Error: Must 'roll' first.");
            return null;
        }

        return parseGameAction(actionType, cmd, game);
    }

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

    // Extracted helpers to keep parseGameAction complexity low
    private PlayerAction handleSettle(String[] cmd, GameMaster game) {
        Vertex vSet = game.getBoard().getVertex(Integer.parseInt(cmd[1]));
        if (game.getRuleValidator().canBuildSettlement(this, vSet)) {
            return new BuildSettlementAction(this, vSet);
        }
        return null;
    }

    private PlayerAction handleRoad(String[] cmd, GameMaster game) {
        Vertex v1 = game.getBoard().getVertex(Integer.parseInt(cmd[1]));
        Vertex v2 = game.getBoard().getVertex(Integer.parseInt(cmd[2]));
        if (game.getRuleValidator().canBuildRoad(this, v1, v2)) {
            return new BuildRoadAction(this, v1, v2);
        }
        return null;
    }

    private PlayerAction handleCity(String[] cmd, GameMaster game) {
        Vertex vCity = game.getBoard().getVertex(Integer.parseInt(cmd[1]));
        if (game.getRuleValidator().canBuildCity(this, vCity)) {
            return new BuildCityAction(this, vCity);
        }
        return null;
    }

    public void discardHalf() {
        int toDiscard = getHand().totalCards() / 2;
        LOGGER.info(() -> "ROBBER ALERT! You must discard " + toDiscard + " cards.");
        LOGGER.info(() -> "Your current hand: " + getHand().toString());

        int discardedCount = 0;
        while (discardedCount < toDiscard) {
            final int currentStep = discardedCount + 1;
            LOGGER.info(() -> String.format("Enter resource type to discard (%d/%d): ", currentStep, toDiscard));
            
            String resStr = scanner.nextLine().toUpperCase().trim();
            
            try {
                ResourceType res = ResourceType.valueOf(resStr);
                
                if (getHand().getCount(res) > 0) {
                    getHand().remove(res, 1); 
                    LOGGER.info(() -> "Discarded 1 " + res + ". Hand: " + getHand().toString());
                    
                    // Increment counter ONLY on success
                    discardedCount++; 
                } else {
                    LOGGER.warning(() -> "Error: You don't have any " + res + ". Try again.");
                }
            } catch (IllegalArgumentException e) {
                LOGGER.warning("Error: Invalid resource name. Enter WOOD, BRICK, SHEEP, WHEAT, or ORE.");
            }
        }
    }
}