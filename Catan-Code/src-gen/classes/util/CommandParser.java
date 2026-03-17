package classes.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for parsing user commands.
 */
public class CommandParser {
    private CommandParser() {
        throw new IllegalStateException("Utility class");
    }

    private static final Pattern ROLL_PAT   = Pattern.compile("(?i)^roll$");
    private static final Pattern GO_PAT     = Pattern.compile("(?i)^go$");
    private static final Pattern LIST_PAT   = Pattern.compile("(?i)^list$");
    // R3.1: undo / redo commands
    private static final Pattern UNDO_PAT   = Pattern.compile("(?i)^undo$");
    private static final Pattern REDO_PAT   = Pattern.compile("(?i)^redo$");
    private static final Pattern SETTLE_PAT = Pattern.compile("(?i)^build\\s+settlement\\s+(\\d+)$");
    private static final Pattern CITY_PAT   = Pattern.compile("(?i)^build\\s+city\\s+(\\d+)$");
    private static final Pattern ROAD_PAT   = Pattern.compile("(?i)^build\\s+road\\s+(\\d+)\\s*,\\s*(\\d+)$");

    /**
     * Parses input and returns an array: [CommandName, Arg1, Arg2, ...]
     * Returns an empty array if the input does not match any known command.
     */
    public static String[] parse(String input) {
        if (input == null) {
            return new String[0];
        }

        String trimmed = input.trim();
        if (ROLL_PAT.matcher(trimmed).matches()) return new String[]{"ROLL"};
        if (GO_PAT.matcher(trimmed).matches())   return new String[]{"GO"};
        if (LIST_PAT.matcher(trimmed).matches()) return new String[]{"LIST"};
        if (UNDO_PAT.matcher(trimmed).matches()) return new String[]{"UNDO"};
        if (REDO_PAT.matcher(trimmed).matches()) return new String[]{"REDO"};

        Matcher m;
        m = SETTLE_PAT.matcher(trimmed);
        if (m.matches()) return new String[]{"SETTLE", m.group(1)};
        m = CITY_PAT.matcher(trimmed);
        if (m.matches()) return new String[]{"CITY", m.group(1)};
        m = ROAD_PAT.matcher(trimmed);
        if (m.matches()) return new String[]{"ROAD", m.group(1), m.group(2)};

        return new String[0];
    }
}