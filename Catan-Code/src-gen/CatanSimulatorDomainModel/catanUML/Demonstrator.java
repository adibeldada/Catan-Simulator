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

/**
 * Demonstrator class for the Catan Simulator.
 */
public class Demonstrator {

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
        System.out.println("╔════════════════════════════════════════════════╗");
        System.out.println("║   SETTLERS OF CATAN - SIMULATOR DEMONSTRATOR   ║");
        System.out.println("╚════════════════════════════════════════════════╝");
        System.out.println();
    }

    private static void printConfiguration(ConfigReader config) {
        System.out.println("Configuration loaded:");
        System.out.println("  Max turns: " + config.getMaxTurns());
        System.out.println("  Max rounds: " + config.getMaxRounds());
        System.out.println();
    }

    private static void performSetupPhase(GameMaster game) {
        List<Player> players = game.getPlayers();
        List<Integer> assignedVertices = new ArrayList<>();
        Random rand = new Random();
        
        for (int setupRound = 1; setupRound <= 2; setupRound++) {
            System.out.println("--- Setup Round " + setupRound + " ---");
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

            if (isValidPlacement(candidate, assigned, game)) {
                // If round 1, or round 2 with resources/timeout, accept the spot
                if (round == 1 || hasEssentialTrio(p, candidate, game) || attempts > 200) {
                    assigned.add(randomIndex);
                    return candidate;
                }
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
        System.out.println("\nInitial placement complete. Starting cards:");
        for (Player p : players) {
            System.out.println("  Player " + p.getId() + ": " + p.getHand().totalCards() + " cards");
        }
        System.out.println("Vertex 0 adjacents: " + game.getBoard().getVertex(0).getAdjacentVertices().size());
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
        System.out.println();
        System.out.println("╔════════════════════════════════════════════════╗");
        System.out.println("║         DEMONSTRATION COMPLETE                 ║");
        System.out.println("╚════════════════════════════════════════════════╝");
    }

    public static void runCustomDemo(int maxRounds) {
        System.out.println("Running custom demonstration with " + maxRounds + " rounds...");
        System.out.println();
        GameMaster game = new GameMaster(maxRounds);
        game.startSimulation();
    }
}