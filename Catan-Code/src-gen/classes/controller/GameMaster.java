package classes.controller;

import classes.model.*;
import classes.util.Dice;
import classes.util.RuleValidator;
import classes.util.LoggerUtil;
import classes.enums.ResourceType;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Logger;

/**
 * Main controller for the Catan simulation.
 * Handles turn order, game loops, and resource distribution.
 */
public class GameMaster {
    private static final Logger LOGGER = Logger.getLogger(GameMaster.class.getName());

    private Board board;
    private List<Player> players;
    private Dice dice;
    private RuleValidator ruleValidator;
    private int currentRound;
    private int maxRounds;
    private static final int MAX_VICTORY_POINTS = 10;

    public GameMaster(int maxRounds) {
        LoggerUtil.setupLogging(); 

        this.board = new Board();
        this.players = new ArrayList<>();
        this.dice = new Dice();
        this.ruleValidator = new RuleValidator(board);
        this.currentRound = 0;
        this.maxRounds = Math.min(maxRounds, 8192);
        
        board.initializeDefaultMap();

        // R2.1: Initialize 1 Human and 3 AI agents
        players.add(new HumanPlayer(1)); 
        for (int i = 2; i <= 4; i++) {
            players.add(new AIPlayer(i));
        }
    }

    /**
     * Main game loop that runs until a winner is found or max rounds are reached.
     */
    public void startSimulation() {
        LOGGER.info("=== Starting Catan Simulation ===");
        LOGGER.info(() -> String.format("Max Rounds: %d", maxRounds));
        LOGGER.info(() -> String.format("Players: %d", players.size()));
        LOGGER.info("");

        while (currentRound < maxRounds) {
            currentRound++;
            runRound();
            Player winner = checkVictory();
            if (winner != null) {
                LOGGER.info("");
                LOGGER.info("=== GAME OVER ===");
                LOGGER.info(() -> String.format("Winner: Player %d with %d victory points!", 
                                           winner.getId(), winner.getVictoryPoints()));
                return;
            }
        }

        LOGGER.info("=== SIMULATION ENDED ===");
        printFinalStandings();
    }

    /**
     * Iterates through all players to let them take their turns.
     */
    public void runRound() {
        LOGGER.info(() -> String.format("--- Round %d ---", currentRound));
        for (Player player : players) {
            
            // R2.4: Step forward functionality
            // Waits for "go" command before proceeding to an AI agent's turn
            if (player instanceof AIPlayer) {
                waitForGoCommand(player.getId());
            }

            runTurn(player);
        }
        printRoundSummary();
    }

    /**
     * Delegates the turn logic to the player object.
     */
    public void runTurn(Player player) {
        // The player class now decides when to call rollAndDistribute()
        player.takeTurn(this);
    }

    /**
     * Logic for rolling dice and distributing resources.
     * This is called by players during their takeTurn() method.
     */
    public void rollAndDistribute(Player player) {
        int roll = dice.roll();
        logAction(player, "rolled " + roll);

        if (roll != 7) {
            produceResources(roll);
        } else {
            // R2.5: Robber logic placeholder
            LOGGER.info("A 7 was rolled! Robber activated.");
        }
    }

    /**
     * R2.4: Pauses the console and waits for the user to type 'go'.
     */
    private void waitForGoCommand(int nextPlayerId) {
        System.out.println("\n[PAUSED] Ready for AI Player " + nextPlayerId + ".");
        System.out.println("Type 'go' to proceed to the next agent's turn:");
        
        Scanner sc = new Scanner(System.in);
        while (true) {
            String input = sc.nextLine().trim();
            if (input.equalsIgnoreCase("go")) {
                break;
            }
            System.out.println("Waiting for 'go' command...");
        }
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

    public void logAction(Player player, String action) {
        LOGGER.info(() -> String.format("[%d] / [Player %d]: %s", currentRound, player.getId(), action));
    }

    public void printRoundSummary() {
        LOGGER.info("Summary:");
        for (Player player : players) {
            LOGGER.info(() -> String.format("  Player %d: %d VP | %s", 
                                       player.getId(), 
                                       player.getVictoryPoints(),
                                       player.getHand().toString()));
        }
        LOGGER.info("");
    }

    private void printFinalStandings() {
        players.sort((p1, p2) -> Integer.compare(p2.getVictoryPoints(), p1.getVictoryPoints()));
        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            LOGGER.info((i + 1) + ". Player " + p.getId() + ": " + p.getVictoryPoints() + " VP");
        }
    }

    // Getters
    public Board getBoard() { return board; }
    public List<Player> getPlayers() { return players; }
    public RuleValidator getRuleValidator() { return ruleValidator; }
    public int getCurrentRound() { return currentRound; }
}