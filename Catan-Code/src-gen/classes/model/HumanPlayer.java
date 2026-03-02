package classes.model;

import classes.controller.GameMaster;
import classes.moves.*;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * R2.1: Human player that reads input from the command line.
 */
public class HumanPlayer extends Player {

    private final Scanner scanner;

    // Regular Expression Patterns (Requirement R3.3)
    private static final String REGEX_ROLL = "(?i)^roll$";
    private static final String REGEX_GO = "(?i)^go$";
    private static final String REGEX_LIST = "(?i)^list$";
    private static final String REGEX_BUILD_SETTLEMENT = "(?i)^build\\s+settlement\\s+(\\d+)$";
    private static final String REGEX_BUILD_CITY = "(?i)^build\\s+city\\s+(\\d+)$";
    private static final String REGEX_BUILD_ROAD = "(?i)^build\\s+road\\s+(\\d+),\\s*(\\d+)$";

    public HumanPlayer(int id) {
        super(id);
        this.scanner = new Scanner(System.in);
    }

    /**
     * The main loop for the human turn.
     */
    @Override
    public void takeTurn(GameMaster game) {
        System.out.println("\n--- IT IS YOUR TURN (Player " + id + ") ---");
        System.out.println("Commands: Roll, Build [type] [id], List, Go");

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
     * R2.1 & R3.3: Uses Regex to parse the command line input.
     */
    @Override
    protected PlayerAction decideMove(GameMaster game, boolean mustBuild) {
        System.out.print("> ");
        String input = scanner.nextLine().trim();

        // 1. Check for "Go" (End turn)
        if (input.matches(REGEX_GO)) {
            return new PassAction(this);
        }

        // 2. Check for "List" (Show cards)
        if (input.matches(REGEX_LIST)) {
            System.out.println("Your hand: " + hand.toString());
            return null; // Return null so the loop asks for another command
        }

        // 3. Check for "Roll"
        if (input.matches(REGEX_ROLL)) {
            // Note: In your current GameMaster, dice are rolled automatically 
            // at the start of runTurn. You can either move dice rolling here 
            // or just inform the user the roll already happened.
            System.out.println("Dice already rolled for this turn.");
            return null;
        }

        // 4. Check for "Build Settlement [nodeId]"
        Matcher settlementMatcher = Pattern.compile(REGEX_BUILD_SETTLEMENT).matcher(input);
        if (settlementMatcher.matches()) {
            int nodeId = Integer.parseInt(settlementMatcher.group(1));
            Vertex v = game.getBoard().getVertex(nodeId);
            return new BuildSettlementAction(this, v);
        }

        // 5. Check for "Build City [nodeId]"
        Matcher cityMatcher = Pattern.compile(REGEX_BUILD_CITY).matcher(input);
        if (cityMatcher.matches()) {
            int nodeId = Integer.parseInt(cityMatcher.group(1));
            Vertex v = game.getBoard().getVertex(nodeId);
            return new BuildCityAction(this, v);
        }

        // 6. Check for "Build Road [from, to]"
        Matcher roadMatcher = Pattern.compile(REGEX_BUILD_ROAD).matcher(input);
        if (roadMatcher.matches()) {
            int fromId = Integer.parseInt(roadMatcher.group(1));
            int toId = Integer.parseInt(roadMatcher.group(2));
            Vertex v1 = game.getBoard().getVertex(fromId);
            Vertex v2 = game.getBoard().getVertex(toId);
            return new BuildRoadAction(this, v1, v2);
        }

        System.out.println("Invalid command. Format: 'Build settlement 5' or 'Build road 1, 2'");
        return null;
    }

    
    public void discardHalf() {
        System.out.println("You have more than 7 cards! You must discard half.");
        // For A2 simplification, you could just call the AI's random discard logic
        // or prompt the user to type which cards to lose.
    }
}