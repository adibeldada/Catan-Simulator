package classes.model;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BuildingsTest {
    private Player player;
    private Vertex vertex;

    @BeforeEach
    void setUp() {
        player = new AIPlayer(1);
        vertex = new Vertex(5);
    }

    @Test
    void testBuildingInitialization() {
        // Testing abstract class via an anonymous implementation
        Buildings building = new Buildings(player) {
            @Override
            public int getVictoryPoints() { return 0; }
        };
        
        assertEquals(player, building.getOwner(), "Building should store the owner correctly.");
        assertNull(building.getLocation(), "New building should not have a location initially.");
    }

    @Test
    void testPlaceOnUpdatesBidirectionalLink() {
        Buildings settlement = new Settlement(player);
        settlement.placeOn(vertex);

        assertEquals(vertex, settlement.getLocation(), "Building should know its location.");
        assertEquals(settlement, vertex.getBuilding(), "Vertex should know what building is on it.");
    }
}