package classes.model;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SettlementTest {
    private Player player;
    private Settlement settlement;
    private Vertex vertex;

    @BeforeEach
    void setUp() {
        player = new AIPlayer(1); // Using AIPlayer as a concrete implementation of Player
        settlement = new Settlement(player);
        vertex = new Vertex(10);
    }

    @Test
    void testGetVictoryPoints() {
        // R1.3: Settlements must provide exactly 1 victory point
        assertEquals(1, settlement.getVictoryPoints(), "Settlement should provide 1 VP");
    }

    @Test
    void testGetOwner() {
        assertEquals(player, settlement.getOwner(), "Owner should match the player assigned in constructor");
    }

    @Test
    void testPlaceOn() {
        settlement.placeOn(vertex);
        
        // Verify bidirectional relationship
        assertEquals(vertex, settlement.getLocation(), "Settlement location should be updated to the vertex");
        assertEquals(settlement, vertex.getBuilding(), "Vertex building reference should point to the settlement");
    }

    @Test
    void testToString() {
        assertEquals("Settlement (VP:1)", settlement.toString(), "String representation should match specification");
    }
}