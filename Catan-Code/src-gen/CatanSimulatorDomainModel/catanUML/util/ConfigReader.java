package util;
package CatanSimulatorDomainModel.catanUML;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Reads configuration from a file.
 * 
 * R1.4: Configuration file format: turns: int [1-8192]
 * 
 * The config file can contain:
 * - Comments (lines starting with #)
 * - Empty lines
 * - turns: <number> (the number of turns to simulate)
 * 
 * Example config.txt:
 * # Catan Simulator Configuration
 * turns: 100
 */
public class ConfigReader {
    private int maxTurns;

    /**
     * Constructs a ConfigReader and reads the configuration file.
     * 
     * @param configFilePath Path to the configuration file
     */
    public ConfigReader(String configFilePath) {
        this.maxTurns = 100; // Default value
        readConfig(configFilePath);
    }

    /**
     * Reads the configuration file and extracts the number of turns.
     * 
     * Format: turns: <number>
     * - Number must be between 1 and 8192 (R1.4)
     * - Invalid values default to 100
     * 
     * @param configFilePath Path to the configuration file
     */
    private void readConfig(String configFilePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(configFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                
                // Skip comments and empty lines
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                // Parse "turns: <number>" format
                if (line.startsWith("turns:")) {
                    String[] parts = line.split(":");
                    if (parts.length == 2) {
                        try {
                            int turns = Integer.parseInt(parts[1].trim());
                            // R1.4: Clamp to valid range [1-8192]
                            maxTurns = Math.max(1, Math.min(turns, 8192));
                        } catch (NumberFormatException e) {
                            System.err.println("Invalid turns value in config. Using default: 100");
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Could not read config file: " + configFilePath);
            System.err.println("Using default turns: 100");
        }
    }

    /**
     * Gets the maximum number of turns.
     * 
     * @return Maximum turns (1-8192)
     */
    public int getMaxTurns() {
        return maxTurns;
    }

    /**
     * Converts turns to rounds (4 players per round).
     * 
     * @return Maximum rounds (turns / 4)
     */
    public int getMaxRounds() {
        return maxTurns / 4;
    }
}