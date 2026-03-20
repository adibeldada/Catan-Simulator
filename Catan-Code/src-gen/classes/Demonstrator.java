package classes;

import classes.controller.GameMaster;
import classes.util.ConfigReader;
import classes.util.LoggerUtil;
import classes.model.Player;
import classes.model.Buildings;
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
 * @author Adib El Dada
 */
public class Demonstrator {
    /** The logger instance for recording simulation events **/
    private static final Logger LOGGER = Logger.getLogger(Demonstrator.class.getName());

    /**
     * Main entry point for the Catan Simulator Demonstrator.
     * Sets up logging, reads configuration, performs setup phase, and starts the simulation.
     * @param args Command line arguments (unused)
     */
    public static void main(String[] args) {
        LoggerUtil.setupLogging();
        printWelcomeBanner();

        ConfigReader config = new ConfigReader("config.txt"); // provides game settings from file
        printConfiguration(config);

        // GameMaster is now configured via config.txt — no hardcoded players
        GameMaster game = new GameMaster(config.getMaxRounds(), config.getPlayerConfigs());
        performSetupPhase(game);

        game.startSimulation();
        printTerminationBanner();
    }

    /**
     * Prints a welcome banner to indicate the start of the simulator.
     */
    private static void printWelcomeBanner() {
        LOGGER.info("╔════════════════════════════════════════════════╗");
        LOGGER.info("║   SETTLERS OF CATAN - SIMULATOR DEMONSTRATOR   ║");
        LOGGER.info("╚════════════════════════════════════════════════╝");
        LOGGER.info("");
    }

    /**
     * Logs the loaded configuration, including maximum turns and rounds.
     * @param config The ConfigReader object containing loaded settings
     */
    private static void printConfiguration(ConfigReader config) {
        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.info("Configuration loaded:");
            LOGGER.info(() -> String.format("  Max turns: %d", config.getMaxTurns()));
            LOGGER.info(() -> String.format("  Max rounds: %d", config.getMaxRounds()));
            LOGGER.info("");
        }
    }

    /**
     * Performs the initial setup phase for all players, including placing settlements and roads.
     * Exports the board state to JSON after each placement.
     * Supports undo/redo for human players during setup.
     * @param game The GameMaster object managing the current simulation
     */
    private static void performSetupPhase(GameMaster game) {
        List<Player> players = game.getPlayers(); // the list of participants
        List<Integer> assignedVertices = new ArrayList<>(); // tracks vertices where settlements are placed
        @SuppressWarnings("java:S2245")
        Random rand = new Random(); // random generator for AI decision making

        // Standard Catan setup: two rounds of initial placements
        for (int setupRound = 1; setupRound <= 2; setupRound++) {
            final int round = setupRound; // effective final variable for lambda use
            if (LOGGER.isLoggable(Level.INFO)) {
                LOGGER.info(() -> String.format("--- Setup Round %d ---", round));
            }
            for (Player p : players) {
                placeInitialPieces(p, round, game, assignedVertices, rand);
                // Update the visualizer state after every move
                JsonStateExporter.exportState(game.getBoard(), "../2aa4-2026-base/assignments/visualize/state.json");
            }
        }
        JsonStateExporter.exportState(game.getBoard(), "../2aa4-2026-base/assignments/visualize/state.json");

        printStartingResources(players);
    }

    /**
     * Handles placing the initial settlement and road for a single player in a setup round.
     * Awards starting resources during the second setup round.
     * For human players, supports undo after settlement and road placement.
     * @param p The player placing pieces
     * @param round The current setup round (1 or 2)
     * @param game The game master controller
     * @param assigned The list of vertex IDs already occupied
     * @param rand Random generator for AI logic
     */
    private static void placeInitialPieces(Player p, int round, GameMaster game, List<Integer> assigned, Random rand) {
        Vertex startVertex; // the vertex chosen for the settlement
        Vertex neighbor; // the target vertex for the road connection

        // Check if the player is controlled by a human or AI
        if (p instanceof classes.model.HumanPlayer) {
            Scanner scanner = new Scanner(System.in); // input reader for human choices

            // Keep looping until the human confirms both placements
            while (true) {
                // Step 1: Place settlement
                startVertex = handleHumanSettlementPlacement(p, round, game, assigned, scanner);
                Settlement s = new Settlement(p);
                s.placeOn(startVertex);
                p.addBuilding(s);
                p.addVictoryPoints(1);
                JsonStateExporter.exportState(game.getBoard(), "../2aa4-2026-base/assignments/visualize/state.json");
                LOGGER.info("Settlement placed at vertex " + startVertex.getId() + ". Type 'undo' to redo, or press Enter to place road:");
                String confirmSettle = scanner.nextLine().trim();

                if (confirmSettle.equalsIgnoreCase("undo")) {
                    // Undo the settlement placement
                    startVertex.setBuilding(null);
                    p.getBuildingsBuilt().remove(s);
                    p.addVictoryPoints(-1);
                    JsonStateExporter.exportState(game.getBoard(), "../2aa4-2026-base/assignments/visualize/state.json");
                    LOGGER.info("Settlement undone. Please choose again.");
                    continue; // restart the loop
                }

                // Step 2: Place road
                neighbor = handleHumanRoadPlacement(p, round, startVertex, game, scanner);
                Road r = new Road(p, startVertex, neighbor);
                p.addRoad(r);
                game.getBoard().placeRoad(r);
                JsonStateExporter.exportState(game.getBoard(), "../2aa4-2026-base/assignments/visualize/state.json");
                LOGGER.info("Road placed from vertex " + startVertex.getId() + " to vertex " + neighbor.getId() + ". Type 'undo' to redo road, or press Enter to confirm:");
                String confirmRoad = scanner.nextLine().trim();

                if (confirmRoad.equalsIgnoreCase("undo")) {
                    // Undo the road placement only — go back to road selection
                    game.getBoard().getRoads().remove(r);
                    p.getRoadsBuilt().remove(r);
                    JsonStateExporter.exportState(game.getBoard(), "../2aa4-2026-base/assignments/visualize/state.json");
                    LOGGER.info("Road undone. Please choose road again.");
                    // Re-place road only
                    neighbor = handleHumanRoadPlacement(p, round, startVertex, game, scanner);
                    r = new Road(p, startVertex, neighbor);
                    p.addRoad(r);
                    game.getBoard().placeRoad(r);
                    JsonStateExporter.exportState(game.getBoard(), "../2aa4-2026-base/assignments/visualize/state.json");
                }

                // Both placements confirmed
                assigned.add(startVertex.getId());

                // In round 2, players receive resources from tiles adjacent to their settlement
                if (round == 2) {
                    awardStartingResources(p, startVertex, game);
                }

                if (LOGGER.isLoggable(Level.INFO)) {
                    game.logAction(p, String.format("Placed initial settlement at vertex %d and road to vertex %d (Setup Round %d)",
                            startVertex.getId(), neighbor.getId(), round));
                }
                break; // exit the while loop — placements confirmed
            }

        } else {
            // AI placement — no undo needed
            startVertex = findValidVertex(p, round, game, assigned, rand);
            assigned.add(startVertex.getId());
            neighbor = startVertex.getAdjacentVertices().get(0); // AI defaults to the first valid neighbor

            executePlacement(p, startVertex, neighbor, game);

            // In round 2, players receive resources from tiles adjacent to their settlement
            if (round == 2) {
                awardStartingResources(p, startVertex, game);
            }

            if (LOGGER.isLoggable(Level.INFO)) {
                game.logAction(p, String.format("Placed initial settlement at vertex %d and road to vertex %d (Setup Round %d)",
                        startVertex.getId(), neighbor.getId(), round));
            }
        }
    }

    /**
     * Prompts a human player to select a valid vertex for placing a settlement.
     * Ensures the vertex is unoccupied and follows distance rules.
     * @param p The human player
     * @param round The current setup round
     * @param game The game master controller
     * @param assigned The list of occupied vertex IDs
     * @param scanner The scanner for user input
     * @return The chosen valid Vertex
     */
    private static Vertex handleHumanSettlementPlacement(Player p, int round, GameMaster game, List<Integer> assigned, Scanner scanner) {
        while (true) {
            LOGGER.info(() -> String.format("[Setup Round %d] Player %d, enter Vertex ID for settlement: ", round, p.getId()));
            try {
                int vertexId = Integer.parseInt(scanner.nextLine()); // the raw ID input from user
                Vertex startVertex = game.getBoard().getVertex(vertexId); // the corresponding vertex object

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
     * Prompts a human player to select an adjacent vertex for placing a road.
     * Ensures the road is adjacent to the settlement.
     * @param p The human player
     * @param round The current setup round
     * @param startVertex The settlement vertex from which the road starts
     * @param game The game master controller
     * @param scanner The scanner for user input
     * @return The chosen neighbor Vertex
     */
    private static Vertex handleHumanRoadPlacement(Player p, int round, Vertex startVertex, GameMaster game, Scanner scanner) {
        while (true) {
            LOGGER.info(() -> String.format("[Setup Round %d] Player %d, enter adjacent Vertex ID for road from node %d: ",
                    round, p.getId(), startVertex.getId()));
            try {
                int neighborId = Integer.parseInt(scanner.nextLine()); // the raw ID input for road target
                Vertex neighbor = game.getBoard().getVertex(neighborId); // the corresponding neighbor vertex object

                if (neighbor != null && startVertex.getAdjacentVertices().contains(neighbor)) {
                    return neighbor;
                }
                LOGGER.warning("Invalid road. Vertex must be adjacent to your settlement.");
            } catch (NumberFormatException e) {
                LOGGER.warning("Invalid input. Please enter a numeric Vertex ID.");
            }
        }
    }

    /**
     * Executes placement of a settlement and a road for a player on the board,
     * updates the player's victory points and adds buildings and roads.
     * Used for AI players only — human players use the undo-aware flow in placeInitialPieces.
     * @param p The player placing pieces
     * @param startVertex The vertex for the settlement
     * @param neighbor The vertex for the road end-point
     * @param game The game master controller
     */
    private static void executePlacement(Player p, Vertex startVertex, Vertex neighbor, GameMaster game) {
        Settlement s = new Settlement(p); // the settlement object to be placed
        s.placeOn(startVertex);
        p.addBuilding(s);
        p.addVictoryPoints(1);
        Road r = new Road(p, startVertex, neighbor); // the road object to be placed
        p.addRoad(r);
        game.getBoard().placeRoad(r);
    }

    /**
     * Finds a valid vertex for an AI player to place a settlement,
     * considering distance rules and resource diversity.
     * @param p The AI player
     * @param round The current setup round
     * @param game The game master controller
     * @param assigned The list of occupied vertex IDs
     * @param rand Random generator for selection
     * @return A valid Vertex for placement
     */
    private static Vertex findValidVertex(Player p, int round, GameMaster game, List<Integer> assigned, Random rand) {
        int attempts = 0; // counter to prevent infinite loops in constrained boards
        while (true) {
            attempts++;
            int randomIndex = rand.nextInt(54); // standard Catan board has 54 vertices
            Vertex candidate = game.getBoard().getVertex(randomIndex); // the vertex being evaluated

            // Logic: Must be valid, and in Round 2, try to get a diverse resource set (Essential Trio)
            if (isValidPlacement(candidate, assigned, game) &&
                (round == 1 || hasEssentialTrio(p, candidate, game) || attempts > 200)) {
                return candidate;
            }
        }
    }

    /**
     * Checks if a candidate vertex is valid for settlement placement,
     * ensuring it is unoccupied and not adjacent to another settlement.
     * @param candidate The vertex being checked
     * @param assigned The list of currently occupied vertex IDs
     * @param game The game master controller
     * @return True if placement is valid, false otherwise
     */
    private static boolean isValidPlacement(Vertex candidate, List<Integer> assigned, GameMaster game) {
        if (candidate.getAdjacentVertices().isEmpty()) {
            return false;
        }
        for (int occupiedId : assigned) {
            Vertex occupied = game.getBoard().getVertex(occupiedId); // an existing settlement vertex
            // Enforce Distance Rule: No settlement can be on or adjacent to another settlement
            if (candidate == occupied || candidate.getAdjacentVertices().contains(occupied)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Awards starting resources to a player based on tiles adjacent to their settlement.
     * @param p The player receiving resources
     * @param vertex The vertex where the settlement was placed
     * @param game The game master controller
     */
    private static void awardStartingResources(Player p, Vertex vertex, GameMaster game) {
        for (Tile tile : game.getBoard().getTiles()) {
            if (tile.getAdjacentVertices().contains(vertex)) {
                p.collectResource(tile.getResourceType(), 1);
            }
        }
    }

    /**
     * Prints the initial resources/cards for all players after setup is complete.
     * @param players The list of players in the game
     */
    private static void printStartingResources(List<Player> players) {
        LOGGER.info("");
        LOGGER.info("Initial placement complete. Starting cards:");
        for (Player p : players) {
            if (LOGGER.isLoggable(Level.INFO)) {
                LOGGER.info(() -> String.format("  Player %d: %d cards", p.getId(), p.getHand().totalCards()));
            }
        }
    }

    /**
     * Checks if placing a settlement on a candidate vertex provides access to a diverse set of essential resources.
     * @param p The player whose resources are being checked
     * @param candidate The vertex being considered for the second settlement
     * @param game The game master controller
     * @return True if the combination of settlements provides Wood, Brick, Wheat, and Ore
     */
    private static boolean hasEssentialTrio(Player p, Vertex candidate, GameMaster game) {
        if (p.getBuildingsBuilt().isEmpty()) return false;

        Vertex firstVertex = p.getBuildingsBuilt().get(0).getLocation(); // the first settlement placed
        List<ResourceType> current = getProducedResources(firstVertex, game); // resources from first settlement
        List<ResourceType> potential = getProducedResources(candidate, game); // resources from second settlement

        // Check for the "Essential Trio" (and Ore) to ensure a strong start
        boolean hasWood = current.contains(ResourceType.WOOD) || potential.contains(ResourceType.WOOD);
        boolean hasBrick = current.contains(ResourceType.BRICK) || potential.contains(ResourceType.BRICK);
        boolean hasWheat = current.contains(ResourceType.WHEAT) || potential.contains(ResourceType.WHEAT);
        boolean hasOre = current.contains(ResourceType.ORE) || potential.contains(ResourceType.ORE);

        return hasWood && hasBrick && hasWheat && hasOre;
    }

    /**
     * Returns a list of resources produced by a vertex based on its adjacent tiles.
     * @param v The vertex to check
     * @param game The game master controller
     * @return A list of ResourceType available at this vertex
     */
    private static List<ResourceType> getProducedResources(Vertex v, GameMaster game) {
        List<ResourceType> resources = new ArrayList<>(); // accumulator for found resources
        for (Tile t : game.getBoard().getTiles()) {
            if (t.getAdjacentVertices().contains(v)) {
                resources.add(t.getResourceType());
            }
        }
        return resources;
    }

    /**
     * Prints a termination banner to indicate the end of the demonstration.
     */
    private static void printTerminationBanner() {
        LOGGER.info("");
        LOGGER.info("╔════════════════════════════════════════════════╗");
        LOGGER.info("║         DEMONSTRATION COMPLETE                 ║");
        LOGGER.info("╚════════════════════════════════════════════════╝");
    }

    /**
     * Runs a custom demonstration using settings from a specified config file.
     * @param configFilePath Path to the configuration file
     */
    public static void runCustomDemo(String configFilePath) {
        LoggerUtil.setupLogging();
        ConfigReader config = new ConfigReader(configFilePath); // read config from file
        GameMaster game = new GameMaster(config.getMaxRounds(), config.getPlayerConfigs());
        game.startSimulation();
    }
}