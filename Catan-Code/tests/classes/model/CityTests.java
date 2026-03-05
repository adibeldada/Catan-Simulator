package classes.model;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CityTest {
    private Player player;
    private City city;
    private Vertex vertex;

    @BeforeEach
    void setUp() {
        player = new AIPlayer(2);
        city = new City(player);
        vertex = new Vertex(20);
    }

    @Test
    void testGetVictoryPoints() {
        // R1.3: Cities must provide exactly 2 victory points
        assertEquals(2, city.getVictoryPoints(), "City should provide 2 VPs");
    }

    @Test
    void testGetOwner() {
        assertEquals(player, city.getOwner(), "Owner should match the player assigned in constructor");
    }

    @Test
    void testPlaceOn() {
        city.placeOn(vertex);
        
        // Verify bidirectional relationship
        assertEquals(vertex, city.getLocation(), "City location should be updated to the vertex");
        assertEquals(city, vertex.getBuilding(), "Vertex building reference should point to the city");
    }

    @Test
    void testToString() {
        assertEquals("City (VP:2)", city.toString(), "String representation should match specification");
    }
}