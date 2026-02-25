package CatanSimulatorDomainModel.catanUML.controller;

import CatanSimulatorDomainModel.catanUML.model.*;
import CatanSimulatorDomainModel.catanUML.util.Dice;
import CatanSimulatorDomainModel.catanUML.util.RuleValidator;
import CatanSimulatorDomainModel.catanUML.enums.ResourceType;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * GameMaster for the Catan simulation.
 * Controls turn order, game loop, dice rolling, and victory checks.
 */
public class GameMaster {
    // Initialize the Logger
    private static final Logger LOGGER = Logger.getLogger(GameMaster.class.getName());

    static {
        // Configure logger to output "white" text to Standard Out and remove metadata clutter
        Logger rootLogger = Logger.getLogger("");
        for (java.util.logging.Handler handler : rootLogger.getHandlers()) {
            rootLogger.removeHandler(handler);
        }

        ConsoleHandler whiteHandler = new ConsoleHandler() {
            {
                setOutputStream(System.out); // Redirects from System.err to System.out
            }
        };

        // Custom formatter to show ONLY the message (no timestamps or class names)
        whiteHandler.setFormatter(new Formatter() {
            @Override
            public String format(LogRecord record) {
                return record.getMessage() + System.lineSeparator();
            }
        });

        rootLogger.addHandler(whiteHandler);
    }

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
        LOGGER.info("=== Starting Catan Simulation ===");
        LOGGER.info(String.format("Max Rounds: %d", maxRounds));
        LOGGER.info(String.format("Players: %d", players.size()));
        LOGGER.info("");

        while (currentRound < maxRounds) {
            currentRound++;
            runRound();

            Player winner = checkVictory();
            if (winner != null) {
                LOGGER.info("");
                LOGGER.info("=== GAME OVER ===");
                LOGGER.info(String.format("Winner: Player %d with %d victory points!", 
                                           winner.getId(), winner.getVictoryPoints()));
                LOGGER.info(String.format("Total rounds played: %d", currentRound));
                return;
            }
        }

        LOGGER.info("");
        LOGGER.info("=== SIMULATION ENDED ===");
        LOGGER.info(String.format("Maximum rounds reached: %d", maxRounds));
        printFinalStandings();
    }

    public void runRound() {
        LOGGER.info(String.format("--- Round %d ---", currentRound));
        
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
        
        // Print the dice roll to the console via Logger
        LOGGER.info(String.format("[Dice Roll]: %d", roll));
        
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
        LOGGER.info("Victory Points & Resource Breakdown:");
        for (Player player : players) {
            // Uses player.getHand().toString() to show the Wood/Brick/Wheat/Sheep/Ore breakdown
            LOGGER.info(String.format("  Player %d: %d VP | Hand: %s", 
                                       player.getId(), 
                                       player.getVictoryPoints(),
                                       player.getHand().toString()));
        }
        LOGGER.info("");
    }

    private void printFinalStandings() {
        LOGGER.info("Final Standings:");
        players.sort((p1, p2) -> Integer.compare(p2.getVictoryPoints(), p1.getVictoryPoints()));
        
        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            LOGGER.info(String.format("%d. Player %d: %d VP", 
                                       i + 1, player.getId(), player.getVictoryPoints()));
        }
    }

    public void logAction(Player player, String action) {
        LOGGER.info(String.format("[%d] / [Player %d]: %s", currentRound, player.getId(), action));
    }

    // Getters
    public Board getBoard() { return board; }
    public List<Player> getPlayers() { return players; }
    public Dice getDice() { return dice; }
    public RuleValidator getRuleValidator() { return ruleValidator; }
    public int getCurrentRound() { return currentRound; }
    public int getMaxRounds() { return maxRounds; }
}