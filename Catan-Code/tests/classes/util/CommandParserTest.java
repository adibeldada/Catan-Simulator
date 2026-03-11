package classes.util;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.util.regex.Pattern;

/**
 * R3.3: Parser tests to demonstrate the correctness of regular expressions 
 * used for human input (R2.1).
 */
public class CommandParserTest {

    // Mirroring regex constants from HumanPlayer for isolated testing
    private static final String REGEX_ROLL = "(?i)^roll$";
    private static final String REGEX_GO = "(?i)^go$";
    private static final String REGEX_LIST = "(?i)^list$";
    private static final String REGEX_BUILD_SETTLEMENT = "(?i)^build\\s+settlement\\s+(\\d+)$";
    private static final String REGEX_BUILD_CITY = "(?i)^build\\s+city\\s+(\\d+)$";
    private static final String REGEX_BUILD_ROAD = "(?i)^build\\s+road\\s+(\\d+),\\s*(\\d+)$";

    @Test
    void testRollCommand() {
        assertTrue("roll".matches(REGEX_ROLL), "Should match lowercase roll");
        assertTrue("ROLL".matches(REGEX_ROLL), "Should match uppercase ROLL");
        assertFalse("rolls".matches(REGEX_ROLL), "Should not match partial strings");
    }

    @Test
    void testListAndGoCommands() {
        assertTrue("list".matches(REGEX_LIST));
        assertTrue("go".matches(REGEX_GO));
        assertTrue("GO".matches(REGEX_GO));
    }

    @Test
    void testBuildSettlementValid() {
        assertTrue("build settlement 10".matches(REGEX_BUILD_SETTLEMENT), "Standard settlement build");
        assertTrue("BUILD SETTLEMENT 5".matches(REGEX_BUILD_SETTLEMENT), "Case insensitivity check");
        assertTrue("build  settlement  3".matches(REGEX_BUILD_SETTLEMENT), "Should handle multiple spaces");
    }

    @Test
    void testBuildSettlementInvalid() {
        assertFalse("build settlement".matches(REGEX_BUILD_SETTLEMENT), "Missing ID");
        assertFalse("build settlement abc".matches(REGEX_BUILD_SETTLEMENT), "Non-numeric ID");
    }

    @Test
    void testBuildCityValid() {
        assertTrue("build city 42".matches(REGEX_BUILD_CITY));
        assertTrue("build city 0".matches(REGEX_BUILD_CITY));
    }

    @Test
    void testBuildRoadStandard() {
        assertTrue("build road 4, 5".matches(REGEX_BUILD_ROAD), "Standard road with comma and space");
        assertTrue("build road 10,20".matches(REGEX_BUILD_ROAD), "Road with comma and no space");
    }


    @Test
    void testBuildRoadInvalid() {
        assertFalse("build road 1 2".matches(REGEX_BUILD_ROAD), "Missing comma between IDs");
        assertFalse("build road 1,".matches(REGEX_BUILD_ROAD), "Missing second ID");
    }

    @Test
    void testMalformedCommands() {
        assertFalse("build village 1".matches(REGEX_BUILD_SETTLEMENT), "Wrong keyword");
        assertFalse("just roll".matches(REGEX_ROLL), "Leading text should fail");
    }

    @Test
    void testNumericIDCapture() {
        var matcher = Pattern.compile(REGEX_BUILD_SETTLEMENT).matcher("build settlement 15");
        assertTrue(matcher.find());
        assertEquals("15", matcher.group(1), "Should correctly capture ID 15");
    }
}