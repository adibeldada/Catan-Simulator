package classes.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for parsing user commands.
 */
public class CommandParser {

    // S1118: Private constructor to prevent instantiation of utility class
    private CommandParser() {
        throw new IllegalStateException("Utility class");
    }

    // Regex patterns updated to be more robust
    private static final Pattern ROLL_PAT = Pattern.compile("(?i)^roll$");
    private static final Pattern GO_PAT = Pattern.compile("(?i)^go$");
    private static final Pattern LIST_PAT = Pattern.compile("(?i)^list$");
    private static final Pattern SETTLE_PAT = Pattern.compile("(?i)^build\\s+settlement\\s+(\\d+)$");
    private static final Pattern CITY_PAT = Pattern.compile("(?i)^build\\s+city\\s+(\\d+)$");
    private static final Pattern ROAD_PAT = Pattern.compile("(?i)^build\\s+road\\s+(\\d+)\\s*,\\s*(\\d+)$");

    /**
     * Parses input and returns an array: [CommandName, Arg1, Arg2, ...]
     * Returns an empty array instead of null if the input doesn't match.
     */
    public static String[] parse(String input) {
        // S1168: Return empty array instead of null
        if (input == null) {
            return new String[0];
        }
        
        String trimmedInput = input.trim();

        if (ROLL_PAT.matcher(trimmedInput).matches()) return new String[]{"ROLL"};
        if (GO_PAT.matcher(trimmedInput).matches())   return new String[]{"GO"};
        if (LIST_PAT.matcher(trimmedInput).matches()) return new String[]{"LIST"};

        Matcher m;
        m = SETTLE_PAT.matcher(trimmedInput);
        if (m.matches()) return new String[]{"SETTLE", m.group(1)};

        m = CITY_PAT.matcher(trimmedInput);
        if (m.matches()) return new String[]{"CITY", m.group(1)};

        m = ROAD_PAT.matcher(trimmedInput);
        if (m.matches()) return new String[]{"ROAD", m.group(1), m.group(2)};

        // S1168: Return empty array instead of null
        return new String[0]; 
    }
}