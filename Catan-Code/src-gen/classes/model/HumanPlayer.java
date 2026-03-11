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
       String input = scanner.nextLine();
       
       // Ask the parser to break down the string
       String[] cmd = classes.util.CommandParser.parse(input);

       if (cmd == null) {
           System.out.println("Unknown command format.");
           return null;
       }

       String action = cmd[0];

       // 1. Logic-only handling (No regex here!)
       if (action.equals("LIST")) {
           System.out.println("Your hand: " + getHand().toString());
           return null;
       }

       if (action.equals("ROLL")) {
           if (hasRolled) {
               System.out.println("Error: Already rolled.");
           } else {
               game.rollAndDistribute(this);
               hasRolled = true;
           }
           return null;
       }

       // Check roll requirement for game actions
       if (!hasRolled && !action.equals("GO")) {
           System.out.println("Error: Must 'roll' first.");
           return null;
       }

       // 2. Map the parsed arguments to Game Actions
       switch (action) {
           case "GO":
               if (!hasRolled) return null;
               return new PassAction(this);

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

