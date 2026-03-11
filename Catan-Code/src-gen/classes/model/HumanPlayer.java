package classes.model;
import classes.controller.GameMaster;
import classes.enums.ResourceType;
import classes.moves.*;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
* Represents a human-controlled player.
* Uses console input to execute moves and enforces turn-based rules.
*/
public class HumanPlayer extends Player {
   private final Scanner scanner;
   private boolean hasRolled; // Prevents taking actions or ending turn before rolling (R2.4)
   // Regex Constants for command parsing (R3.3)
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
           // If decideMove returns a PassAction, end the loop to conclude the turn
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
       // 1. Handle List (Can be done anytime)
       if (input.matches(REGEX_LIST)) {
           System.out.println("Your hand: " + getHand().toString());
           return null; // Return null to stay in the turn loop
       }
       // 2. Handle Roll
       if (input.matches(REGEX_ROLL)) {
           if (hasRolled) {
               System.out.println("Error: You have already rolled this turn.");
           } else {
               game.rollAndDistribute(this);
               hasRolled = true;
           }
           return null;
       }
       // 3. Handle Go (End Turn) - Enforce Roll Requirement
       if (input.matches(REGEX_GO)) {
           if (!hasRolled) {
               System.out.println("Error: You must 'roll' before ending your turn.");
               return null;
           }
           return new PassAction(this); // Signal turn completion
       }
       // 4. Enforce roll requirement for all subsequent building actions
       if (!hasRolled) {
           System.out.println("Error: You must 'roll' before taking building actions.");
           return null;
       }
       // 5. Build Settlement
       Matcher setMatch = Pattern.compile(REGEX_BUILD_SETTLEMENT).matcher(input);
       if (setMatch.matches()) {
           int vertexId = Integer.parseInt(setMatch.group(1));
           Vertex v = game.getBoard().getVertex(vertexId);
           if (game.getRuleValidator().canBuildSettlement(this, v)) {
               return new BuildSettlementAction(this, v);
           }
           System.out.println("Invalid Move: Check resources, occupancy, or the distance rule.");
           return null;
       }
       // 6. Build Road
       Matcher roadMatch = Pattern.compile(REGEX_BUILD_ROAD).matcher(input);
       if (roadMatch.matches()) {
           int id1 = Integer.parseInt(roadMatch.group(1));
           int id2 = Integer.parseInt(roadMatch.group(2));
           Vertex v1 = game.getBoard().getVertex(id1);
           Vertex v2 = game.getBoard().getVertex(id2);
           if (game.getRuleValidator().canBuildRoad(this, v1, v2)) {
               return new BuildRoadAction(this, v1, v2);
           }
           System.out.println("Invalid Move: Cannot build road here (check cost or connectivity).");
           return null;
       }
       // 7. Build City
       Matcher cityMatch = Pattern.compile(REGEX_BUILD_CITY).matcher(input);
       if (cityMatch.matches()) {
           int cityVertexId = Integer.parseInt(cityMatch.group(1));
           Vertex v = game.getBoard().getVertex(cityVertexId);
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
       int toDiscard = getHand().totalCards() / 2;
       System.out.println("ROBBER ALERT! You must discard " + toDiscard + " cards.");
       System.out.println("Your current hand: " + getHand().toString());
       
       for (int i = 0; i < toDiscard; i++) {
           System.out.print("Enter resource type to discard (" + (i + 1) + "/" + toDiscard + "): ");
           String resStr = scanner.nextLine().toUpperCase().trim();
           try {
               ResourceType res = ResourceType.valueOf(resStr);
               
               // Using the new getCount helper to verify availability
               if (getHand().getCount(res) > 0) {
                   // Using the existing 'remove' method correctly
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

