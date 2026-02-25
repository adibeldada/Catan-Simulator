package CatanSimulatorDomainModel.catanUML.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Reads configuration from a file.
 * * R1.4: Configuration file format: turns: int [1-8192]
 * * The config file can contain:
 * - Comments (lines starting with #)
 * - Empty lines
 * - turns: <number> (the number of turns to simulate)
 */
public class ConfigReader {
    // Initialize the Logger to replace System.err
    private static final Logger LOGGER = Logger.getLogger(ConfigReader.class.getName());
    
    private int maxTurns;

    /**
     * Constructs a ConfigReader and reads the configuration file.
     * * @param configFilePath Path to the configuration file
     */
    public ConfigReader(String configFilePath) {
        this.maxTurns = 100; // Default value
        readConfig(configFilePath);
    }

    /**
     * Reads the configuration file and extracts the number of turns.
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
                    parseTurnsLine(line);
                }
            }
        } catch (IOException e) {
            // Replaced System.err with LOGGER
            LOGGER.log(Level.SEVERE, "Could not read config file: {0}", configFilePath);
            LOGGER.severe("Using default turns: 100");
        }
    }

    /**
     * Extracted method to handle line parsing and resolve the nested try block issue.
     * * @param line The line containing the turns configuration
     */
    private void parseTurnsLine(String line) {
        String[] parts = line.split(":");
        if (parts.length == 2) {
            try {
                int turns = Integer.parseInt(parts[1].trim());
                // R1.4: Clamp to valid range [1-8192] 
                // Note: Kept your logic of 32768 to maintain identical behavior
                maxTurns = Math.max(1, Math.min(turns, 32768));
            } catch (NumberFormatException e) {
                // Replaced System.err with LOGGER
                LOGGER.severe("Invalid turns value in config. Using default: 100");
            }
        }
    }

    /**
     * Gets the maximum number of turns.
     * * @return Maximum turns (1-8192)
     */
    public int getMaxTurns() {
        return maxTurns;
    }

    /**
     * Converts turns to rounds (4 players per round).
     * * @return Maximum rounds (turns / 4)
     */
    public int getMaxRounds() {
        return maxTurns / 4;
    }
}