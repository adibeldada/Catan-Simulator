package CatanSimulatorDomainModel.catanUML;

import CatanSimulatorDomainModel.catanUML.controller.GameMaster;
import CatanSimulatorDomainModel.catanUML.util.ConfigReader;
/**
 * Demonstrator class for the Catan Simulator.
 * 
 * R1.9: Demonstrates the key functionality of the simulator.
 * 
 * This class runs one or more demonstrative simulations of the Settlers of Catan game.
 * The simulation follows these key rules:
 * 
 * REQUIREMENTS SATISFIED:
 * 
 * - R1.1: Uses a fixed, valid map setup with specified tile/vertex identification
 *         (Tiles: 0-18, Vertices: 0-53)
 * 
 * - R1.2: Simulates 4 randomly acting agents
 *         (Each player makes random decisions based on available resources)
 * 
 * - R1.3: Follows the Catan rulebook 
 *         (Excluding: harbours, trading, development cards, robber)
 * 
 * - R1.4: Runs for user-defined rounds (max 8192) or until 10 VPs achieved
 *         (Configuration read from config.txt)
 * 
 * - R1.5: Halts upon reaching termination conditions
 *         (Either 10 VP reached or max rounds exceeded)
 * 
 * - R1.6: Respects game invariants
 *         (Road connectivity, distance rules, city upgrades enforced by RuleValidator)
 * 
 * - R1.7: Prints actions in specified format: [RoundNumber] / [PlayerID]: [Action]
 *         (Each move logs its action through GameMaster.logAction())
 * 
 * - R1.8: Players with >7 cards must attempt to build
 *         (Enforced in Player.takeTurn() method)
 * 
 * - R1.9: Demonstrator class with comprehensive comments
 *         (This class)
 * 
 * CONFIGURATION:
 * Configuration is read from config.txt with format:
 *   turns: <number>  (1-8192)
 * 
 * SIMULATION FLOW:
 * 1. Read configuration from config.txt
 * 2. Initialize the GameMaster with the board and 4 players
 * 3. Execute rounds until victory or max rounds reached
 * 4. Each round consists of:
 *    a. Dice roll for resource production
 *    b. Each player takes a turn (build or pass)
 *    c. Victory point summary printed
 * 5. Declare winner or show final standings
 * 
 * DEMONSTRATION FEATURES:
 * - Board initialization with tiles, vertices, and adjacencies
 * - Resource production based on dice rolls
 * - Random player decision-making
 * - Building roads, settlements, and cities
 * - Victory point tracking
 * - Game termination (10 VPs or max rounds)
 * - Rule validation (connectivity, distance, upgrades)
 * - Formatted console output
 */
public class Demonstrator {
    
    /**
     * Main entry point for the Catan simulator demonstration.
     * 
     * EXECUTION FLOW:
     * 
     * 1. CONFIGURATION PHASE:
     *    - Read config.txt to get max turns
     *    - Convert turns to rounds (4 players per round)
     *    - Display configuration to user
     * 
     * 2. INITIALIZATION PHASE:
     *    - Create GameMaster instance
     *    - Initialize board with 19 tiles (R1.1)
     *    - Create 54 vertices with adjacencies (R1.1)
     *    - Create 4 player agents (R1.2)
     *    - Initialize dice and rule validator
     * 
     * 3. SIMULATION PHASE:
     *    - Loop through rounds until termination:
     *      a. For each player in order:
     *         i.   Roll 2d6 for resource production
     *         ii.  Produce resources from tiles (R1.3)
     *         iii. Player decides action (random AI)
     *         iv.  Validate move (RuleValidator - R1.6)
     *         v.   Execute move (deduct resources, place structure)
     *         vi.  Log action (R1.7: [Round] / [Player]: [Action])
     *      b. Print round summary with VP counts (R1.7)
     *      c. Check victory condition (R1.5: 10 VP)
     *      d. Check round limit (R1.5: max rounds)
     * 
     * 4. TERMINATION PHASE:
     *    - Announce winner (if 10 VP reached)
     *    - Or show final standings (if max rounds reached)
     * 
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        // Print welcome banner
        System.out.println("╔════════════════════════════════════════════════╗");
        System.out.println("║   SETTLERS OF CATAN - SIMULATOR DEMONSTRATOR   ║");
        System.out.println("╚════════════════════════════════════════════════╝");
        System.out.println();
        
        // ============================================================
        // CONFIGURATION PHASE (R1.4)
        // ============================================================
        // Read configuration from file
        // Format: turns: <number> (1-8192)
        String configPath = "config.txt";
        ConfigReader config = new ConfigReader(configPath);
        
        System.out.println("Configuration loaded:");
        System.out.println("  Max turns: " + config.getMaxTurns());
        System.out.println("  Max rounds: " + config.getMaxRounds());
        System.out.println();
        
        // ============================================================
        // INITIALIZATION PHASE (R1.1, R1.2)
        // ============================================================
        // Create GameMaster which will:
        // - Initialize the board with tiles and vertices (R1.1)
        // - Create 4 player agents (R1.2)
        // - Set up dice and rule validator
        GameMaster game = new GameMaster(config.getMaxRounds());
        
        // ============================================================
        // SIMULATION PHASE (R1.3, R1.4, R1.5, R1.6, R1.7, R1.8)
        // ============================================================
        // Start the simulation which will:
        // - Run rounds following Catan rules (R1.3)
        // - Continue until termination condition (R1.4, R1.5)
        // - Enforce game invariants through RuleValidator (R1.6)
        // - Print formatted output (R1.7)
        // - Force building when >7 cards (R1.8)
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
     * Alternative demonstration method with custom parameters.
     * Can be used for testing specific scenarios.
     * 
     * This method bypasses config.txt and uses a direct parameter.
     * Useful for automated testing or custom demonstrations.
     * 
     * @param maxRounds The maximum number of rounds to simulate
     */
    public static void runCustomDemo(int maxRounds) {
        System.out.println("Running custom demonstration with " + maxRounds + " rounds...");
        System.out.println();
        
        GameMaster game = new GameMaster(maxRounds);
        game.startSimulation();
    }
}