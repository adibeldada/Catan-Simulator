package classes.util;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Reads configuration from a file.
 * R1.4: Configuration file format: turns: int [1-8192]
 * Player configuration: player: <id>, <type> (HUMAN or AI)
 */
public class ConfigReader {
    private static final Logger LOGGER = Logger.getLogger(ConfigReader.class.getName());

    private int maxTurns;

    /**
     * Stores player configuration entries from the config file.
     * Each entry is a String array: [id, type]
     */
    private final List<String[]> playerConfigs = new ArrayList<>();

    public ConfigReader(String configFilePath) {
        this.maxTurns = 100; // Default value
        readConfig(configFilePath);
    }

    private void readConfig(String configFilePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(configFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();

                // Skip comments and empty lines
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                if (line.startsWith("turns:")) {
                    parseTurnsLine(line);
                }
                if (line.startsWith("player:")) {
                    parsePlayerLine(line);
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Could not read config file: {0}", configFilePath);
            LOGGER.severe("Using default turns: 100");
        }

        // If no players were defined in config, default to 4 AI players
        if (playerConfigs.isEmpty()) {
            LOGGER.warning("No player configuration found. Defaulting to 4 AI players.");
            playerConfigs.add(new String[]{"1", "AI"});
            playerConfigs.add(new String[]{"2", "AI"});
            playerConfigs.add(new String[]{"3", "AI"});
            playerConfigs.add(new String[]{"4", "AI"});
        }
    }

    private void parseTurnsLine(String line) {
        String[] parts = line.split(":");
        if (parts.length == 2) {
            try {
                int turns = Integer.parseInt(parts[1].trim());
                maxTurns = Math.max(1, Math.min(turns, 32768));
            } catch (NumberFormatException e) {
                LOGGER.severe("Invalid turns value in config. Using default: 100");
            }
        }
    }

    /**
     * Parses a player configuration line.
     * Format: player: <id>, <type>
     * Example: player: 1, HUMAN
     */
    private void parsePlayerLine(String line) {
        // Remove "player:" prefix
        String value = line.substring("player:".length()).trim();
        String[] parts = value.split(",");
        if (parts.length == 2) {
            String id = parts[0].trim();
            String type = parts[1].trim().toUpperCase();
            if (type.equals("HUMAN") || type.equals("AI")) {
                playerConfigs.add(new String[]{id, type});
            } else {
                LOGGER.warning("Invalid player type: " + type + ". Must be HUMAN or AI. Defaulting to AI.");
                playerConfigs.add(new String[]{id, "AI"});
            }
        } else {
            LOGGER.warning("Invalid player config line: " + line);
        }
    }

    public int getMaxTurns() {
        return maxTurns;
    }

    public int getMaxRounds() {
        return maxTurns / 4;
    }

    /**
     * Returns the list of player configurations.
     * Each entry is a String array: [id, type]
     *
     * @return List of player config entries
     */
    public List<String[]> getPlayerConfigs() {
        return playerConfigs;
    }
}