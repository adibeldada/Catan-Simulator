package CatanSimulatorDomainModel.catanUML;

import CatanSimulatorDomainModel.catanUML.controller.GameMaster;
import CatanSimulatorDomainModel.catanUML.util.ConfigReader;
import CatanSimulatorDomainModel.catanUML.model.Player;
import CatanSimulatorDomainModel.catanUML.enums.ResourceType;
import CatanSimulatorDomainModel.catanUML.model.Buildings;
import CatanSimulatorDomainModel.catanUML.model.Road;
import CatanSimulatorDomainModel.catanUML.model.Settlement;
import CatanSimulatorDomainModel.catanUML.model.Vertex;
import CatanSimulatorDomainModel.catanUML.model.City;
import CatanSimulatorDomainModel.catanUML.model.Tile;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Demonstrator class for the Catan Simulator.
 *
 * R1.9 Requirement:
 * This class contains a single static main method that executes
 * one full demonstrative simulation of the system.
 *
 * The demonstration includes:
 *  - Configuration loading
 *  - Game initialization
 *  - Initial settlement and road placement
 *  - Resource allocation
 *  - Execution of the main simulation loop
 *  - Proper termination output
 *
 * The purpose of this class is to clearly showcase the key
 * functionality of the simulator in a controlled and readable way.
 */

public class Demonstrator {
    /**
     * Entry point of the simulator demonstration.
     *
     * This method walks through all major simulator phases:
     *  1. Configuration Phase
     *  2. Initialization Phase
     *  3. Setup Phase (Initial placements)
     *  4. Simulation Phase
     *  5. Termination Phase
     *
     * The output logs allow the observer to trace the behavior
     * of the system step-by-step.
     */
    public static void main(String[] args) {
        // Print welcome banner
        System.out.println("╔════════════════════════════════════════════════╗");
        System.out.println("║   SETTLERS OF CATAN - SIMULATOR DEMONSTRATOR   ║");
        System.out.println("╚════════════════════════════════════════════════╝");
        System.out.println();
        
        // ============================================================
        // 1. CONFIGURATION PHASE
        // ============================================================
        // Loads simulation settings (max turns, max rounds)
        // from an external configuration file.
        
        String configPath = "config.txt";
        ConfigReader config = new ConfigReader(configPath);
        
        System.out.println("Configuration loaded:");
        System.out.println("  Max turns: " + config.getMaxTurns());
        System.out.println("  Max rounds: " + config.getMaxRounds());
        System.out.println();
        
        // ============================================================
        // 2. INITIALIZATION PHASE
        // ============================================================
        // Creates the GameMaster, which initializes:
        //  - The board
        //  - Players
        //  - Internal game state
        GameMaster game = new GameMaster(config.getMaxRounds());
        
        // ============================================================
        // 3. SETUP PHASE (Initial Placement)
        // ============================================================
        // Each player places:
        //  - 2 settlements
        //  - 2 roads
        //
        // Rules enforced:
        //  - Distance rule (no adjacent settlements)
        //  - No dead-zone placement
        //  - Second settlement prioritizes essential resources
        //
        // After second settlement:
        //  - Players collect starting resources
        
        List<Player> players = game.getPlayers();
        List<Integer> assignedVertices = new ArrayList<>();
        Random rand = new Random();
        
        // R1.3: Loop twice for each player to place 2 settlements and 2 roads
        for (int setupRound = 1; setupRound <= 2; setupRound++) {
            System.out.println("--- Setup Round " + setupRound + " ---");
            
            for (int i = 0; i < players.size(); i++) {
                Player p = players.get(i);
                Vertex startVertex = null;
                boolean validSpotFound = false;
                int attempts = 0;
                
                while (!validSpotFound) {
                    attempts++;
                    int randomIndex = rand.nextInt(54); 
                    Vertex candidate = game.getBoard().getVertex(randomIndex);

                    // CHECK 1: Is it a dead zone?
                    boolean isNotDeadZone = candidate.getAdjacentVertices().size() >= 2;

                 // Enforces R1.6 invariant: settlements must be at least
                 // one vertex apart (distance ≥ 2 between players)
                    boolean respectsDistance = true;
                    for (int occupiedId : assignedVertices) {
                        Vertex occupied = game.getBoard().getVertex(occupiedId);
                        if (candidate == occupied || candidate.getAdjacentVertices().contains(occupied)) {
                            respectsDistance = false;
                            break;
                        }
                    }
                    
                    if (isNotDeadZone && respectsDistance) {
                    	// For the second settlement, we prioritize essential expansion resources
                    	// while limiting attempts to prevent infinite loops.
                        if (setupRound == 2) {
                            // Attempts limit prevents infinite loops while prioritizing essential resources
                            if (hasEssentialTrio(p, candidate, game) || attempts > 200) {
                                startVertex = candidate;
                                assignedVertices.add(randomIndex);
                                validSpotFound = true;
                            }
                        } else {
                            startVertex = candidate;
                            assignedVertices.add(randomIndex);
                            validSpotFound = true;
                        }
                    }
                }
                
                // Place initial settlement (R1.3)
                Settlement s = new Settlement(p);
                startVertex.placeBuilding(s);
                p.addBuilding(s);
                p.addVictoryPoints(1);
                
                // Place initial road (R1.3)
                Vertex neighbor = startVertex.getAdjacentVertices().get(0);
                Road r = new Road(p, startVertex, neighbor);
                p.addRoad(r);
                game.getBoard().placeRoad(r);

                // R1.3: Award resources for the SECOND settlement placement
                if (setupRound == 2) {
                    for (Tile tile : game.getBoard().getTiles()) {
                        if (tile.getAdjacentVertices().contains(startVertex)) {
                            p.collectResource(tile.getResourceType(), 1);
                        }
                    }
                }
                
                // FIXED: Enhanced logging to show IDs of vertices (R1.7)
                game.logAction(p, String.format("Placed initial settlement at vertex %d and road to vertex %d (Setup Round %d)", 
                               startVertex.getId(), neighbor.getId(), setupRound));
            }
        }
        
        // FIXED: Displays starting cards for ALL players for full transparency
        System.out.println("\nInitial placement complete. Starting cards:");
        for (Player p : players) {
            System.out.println("  Player " + p.getId() + ": " + p.getHand().totalCards() + " cards");
        }
        System.out.println("Vertex 0 adjacents: " + game.getBoard().getVertex(0).getAdjacentVertices().size());
        
        // ============================================================
        // 4. SIMULATION PHASE
        // ============================================================
        // Starts the main simulation loop.
        // The GameMaster controls:
        //  - Turn rotation
        //  - Dice rolls
        //  - Resource distribution
        //  - Building logic
        //  - Victory conditions
        game.startSimulation();
        
        // ============================================================
        // 5. TERMINATION PHASE
        // ============================================================
        // Prints completion banner.
        // The simulation ends when:
        //  - Maximum rounds are reached
        //  - Or a player meets victory conditions
        System.out.println();
        System.out.println("╔════════════════════════════════════════════════╗");
        System.out.println("║         DEMONSTRATION COMPLETE                 ║");
        System.out.println("╚════════════════════════════════════════════════╝");
    }

    /**
     * Determines whether placing the second settlement at the
     * candidate vertex gives the player access to the
     * essential expansion resources:
     *  - WOOD
     *  - BRICK
     *  - WHEAT
     *
     * This improves early-game viability without requiring trading.
     */
    private static boolean hasEssentialTrio(Player p, Vertex candidate, GameMaster game) {
        // Access the location of the first building placed in Round 1
        Vertex firstVertex = p.getBuildingsBuilt().get(0).getLocation();
        List<ResourceType> current = getProducedResources(firstVertex, game);
        List<ResourceType> potential = getProducedResources(candidate, game);
        
        // Check combined resource availability
        boolean hasWood = current.contains(ResourceType.WOOD) || potential.contains(ResourceType.WOOD);
        boolean hasBrick = current.contains(ResourceType.BRICK) || potential.contains(ResourceType.BRICK);
        boolean hasWheat = current.contains(ResourceType.WHEAT) || potential.contains(ResourceType.WHEAT);
        
        return hasWood && hasBrick && hasWheat;
    }

    /**
     * Returns all resource types produced by tiles
     * adjacent to the given vertex.
     *
     * Used to evaluate strategic settlement placement.
     */
    private static List<ResourceType> getProducedResources(Vertex v, GameMaster game) {
        List<ResourceType> resources = new ArrayList<>();
        for (Tile t : game.getBoard().getTiles()) {
            if (t.getAdjacentVertices().contains(v)) {
                resources.add(t.getResourceType());
            }
        }
        return resources;
    }
    
    /**
     * Alternative entry point used for testing custom round limits.
     * Allows running the simulation without reading from config file.
     */
    public static void runCustomDemo(int maxRounds) {
        System.out.println("Running custom demonstration with " + maxRounds + " rounds...");
        System.out.println();
        GameMaster game = new GameMaster(maxRounds);
        game.startSimulation();
    }
}