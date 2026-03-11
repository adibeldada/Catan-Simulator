package classes;

import classes.controller.GameMaster;
import classes.util.ConfigReader;
import classes.util.LoggerUtil;
import classes.model.Player;
import classes.enums.ResourceType;
import classes.model.Road;
import classes.model.Settlement;
import classes.model.Vertex;
import classes.model.Tile;
import classes.util.JsonStateExporter;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Demonstrator class for the Catan Simulator.
 */
public class Demonstrator {
    private static final Logger LOGGER = Logger.getLogger(Demonstrator.class.getName());

    public static void main(String[] args) {
        LoggerUtil.setupLogging();
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
                JsonStateExporter.exportState(game.getBoard(), "../2aa4-2026-base/assignments/visualize/state.json");
            }
        }
        JsonStateExporter.exportState(game.getBoard(), "../2aa4-2026-base/assignments/visualize/state.json");
        printStartingResources(players, game);
    }

    private static void placeInitialPieces(Player p, int round, GameMaster game, List<Integer> assigned, Random rand) {
        Vertex startVertex;
        Vertex neighbor;

        if (p instanceof classes.model.HumanPlayer) {
            Scanner scanner = new Scanner(System.in);
            startVertex = handleHumanSettlementPlacement(p, round, game, assigned, scanner);
            assigned.add(startVertex.getId());
            neighbor = handleHumanRoadPlacement(p, round, startVertex, game, scanner);
        } else {
            startVertex = findValidVertex(p, round, game, assigned, rand);
            assigned.add(startVertex.getId());
            neighbor = startVertex.getAdjacentVertices().get(0);
        }

        executePlacement(p, startVertex, neighbor, game);

        if (round == 2) {
            awardStartingResources(p, startVertex, game);
        }

        if (LOGGER.isLoggable(Level.INFO)) {
            game.logAction(p, String.format("Placed initial settlement at vertex %d and road to vertex %d (Setup Round %d)",
                    startVertex.getId(), neighbor.getId(), round));
        }
    }

    /**
     * Fixed: System.out replaced with LOGGER.info for Sonar S106 compliance.
     */
    private static Vertex handleHumanSettlementPlacement(Player p, int round, GameMaster game, List<Integer> assigned, Scanner scanner) {
        while (true) {
            LOGGER.info(() -> String.format("[Setup Round %d] Player %d, enter Vertex ID for settlement: ", round, p.getId()));
            try {
                int vertexId = Integer.parseInt(scanner.nextLine());
                Vertex startVertex = game.getBoard().getVertex(vertexId);

                if (startVertex != null && isValidPlacement(startVertex, assigned, game)) {
                    return startVertex;
                }
                LOGGER.warning("Invalid placement. Vertex must be unoccupied and follow the distance rule.");
            } catch (NumberFormatException e) {
                LOGGER.warning("Invalid input. Please enter a numeric Vertex ID.");
            }
        }
    }

    /**
     * Fixed: System.out replaced with LOGGER.info for Sonar S106 compliance.
     */
    private static Vertex handleHumanRoadPlacement(Player p, int round, Vertex startVertex, GameMaster game, Scanner scanner) {
        while (true) {
            LOGGER.info(() -> String.format("[Setup Round %d] Player %d, enter adjacent Vertex ID for road from node %d: ",
                    round, p.getId(), startVertex.getId()));
            try {
                int neighborId = Integer.parseInt(scanner.nextLine());
                Vertex neighbor = game.getBoard().getVertex(neighborId);

                if (neighbor != null && startVertex.getAdjacentVertices().contains(neighbor)) {
                    return neighbor;
                }
                LOGGER.warning("Invalid road. Vertex must be adjacent to your settlement.");
            } catch (NumberFormatException e) {
                LOGGER.warning("Invalid input. Please enter a numeric Vertex ID.");
            }
        }
    }

    private static void executePlacement(Player p, Vertex startVertex, Vertex neighbor, GameMaster game) {
        Settlement s = new Settlement(p);
        startVertex.placeBuilding(s);
        p.addBuilding(s);
        p.addVictoryPoints(1);

        Road r = new Road(p, startVertex, neighbor);
        p.addRoad(r);
        game.getBoard().placeRoad(r);
    }

    private static Vertex findValidVertex(Player p, int round, GameMaster game, List<Integer> assigned, Random rand) {
        int attempts = 0;
        while (true) {
            attempts++;
            int randomIndex = rand.nextInt(54);
            Vertex candidate = game.getBoard().getVertex(randomIndex);

            if (isValidPlacement(candidate, assigned, game) &&
                (round == 1 || hasEssentialTrio(p, candidate, game) || attempts > 200)) {
                return candidate;
            }
        }
    }

    private static boolean isValidPlacement(Vertex candidate, List<Integer> assigned, GameMaster game) {
        if (candidate.getAdjacentVertices().isEmpty()) {
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
    }

    private static boolean hasEssentialTrio(Player p, Vertex candidate, GameMaster game) {
        if (p.getBuildingsBuilt().isEmpty()) return false;
        
        Vertex firstVertex = p.getBuildingsBuilt().get(0).getLocation();
        List<ResourceType> current = getProducedResources(firstVertex, game);
        List<ResourceType> potential = getProducedResources(candidate, game);

        boolean hasWood = current.contains(ResourceType.WOOD) || potential.contains(ResourceType.WOOD);
        boolean hasBrick = current.contains(ResourceType.BRICK) || potential.contains(ResourceType.BRICK);
        boolean hasWheat = current.contains(ResourceType.WHEAT) || potential.contains(ResourceType.WHEAT);
        boolean hasOre = current.contains(ResourceType.ORE) || potential.contains(ResourceType.ORE);

        return hasWood && hasBrick && hasWheat && hasOre;
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
        LoggerUtil.setupLogging();
        GameMaster game = new GameMaster(maxRounds);
        game.startSimulation();
    }
}