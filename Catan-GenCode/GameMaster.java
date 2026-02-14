package controller;

import model.*;
import util.Dice;
import util.RuleValidator;
import enums.ResourceType;
import java.util.ArrayList;
import java.util.List;

/**
 * Main controller for the Catan simulation.
 * 
 * Controls turn order, game loop, dice rolling, and victory checks.
 * This is the central orchestrator for the entire simulation.
 * 
 * Responsibilities:
 * - Initialize board and players (R1.1, R1.2)
 * - Run game rounds and turns (R1.4)
 * - Roll dice and produce resources (R1.3)
 * - Check victory conditions (R1.5)
 * - Log actions (R1.7)
 * - Enforce rules through RuleValidator (R1.6)
 */
public class GameMaster {
    private Board board;
    private List<Player> players;
    private Dice dice;
    private RuleValidator ruleValidator;
    private int currentRound;
    private int maxRounds;
    private static final int MAX_VICTORY_POINTS = 10;

    /**
     * Constructs a GameMaster for a simulation.
     * 
     * @param maxRounds Maximum number of rounds (R1.4: max 8192)
     */
    public GameMaster(int maxRounds) {
        this.board = new Board();
        this.players = new ArrayList<>();
        this.dice = new Dice();
        this.ruleValidator = new RuleValidator(board);
        this.currentRound = 0;
        this.maxRounds = Math.min(maxRounds, 8192); // R1.4: Max 8192 rounds
        
        // Initialize board (R1.1)
        board.initializeDefaultMap();
        
        // Create 4 players (R1.2)
        for (int i = 1; i <= 4; i++) {
            players.add(new Player(i));
        }
    }

    /**
     * Starts and runs the simulation.
     * 
     * R1.4, R1.5: Runs until max rounds or 10 VPs achieved.
     * 
     * Main game loop:
     * 1. Run a round (all players take turns)
     * 2. Check victory condition
     * 3. Repeat until termination condition met
     */
    public void startSimulation() {
        System.out.println("=== Starting Catan Simulation ===");
        System.out.println("Max Rounds: " + maxRounds);
        System.out.println("Players: " + players.size());
        System.out.println();

        while (currentRound < maxRounds) {
            currentRound++;
            runRound();

            // Check victory condition (R1.4, R1.5)
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

    /**
     * Runs a single round (all players take one turn).
     */
    public void runRound() {
        System.out.println("--- Round " + currentRound + " ---");
        
        for (Player player : players) {
            runTurn(player);
        }
        
        printRoundSummary();
    }

    /**
     * Runs a single player's turn.
     * 
     * Turn sequence:
     * 1. Roll dice
     * 2. Produce resources (if not 7)
     * 3. Player takes action (build or pass)
     * 
     * R1.7: Prints actions in specified format.
     */
    public void runTurn(Player player) {
        // Roll dice
        int roll = dice.roll();
        
        // Produce resources (skip if roll is 7 as per requirements)
        if (roll != 7) {
            produceResources(roll);
        }

        // Player takes their action
        player.takeTurn(this);
    }

    /**
     * Produces resources based on dice roll.
     * 
     * For each tile that produces on this roll:
     * - Find adjacent vertices with buildings
     * - Award resources to building owners
     * - Settlements get 1 resource, cities get 2
     */
    private void produceResources(int roll) {
        for (Tile tile : board.getTiles()) {
            if (tile.producesOnRoll(roll)) {
                ResourceType resource = tile.getResourceType();
                
                // Award resources to players with buildings on adjacent vertices
                for (Vertex vertex : tile.getAdjacentVertices()) {
                    if (vertex.isOccupied()) {
                        Building building = vertex.getBuilding();
                        Player owner = building.getOwner();
                        
                        // Settlements produce 1, cities produce 2
                        int amount = (building instanceof City) ? 2 : 1;
                        owner.collectResource(resource, amount);
                    }
                }
            }
        }
    }

    /**
     * Checks if any player has won.
     * 
     * R1.4: Victory at 10 VPs.
     * 
     * @return The winning player, or null if no winner yet
     */
    public Player checkVictory() {
        for (Player player : players) {
            if (player.getVictoryPoints() >= MAX_VICTORY_POINTS) {
                return player;
            }
        }
        return null;
    }

    /**
     * Prints a summary at the end of each round.
     * 
     * R1.7: Print victory points at end of each round.
     */
    public void printRoundSummary() {
        System.out.println("Victory Points:");
        for (Player player : players) {
            System.out.printf("  Player %d: %d VP (Cards: %d)%n", 
                            player.getId(), 
                            player.getVictoryPoints(),
                            player.getHand().totalCards());
        }
        System.out.println();
    }

    /**
     * Prints final standings at end of simulation.
     */
    private void printFinalStandings() {
        System.out.println("Final Standings:");
        players.sort((p1, p2) -> Integer.compare(p2.getVictoryPoints(), p1.getVictoryPoints()));
        
        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            System.out.printf("%d. Player %d: %d VP%n", 
                            i + 1, player.getId(), player.getVictoryPoints());
        }
    }

    /**
     * Prints an action to the console in the specified format.
     * 
     * R1.7: Format: [RoundNumber] / [PlayerID]: [Action]
     * 
     * @param player The player taking the action
     * @param action Description of the action
     */
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