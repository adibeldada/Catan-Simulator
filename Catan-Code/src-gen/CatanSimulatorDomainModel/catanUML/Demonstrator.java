package CatanSimulatorDomainModel.catanUML;

import CatanSimulatorDomainModel.catanUML.controller.GameMaster;
import CatanSimulatorDomainModel.catanUML.util.ConfigReader;
import CatanSimulatorDomainModel.catanUML.model.Player;
import CatanSimulatorDomainModel.catanUML.enums.ResourceType;
import CatanSimulatorDomainModel.catanUML.model.Road;
import CatanSimulatorDomainModel.catanUML.model.Settlement;
import CatanSimulatorDomainModel.catanUML.model.Vertex;
import CatanSimulatorDomainModel.catanUML.model.Tile;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Demonstrator class for the Catan Simulator.
 */
public class Demonstrator {
    // Initialize the Logger
    private static final Logger LOGGER = Logger.getLogger(Demonstrator.class.getName());

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

    public static void main(String[] args) {
        printWelcomeBanner();
        
        // 1. CONFIGURATION PHASE
        ConfigReader config = new ConfigReader("config.txt");
        printConfiguration(config);
        
        // 2. INITIALIZATION PHASE
        GameMaster game = new GameMaster(config.getMaxRounds());
        
        // 3. SETUP PHASE
        performSetupPhase(game);
        
        // 4. SIMULATION PHASE
        game.startSimulation();
        
        // 5. TERMINATION PHASE
        printTerminationBanner();
    }

    private static void printWelcomeBanner() {
        LOGGER.info("╔════════════════════════════════════════════════╗");
        LOGGER.info("║   SETTLERS OF CATAN - SIMULATOR DEMONSTRATOR   ║");
        LOGGER.info("╚════════════════════════════════════════════════╝");
        LOGGER.info("");
    }

    private static void printConfiguration(ConfigReader config) {
        LOGGER.info("Configuration loaded:");
        LOGGER.info("  Max turns: " + config.getMaxTurns());
        LOGGER.info("  Max rounds: " + config.getMaxRounds());
        LOGGER.info("");
    }

    private static void performSetupPhase(GameMaster game) {
        List<Player> players = game.getPlayers();
        List<Integer> assignedVertices = new ArrayList<>();
        @SuppressWarnings("java:S2245")
        Random rand = new Random();
        
        for (int setupRound = 1; setupRound <= 2; setupRound++) {
            LOGGER.info("--- Setup Round " + setupRound + " ---");
            for (Player p : players) {
                placeInitialPieces(p, setupRound, game, assignedVertices, rand);
            }
        }
        
        printStartingResources(players, game);
    }

    private static void placeInitialPieces(Player p, int round, GameMaster game, List<Integer> assigned, Random rand) {
        Vertex startVertex = findValidVertex(p, round, game, assigned, rand);
        
        // Place initial settlement
        Settlement s = new Settlement(p);
        startVertex.placeBuilding(s);
        p.addBuilding(s);
        p.addVictoryPoints(1);
        
        // Place initial road
        Vertex neighbor = startVertex.getAdjacentVertices().get(0);
        Road r = new Road(p, startVertex, neighbor);
        p.addRoad(r);
        game.getBoard().placeRoad(r);

        // Award resources for the second settlement
        if (round == 2) {
            awardStartingResources(p, startVertex, game);
        }
        
        game.logAction(p, String.format("Placed initial settlement at vertex %d and road to vertex %d (Setup Round %d)", 
                       startVertex.getId(), neighbor.getId(), round));
    }

    private static Vertex findValidVertex(Player p, int round, GameMaster game, List<Integer> assigned, Random rand) {
        int attempts = 0;
        while (true) {
            attempts++;
            int randomIndex = rand.nextInt(54); 
            Vertex candidate = game.getBoard().getVertex(randomIndex);

            // Merged if statements to reduce nesting and satisfy SonarQube S1066
            if (isValidPlacement(candidate, assigned, game) && 
               (round == 1 || hasEssentialTrio(p, candidate, game) || attempts > 200)) {
                assigned.add(randomIndex);
                return candidate;
            }
        }
    }

    private static boolean isValidPlacement(Vertex candidate, List<Integer> assigned, GameMaster game) {
        // R1.6 Invariant & Dead-zone check
        if (candidate.getAdjacentVertices().size() < 2) return false;

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
        LOGGER.info("\nInitial placement complete. Starting cards:");
        for (Player p : players) {
            LOGGER.info("  Player " + p.getId() + ": " + p.getHand().totalCards() + " cards");
        }
        LOGGER.info("Vertex 0 adjacents: " + game.getBoard().getVertex(0).getAdjacentVertices().size());
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
        LOGGER.info(String.format("Running custom demonstration with %d rounds...%n", maxRounds));
        GameMaster game = new GameMaster(maxRounds);
        game.startSimulation();
    }
}