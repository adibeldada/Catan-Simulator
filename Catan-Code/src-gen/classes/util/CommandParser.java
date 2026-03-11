package classes.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandParser {
    // Regex patterns updated to be more robust
    private static final Pattern ROLL_PAT = Pattern.compile("(?i)^roll$");
    private static final Pattern GO_PAT = Pattern.compile("(?i)^go$");
    private static final Pattern LIST_PAT = Pattern.compile("(?i)^list$");
    private static final Pattern SETTLE_PAT = Pattern.compile("(?i)^build\\s+settlement\\s+(\\d+)$");
    private static final Pattern CITY_PAT = Pattern.compile("(?i)^build\\s+city\\s+(\\d+)$");
    private static final Pattern ROAD_PAT = Pattern.compile("(?i)^build\\s+road\\s+(\\d+)\\s*,\\s*(\\d+)$");

    /**
     * Parses input and returns an array: [CommandName, Arg1, Arg2, ...]
     * Returns null if the input doesn't match any command.
     */
    public static String[] parse(String input) {
        if (input == null) return null;
        input = input.trim();

        if (ROLL_PAT.matcher(input).matches()) return new String[]{"ROLL"};
        if (GO_PAT.matcher(input).matches())   return new String[]{"GO"};
        if (LIST_PAT.matcher(input).matches()) return new String[]{"LIST"};

        Matcher m;
        m = SETTLE_PAT.matcher(input);
        if (m.matches()) return new String[]{"SETTLE", m.group(1)};

        m = CITY_PAT.matcher(input);
        if (m.matches()) return new String[]{"CITY", m.group(1)};

        m = ROAD_PAT.matcher(input);
        if (m.matches()) return new String[]{"ROAD", m.group(1), m.group(2)};

        return null; // No match found
    }
}