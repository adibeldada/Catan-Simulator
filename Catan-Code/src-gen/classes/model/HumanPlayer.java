package classes.model;

import classes.controller.GameMaster;
import classes.enums.ResourceType;
import classes.moves.*;
import java.util.Scanner;

/**
 * Represents a human-controlled player.
 * Uses console input to execute moves and enforces turn-based rules.
 */
public class HumanPlayer extends Player {
    private final Scanner scanner;
    private boolean hasRolled; // Prevents taking actions or ending turn before rolling (R2.4)

    public HumanPlayer(int id) {
        super(id);
        this.scanner = new Scanner(System.in);
    }

    @Override
    public void takeTurn(GameMaster game) {
        System.out.println("\n--- IT IS YOUR TURN (Player " + id + ") ---");
        System.out.println("Commands: Roll, Build settlement [id], Build road [id1, id2], Build city [id], List, Go");

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

    /**
     * Refactored: Complexity reduced by extracting system commands 
     * and mapping logic into helper methods.
     */
    @Override
    protected PlayerAction decideMove(GameMaster game, boolean mustBuild) {
        System.out.print("> ");
        String[] cmd = classes.util.CommandParser.parse(scanner.nextLine());

        if (cmd == null) {
            System.out.println("Unknown command format.");
            return null;
        }

        String actionType = cmd[0];

        // 1. Handle non-move commands (LIST, ROLL)
        if (handleSystemCommands(actionType, game)) {
            return null;
        }

        // 2. Validate turn state (must roll before game actions)
        if (!hasRolled && !"GO".equals(actionType)) {
            System.out.println("Error: Must 'roll' first.");
            return null;
        }

        // 3. Map input to Game Actions
        return parseGameAction(actionType, cmd, game);
    }

    /**
     * Handles commands that don't result in a move (List, Roll).
     * Returns true if the command was processed.
     */
    private boolean handleSystemCommands(String action, GameMaster game) {
        if ("LIST".equals(action)) {
            System.out.println("Your hand: " + getHand().toString());
            return true;
        }
        if ("ROLL".equals(action)) {
            if (hasRolled) {
                System.out.println("Error: Already rolled.");
            } else {
                game.rollAndDistribute(this);
                hasRolled = true;
            }
            return true;
        }
        return false;
    }

    /**
     * Maps the parsed string command to a PlayerAction object.
     */
    private PlayerAction parseGameAction(String action, String[] cmd, GameMaster game) {
        switch (action) {
            case "GO":
                return hasRolled ? new PassAction(this) : null;

            case "SETTLE":
                Vertex vSet = game.getBoard().getVertex(Integer.parseInt(cmd[1]));
                if (game.getRuleValidator().canBuildSettlement(this, vSet)) {
                    return new BuildSettlementAction(this, vSet);
                }
                break;

            case "ROAD":
                Vertex v1 = game.getBoard().getVertex(Integer.parseInt(cmd[1]));
                Vertex v2 = game.getBoard().getVertex(Integer.parseInt(cmd[2]));
                if (game.getRuleValidator().canBuildRoad(this, v1, v2)) {
                    return new BuildRoadAction(this, v1, v2);
                }
                break;

            case "CITY":
                Vertex vCity = game.getBoard().getVertex(Integer.parseInt(cmd[1]));
                if (game.getRuleValidator().canBuildCity(this, vCity)) {
                    return new BuildCityAction(this, vCity);
                }
                break;

            default:
                // Rule S131: Switch statements should have default clauses
                break;
        }
        return null;
    }

    public void discardHalf() {
        int toDiscard = getHand().totalCards() / 2;
        System.out.println("ROBBER ALERT! You must discard " + toDiscard + " cards.");
        System.out.println("Your current hand: " + getHand().toString());

        for (int i = 0; i < toDiscard; i++) {
            System.out.print("Enter resource type to discard (" + (i + 1) + "/" + toDiscard + "): ");
            String resStr = scanner.nextLine().toUpperCase().trim();
            try {
                ResourceType res = ResourceType.valueOf(resStr);
                if (getHand().getCount(res) > 0) {
                    getHand().remove(res, 1); 
                    System.out.println("Discarded 1 " + res + ". Hand: " + getHand().toString());
                } else {
                    System.out.println("Error: You don't have any " + res + ". Try again.");
                    i--;
                }
            } catch (IllegalArgumentException e) {
                System.out.println("Error: Invalid resource name. Enter WOOD, BRICK, SHEEP, WHEAT, or ORE.");
                i--; 
            }
        }
    }
}