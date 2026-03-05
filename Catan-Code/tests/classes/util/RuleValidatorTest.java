package classes.util;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import classes.model.*;
import classes.enums.ResourceType;

/**
 * R1.6: Unit tests for the RuleValidator to ensure game invariants are maintained.
 * This class uses partition testing to verify resource costs, connectivity, and distance rules.
 */
public class RuleValidatorTest {
    private Board board;
    private RuleValidator validator;
    private Player player;

    @BeforeEach
    void setUp() {
        board = new Board();
        validator = new RuleValidator(board);
        player = new AIPlayer(1);
        
        // Give the player unlimited resources so affordability checks pass in tests
        for (ResourceType type : ResourceType.values()) {
            player.collectResource(type, 100);
        }
    }

    @Test
    void testRespectsDistanceRule_Success() {
        Vertex v = new Vertex(0); 
        assertTrue(validator.respectsDistanceRule(v), "Distance rule should pass for an empty vertex with no neighbors.");
    }

    @Test
    void testRespectsDistanceRule_Failure() {
        Vertex v1 = new Vertex(1);
        Vertex v2 = new Vertex(2);
        
        // Link vertices
        v1.addAdjacentVertex(v2);
        v2.addAdjacentVertex(v1);
        
        // Occupy neighbor v2 with an opponent's building
        Player opponent = new AIPlayer(2);
        new Settlement(opponent).placeOn(v2);
        
        // Should fail because neighbor v2 is occupied
        assertFalse(validator.respectsDistanceRule(v1), "Distance rule should fail if an adjacent vertex is occupied.");
    }

    @Test
    void testCanBuildCity_FailOnEmptyVertex() {
        Vertex v = new Vertex(3);
        // R1.6: Must have a settlement first
        assertFalse(validator.canBuildCity(player, v), "Should not be able to build a city on an empty vertex.");
    }

    @Test
    void testCanBuildCity_Success() {
        Vertex v = new Vertex(4);
        Settlement s = new Settlement(player);
        s.placeOn(v); // Use placeOn to establish bidirectional reference
        
        assertTrue(validator.canBuildCity(player, v), "City building should pass if player owns a settlement at the location.");
    }

    @Test
    void testCanBuildRoad_Failure_NotAdjacent() {
        Vertex v1 = new Vertex(5);
        Vertex v2 = new Vertex(6);
        // Vertices are NOT linked, so road building must fail
        assertFalse(validator.canBuildRoad(player, v1, v2), "Should fail to build a road between non-adjacent vertices.");
    }

    @Test
    void testCanBuildRoad_Success_ConnectedToBuilding() {
        Vertex v1 = new Vertex(7);
        Vertex v2 = new Vertex(8);
        v1.addAdjacentVertex(v2);
        v2.addAdjacentVertex(v1);
        
        // Connectivity: Road must touch a building owned by the player
        new Settlement(player).placeOn(v1);
        
        assertTrue(validator.canBuildRoad(player, v1, v2), "Should pass when the road is connected to the player's settlement.");
    }

    @Test
    void testCanBuildSettlement_DistanceRuleViolation() {
        Vertex v1 = new Vertex(10);
        Vertex v2 = new Vertex(11);
        v1.addAdjacentVertex(v2);
        v2.addAdjacentVertex(v1);
        
        // Place an opponent's settlement at v1
        new Settlement(new AIPlayer(2)).placeOn(v1);
        
        // Try to build a settlement at v2
        assertFalse(validator.canBuildSettlement(player, v2), "Should fail due to distance rule violation (neighbor v1 occupied).");
    }
}