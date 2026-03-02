package classes.model;

import classes.controller.GameMaster;
import classes.moves.*;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HumanPlayer extends Player {
    private final Scanner scanner;
    private boolean hasRolled; // Prevents multiple rolls in one turn

    // Regex Constants (R3.3)
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

    @Override
    protected PlayerAction decideMove(GameMaster game, boolean mustBuild) {
        System.out.print("> ");
        String input = scanner.nextLine().trim();

        if (input.matches(REGEX_GO)) return new PassAction(this);
        if (input.matches(REGEX_LIST)) {
            System.out.println("Your hand: " + getHand().toString());
            return null;
        }

        // Handle Roll
        if (input.matches(REGEX_ROLL)) {
            if (hasRolled) {
                System.out.println("Error: You have already rolled this turn.");
            } else {
                game.rollAndDistribute(this);
                hasRolled = true;
            }
            return null;
        }

        // Standard rules: You must roll before you can build
        if (!hasRolled) {
            System.out.println("Error: You must 'roll' before taking building actions.");
            return null;
        }

        // 1. Build Settlement
        Matcher setMatch = Pattern.compile(REGEX_BUILD_SETTLEMENT).matcher(input);
        if (setMatch.matches()) {
            int id = Integer.parseInt(setMatch.group(1));
            Vertex v = game.getBoard().getVertex(id);
            if (game.getRuleValidator().canBuildSettlement(this, v)) {
                return new BuildSettlementAction(this, v);
            }
            System.out.println("Invalid Move: Lacking resources, location occupied, or distance rule violation.");
            return null;
        }

        // 2. Build Road (Requires comma! Format: build road 46, 47)
        Matcher roadMatch = Pattern.compile(REGEX_BUILD_ROAD).matcher(input);
        if (roadMatch.matches()) {
            int id1 = Integer.parseInt(roadMatch.group(1));
            int id2 = Integer.parseInt(roadMatch.group(2));
            Vertex v1 = game.getBoard().getVertex(id1);
            Vertex v2 = game.getBoard().getVertex(id2);
            if (game.getRuleValidator().canBuildRoad(this, v1, v2)) {
                return new BuildRoadAction(this, v1, v2);
            }
            System.out.println("Invalid Move: Cannot build road here (cost or connectivity).");
            return null;
        }

        // 3. Build City
        Matcher cityMatch = Pattern.compile(REGEX_BUILD_CITY).matcher(input);
        if (cityMatch.matches()) {
            int id = Integer.parseInt(cityMatch.group(1));
            Vertex v = game.getBoard().getVertex(id);
            if (game.getRuleValidator().canBuildCity(this, v)) {
                return new BuildCityAction(this, v);
            }
            System.out.println("Invalid Move: You must upgrade a settlement you own.");
            return null;
        }

        System.out.println("Unknown command or invalid format. Example: 'build road 46, 47'");
        return null;
    }

    public void discardHalf() {
        System.out.println("You must lose half your cards!");
        // R2.5 logic here
    }
}