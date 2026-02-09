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
 * * R1.9: Demonstrates the key functionality of the simulator.
 */
public class Demonstrator {
    
    public static void main(String[] args) {
        // Print welcome banner
        System.out.println("╔════════════════════════════════════════════════╗");
        System.out.println("║   SETTLERS OF CATAN - SIMULATOR DEMONSTRATOR   ║");
        System.out.println("╚════════════════════════════════════════════════╝");
        System.out.println();
        
        // ============================================================
        // CONFIGURATION PHASE (R1.4)
        // ============================================================
        String configPath = "config.txt";
        ConfigReader config = new ConfigReader(configPath);
        
        System.out.println("Configuration loaded:");
        System.out.println("  Max turns: " + config.getMaxTurns());
        System.out.println("  Max rounds: " + config.getMaxRounds());
        System.out.println();
        
        // ============================================================
        // INITIALIZATION PHASE (R1.1, R1.2)
        // ============================================================
        GameMaster game = new GameMaster(config.getMaxRounds());
        
        // ============================================================
        // SETUP PHASE: INITIAL PLACEMENT (R1.3)
        // ============================================================
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

                    // CHECK 2: Distance Rule (R1.6)
                    boolean respectsDistance = true;
                    for (int occupiedId : assignedVertices) {
                        Vertex occupied = game.getBoard().getVertex(occupiedId);
                        if (candidate == occupied || candidate.getAdjacentVertices().contains(occupied)) {
                            respectsDistance = false;
                            break;
                        }
                    }
                    
                    if (isNotDeadZone && respectsDistance) {
                        // FIXED: Strict Fair Placement for Round 2 to prevent Deadlock (R1.3)
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
        // SIMULATION PHASE
        // ============================================================
        game.startSimulation();
        
        // ============================================================
        // TERMINATION PHASE
        // ============================================================
        System.out.println();
        System.out.println("╔════════════════════════════════════════════════╗");
        System.out.println("║         DEMONSTRATION COMPLETE                 ║");
        System.out.println("╚════════════════════════════════════════════════╝");
    }

    /**
     * Checks if a second settlement choice provides access to the "Essential Trio" 
     * (WOOD, BRICK, WHEAT) required for expansion without trading (R1.3).
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
     * Helper to retrieve all resources produced by a specific vertex.
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
    
    public static void runCustomDemo(int maxRounds) {
        System.out.println("Running custom demonstration with " + maxRounds + " rounds...");
        System.out.println();
        GameMaster game = new GameMaster(maxRounds);
        game.startSimulation();
    }
}