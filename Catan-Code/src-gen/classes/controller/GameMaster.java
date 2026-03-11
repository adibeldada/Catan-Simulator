package classes.controller;

import classes.model.*;
import classes.util.Dice;
import classes.util.RuleValidator;
import classes.util.LoggerUtil;
import classes.util.JsonStateExporter;
import classes.enums.ResourceType;
import java.util.*;
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
    private final Scanner scanner = new Scanner(System.in);

    public GameMaster(int maxRounds) {
        LoggerUtil.setupLogging(); 
        this.board = new Board();
        this.players = new ArrayList<>();
        this.dice = new Dice();
        this.ruleValidator = new RuleValidator(board);
        this.currentRound = 0;
        this.maxRounds = Math.min(maxRounds, 8192);
        
        board.initializeDefaultMap();
        players.add(new HumanPlayer(1)); 

        for (int i = 2; i <= 4; i++) {
            players.add(new AIPlayer(i));
        }
    }
    
    public void startSimulation() {
        LOGGER.info("=== Starting Catan Simulation ===");
        boolean hasHumanPlayer = players.stream().anyMatch(HumanPlayer.class::isInstance);

        while (currentRound < maxRounds) {
            currentRound++;
            LOGGER.info(() -> String.format("--- Round %d ---", currentRound));
            
            if (hasHumanPlayer) {
                runRound(); 
            } else {
                players.forEach(this::runTurn);
                printRoundSummary();
            }

            Player winner = checkVictory();
            if (winner != null) {
                LOGGER.info("=== GAME OVER ===");
                LOGGER.info(() -> String.format("Winner: Player %d with %d VP!", winner.getId(), winner.getVictoryPoints()));
                return;
            }
        }
        LOGGER.info("=== SIMULATION ENDED ===");
        printFinalStandings();
    }

    public void runRound() {
        for (Player player : players) {
            if (player instanceof AIPlayer) {
                waitForGoCommand(player.getId());
            }
            runTurn(player);
        }
        printRoundSummary();
    }

    public void runTurn(Player player) {
        player.takeTurn(this);
        JsonStateExporter.exportState(this.board, "../2aa4-2026-base/assignments/visualize/state.json");
    }

    public void rollAndDistribute(Player roller) {
        int roll = dice.roll();
        logAction(roller, "rolled " + roll);
        
        if (roll == 7) {
            handleRobberAction(roller);
        } else {
            produceResources(roll);
        }
    }

    private void handleRobberAction(Player roller) {
        LOGGER.info("A 7 was rolled! Robber activated.");
        discardExcessCards();
        Tile newTile = moveRobber();
        stealCard(roller, newTile);
    }

    private void discardExcessCards() {
        for (Player p : players) {
            int total = p.getHand().totalCards();
            if (total > 7) {
                if (p instanceof HumanPlayer) {
                    ((HumanPlayer) p).discardHalf();
                } else {
                    p.getHand().discardRandomCards(total / 2);
                }
                logAction(p, "discarded cards due to robber.");
            }
        }
    }

    private Tile moveRobber() {
        List<Tile> potentialTiles = new ArrayList<>(board.getTiles());
        potentialTiles.remove(board.getRobber().getCurrentTile());
        
        Tile newTile = potentialTiles.get(new Random().nextInt(potentialTiles.size()));
        board.getRobber().moveTo(newTile);

        LOGGER.info("Robber moved to " + newTile.toString());
        return newTile;
    }

    private void stealCard(Player roller, Tile tile) {
        Set<Player> qualifyingPlayers = new HashSet<>();
        for (Vertex v : tile.getAdjacentVertices()) {
            if (v.isOccupied() && v.getOwner() != roller) {
                qualifyingPlayers.add(v.getOwner());
            }
        }

        if (qualifyingPlayers.isEmpty()) {
            LOGGER.info("No qualifying players to steal from on " + tile.toString());
            return;
        }

        List<Player> victimList = new ArrayList<>(qualifyingPlayers);
        Player victim = victimList.get(new Random().nextInt(victimList.size()));
        ResourceType stolen = victim.getHand().removeRandomCard();
        
        if (stolen != null) {
            roller.collectResource(stolen, 1);
            logAction(roller, "stole a card from Player " + victim.getId());
        }
    }

    private void produceResources(int roll) {
        Tile robberTile = board.getRobber().getCurrentTile();
        for (Tile tile : board.getTiles()) {
            if (tile.producesOnRoll(roll) && !tile.equals(robberTile)) {
                distributeFromTile(tile);
            }
        }
    }

    private void distributeFromTile(Tile tile) {
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

    /**
     * Fixed: System.out replaced with LOGGER.info for Sonar S106 compliance.
     */
    private void waitForGoCommand(int nextPlayerId) {
        LOGGER.info(() -> String.format("%n[PAUSED] Ready for AI Player %d.", nextPlayerId));
        LOGGER.info("Type 'go' to proceed to the next agent's turn:");
        while (true) {
            String input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("go")) {
                break;
            }
            LOGGER.info("Waiting for 'go' command...");
        }
    }

    public Player checkVictory() {
        return players.stream()
                .filter(p -> p.getVictoryPoints() >= MAX_VICTORY_POINTS)
                .findFirst()
                .orElse(null);
    }

    public void logAction(Player player, String action) {
        LOGGER.info(() -> String.format("[%d] / [Player %d]: %s", currentRound, player.getId(), action));
    }

    public void printRoundSummary() {
        LOGGER.info("Summary:");
        for (Player player : players) {
            LOGGER.info(() -> String.format("  Player %d: %d VP | %s", 
                player.getId(), player.getVictoryPoints(), player.getHand().toString()));
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

    public Board getBoard() { return board; }
    public List<Player> getPlayers() { return players; }
    public RuleValidator getRuleValidator() { return ruleValidator; }
    public int getCurrentRound() { return currentRound; }
}