package classes;

import classes.controller.GameMaster;
import classes.util.ConfigReader;
import classes.model.Player;
import classes.enums.ResourceType;
import classes.model.Road;
import classes.model.Settlement;
import classes.model.Vertex;
import classes.model.Tile;

import java.io.FileOutputStream;
import java.io.FileDescriptor;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Demonstrator class for the Catan Simulator.
 */
public class Demonstrator {
    private static final Logger LOGGER = Logger.getLogger(Demonstrator.class.getName());

    // Fix: Define a named static inner class instead of using double-brace initialization
    private static class WhiteTextHandler extends ConsoleHandler {
        WhiteTextHandler() {
            super();
            setOutputStream(new FileOutputStream(FileDescriptor.out));
        }
    }

    static {
        Logger rootLogger = Logger.getLogger("");
        for (java.util.logging.Handler handler : rootLogger.getHandlers()) {
            rootLogger.removeHandler(handler);
        }

        // Use the new class here
        ConsoleHandler whiteHandler = new WhiteTextHandler();

        whiteHandler.setFormatter(new Formatter() {
            @Override
            public String format(LogRecord logRecord) {
                return logRecord.getMessage() + System.lineSeparator();
            }
        });

        rootLogger.addHandler(whiteHandler);
    }

    public static void main(String[] args) {
        printWelcomeBanner();
        ConfigReader config = new ConfigReader("config.txt");
        printConfiguration(config);
        GameMaster game = new GameMaster(config.getMaxRounds());
        performSetupPhase(game);
        game.startSimulation();
        printTerminationBanner();
    }

    private static void printWelcomeBanner() {
        LOGGER.info("╔════════════════════════════════════════════════╗");
        LOGGER.info("║   SETTLERS OF CATAN - SIMULATOR DEMONSTRATOR   ║");
        LOGGER.info("╚════════════════════════════════════════════════╝");
        LOGGER.info("");
    }

    private static void printConfiguration(ConfigReader config) {
        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.info("Configuration loaded:");
            LOGGER.info(() -> String.format("  Max turns: %d", config.getMaxTurns()));
            LOGGER.info(() -> String.format("  Max rounds: %d", config.getMaxRounds()));
            LOGGER.info("");
        }
    }

    private static void performSetupPhase(GameMaster game) {
        List<Player> players = game.getPlayers();
        List<Integer> assignedVertices = new ArrayList<>();
        @SuppressWarnings("java:S2245")
        Random rand = new Random();
        
        for (int setupRound = 1; setupRound <= 2; setupRound++) {
            final int round = setupRound;
            if (LOGGER.isLoggable(Level.INFO)) {
                LOGGER.info(() -> String.format("--- Setup Round %d ---", round));
            }
            for (Player p : players) {
                placeInitialPieces(p, round, game, assignedVertices, rand);
            }
        }
        printStartingResources(players, game);
    }

    private static void placeInitialPieces(Player p, int round, GameMaster game, List<Integer> assigned, Random rand) {
        Vertex startVertex = findValidVertex(p, round, game, assigned, rand);
        Settlement s = new Settlement(p);
        startVertex.placeBuilding(s);
        p.addBuilding(s);
        p.addVictoryPoints(1);
        
        Vertex neighbor = startVertex.getAdjacentVertices().get(0);
        Road r = new Road(p, startVertex, neighbor);
        p.addRoad(r);
        game.getBoard().placeRoad(r);

        if (round == 2) {
            awardStartingResources(p, startVertex, game);
        }
        
        if (LOGGER.isLoggable(Level.INFO)) {
            game.logAction(p, String.format("Placed initial settlement at vertex %d and road to vertex %d (Setup Round %d)", 
                           startVertex.getId(), neighbor.getId(), round));
        }
    }

    private static Vertex findValidVertex(Player p, int round, GameMaster game, List<Integer> assigned, Random rand) {
        int attempts = 0;
        while (true) {
            attempts++;
            int randomIndex = rand.nextInt(54); 
            Vertex candidate = game.getBoard().getVertex(randomIndex);

            if (isValidPlacement(candidate, assigned, game) && 
                (round == 1 || hasEssentialTrio(p, candidate, game) || attempts > 200)) {
                assigned.add(randomIndex);
                return candidate;
            }
        }
    }

    private static boolean isValidPlacement(Vertex candidate, List<Integer> assigned, GameMaster game) {
        if (candidate.getAdjacentVertices().size() < 2) {
            return false;
        }
        for (int occupiedId : assigned) {
            Vertex occupied = game.getBoard().getVertex(occupiedId);
            if (candidate == occupied || candidate.getAdjacentVertices().contains(occupied)) {
                return false;
            }
        }
        return true;
    }

    private static void awardStartingResources(Player p, Vertex vertex, GameMaster game) {
        for (Tile tile : game.getBoard().getTiles()) {
            if (tile.getAdjacentVertices().contains(vertex)) {
                p.collectResource(tile.getResourceType(), 1);
            }
        }
    }

    private static void printStartingResources(List<Player> players, GameMaster game) {
        LOGGER.info("");
        LOGGER.info("Initial placement complete. Starting cards:");
        for (Player p : players) {
            if (LOGGER.isLoggable(Level.INFO)) {
                LOGGER.info(() -> String.format("  Player %d: %d cards", p.getId(), p.getHand().totalCards()));
            }
        }
        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.info(() -> String.format("Vertex 0 adjacents: %d", game.getBoard().getVertex(0).getAdjacentVertices().size()));
        }
    }

    private static boolean hasEssentialTrio(Player p, Vertex candidate, GameMaster game) {
        Vertex firstVertex = p.getBuildingsBuilt().get(0).getLocation();
        List<ResourceType> current = getProducedResources(firstVertex, game);
        List<ResourceType> potential = getProducedResources(candidate, game);
        
        boolean hasWood = current.contains(ResourceType.WOOD) || potential.contains(ResourceType.WOOD);
        boolean hasBrick = current.contains(ResourceType.BRICK) || potential.contains(ResourceType.BRICK);
        boolean hasWheat = current.contains(ResourceType.WHEAT) || potential.contains(ResourceType.WHEAT);
        
        return hasWood && hasBrick && hasWheat;
    }

    private static List<ResourceType> getProducedResources(Vertex v, GameMaster game) {
        List<ResourceType> resources = new ArrayList<>();
        for (Tile t : game.getBoard().getTiles()) {
            if (t.getAdjacentVertices().contains(v)) {
                resources.add(t.getResourceType());
            }
        }
        return resources;
    }

    private static void printTerminationBanner() {
        LOGGER.info("");
        LOGGER.info("╔════════════════════════════════════════════════╗");
        LOGGER.info("║         DEMONSTRATION COMPLETE                 ║");
        LOGGER.info("╚════════════════════════════════════════════════╝");
    }

    public static void runCustomDemo(int maxRounds) {
        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.info(() -> String.format("Running custom demonstration with %d rounds...", maxRounds));
            LOGGER.info("");
        }
        GameMaster game = new GameMaster(maxRounds);
        game.startSimulation();
    }
}