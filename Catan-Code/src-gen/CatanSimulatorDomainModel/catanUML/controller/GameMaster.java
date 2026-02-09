package CatanSimulatorDomainModel.catanUML.controller;

import CatanSimulatorDomainModel.catanUML.model.*;
import CatanSimulatorDomainModel.catanUML.util.Dice;
import CatanSimulatorDomainModel.catanUML.util.RuleValidator;
import CatanSimulatorDomainModel.catanUML.enums.ResourceType;
import java.util.ArrayList;
import java.util.List;

/**
 * GameMaster for the Catan simulation.
 * * Controls turn order, game loop, dice rolling, and victory checks.
 */
public class GameMaster {
    private Board board;
    private List<Player> players;
    private Dice dice;
    private RuleValidator ruleValidator;
    private int currentRound;
    private int maxRounds;
    private static final int MAX_VICTORY_POINTS = 10;

    public GameMaster(int maxRounds) {
        this.board = new Board();
        this.players = new ArrayList<>();
        this.dice = new Dice();
        this.ruleValidator = new RuleValidator(board);
        this.currentRound = 0;
        this.maxRounds = Math.min(maxRounds, 8192); // R1.4: Max 8192 rounds
        
        board.initializeDefaultMap();
        
        for (int i = 1; i <= 4; i++) {
            players.add(new Player(i));
        }
    }

    public void startSimulation() {
        System.out.println("=== Starting Catan Simulation ===");
        System.out.println("Max Rounds: " + maxRounds);
        System.out.println("Players: " + players.size());
        System.out.println();

        while (currentRound < maxRounds) {
            currentRound++;
            runRound();

            Player winner = checkVictory();
            if (winner != null) {
                System.out.println();
                System.out.println("=== GAME OVER ===");
                System.out.println("Winner: Player " + winner.getId() + 
                                 " with " + winner.getVictoryPoints() + " victory points!");
                System.out.println("Total rounds played: " + currentRound);
                return;
            }
        }

        System.out.println();
        System.out.println("=== SIMULATION ENDED ===");
        System.out.println("Maximum rounds reached: " + maxRounds);
        printFinalStandings();
    }

    public void runRound() {
        System.out.println("--- Round " + currentRound + " ---");
        
        for (Player player : players) {
            runTurn(player);
        }
        
        printRoundSummary();
    }

    /**
     * Updated to display the dice roll for each player's turn.
     */
    public void runTurn(Player player) {
        int roll = dice.roll();
        
        // Print the dice roll to the console
        System.out.println("[Dice Roll]: " + roll);
        
        if (roll != 7) {
            produceResources(roll);
        }

        player.takeTurn(this);
    }

    private void produceResources(int roll) {
        for (Tile tile : board.getTiles()) {
            if (tile.producesOnRoll(roll)) {
                ResourceType resource = tile.getResourceType();
                
                for (Vertex vertex : tile.getAdjacentVertices()) {
                    if (vertex.isOccupied()) {
                        Buildings building = vertex.getBuilding();
                        Player owner = building.getOwner();
                        
                        int amount = (building instanceof City) ? 2 : 1;
                        owner.collectResource(resource, amount);
                    }
                }
            }
        }
    }

    public Player checkVictory() {
        for (Player player : players) {
            if (player.getVictoryPoints() >= MAX_VICTORY_POINTS) {
                return player;
            }
        }
        return null;
    }

    /**
     * Updated to show detailed resource counts for each player.
     */
    public void printRoundSummary() {
        System.out.println("Victory Points & Resource Breakdown:");
        for (Player player : players) {
            // Uses player.getHand().toString() to show the Wood/Brick/Wheat/Sheep/Ore breakdown
            System.out.printf("  Player %d: %d VP | Hand: %s%n", 
                            player.getId(), 
                            player.getVictoryPoints(),
                            player.getHand().toString());
        }
        System.out.println();
    }

    private void printFinalStandings() {
        System.out.println("Final Standings:");
        players.sort((p1, p2) -> Integer.compare(p2.getVictoryPoints(), p1.getVictoryPoints()));
        
        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            System.out.printf("%d. Player %d: %d VP%n", 
                            i + 1, player.getId(), player.getVictoryPoints());
        }
    }

    public void logAction(Player player, String action) {
        System.out.printf("[%d] / [Player %d]: %s%n", currentRound, player.getId(), action);
    }

    // Getters
    public Board getBoard() { return board; }
    public List<Player> getPlayers() { return players; }
    public Dice getDice() { return dice; }
    public RuleValidator getRuleValidator() { return ruleValidator; }
    public int getCurrentRound() { return currentRound; }
    public int getMaxRounds() { return maxRounds; }
}