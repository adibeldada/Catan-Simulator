package classes.util;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

// These imports match the "classes.model" structure in your src-gen folder
import classes.model.Board;
import classes.model.Player;
import classes.model.Vertex;
import classes.model.Settlement;
import classes.model.Road;

public class RuleValidatorTest {
    private Board board;
    private RuleValidator validator;
    private Player player;

    @BeforeEach
    void setUp() {
        board = new Board();
        validator = new RuleValidator(board);
        player = new Player(1);
    }

    @Test
    void testRespectsDistanceRule_Success() {
        // Fix: Vertex requires an ID (e.g., 0)
        Vertex v = new Vertex(0); 
        assertTrue(validator.respectsDistanceRule(v));
    }

    @Test
    void testRespectsDistanceRule_Failure() {
        Vertex v1 = new Vertex(1);
        Vertex v2 = new Vertex(2);
        
        // Link vertices and occupy one
        v1.getAdjacentVertices().add(v2);
        v2.setBuilding(new Settlement(new Player(2)));
        
        // Should fail because neighbor v2 is occupied
        assertFalse(validator.respectsDistanceRule(v1));
    }

    @Test
    void testCanBuildCity_FailOnEmptyVertex() {
        Vertex v = new Vertex(3);
        // R1.6: Must have a settlement first
        assertFalse(validator.canBuildCity(player, v));
    }

    @Test
    void testCanBuildCity_Success() {
        Vertex v = new Vertex(4);
        Settlement s = new Settlement(player);
        v.setBuilding(s);
        
        // Should pass if player owns the settlement
        assertTrue(validator.canBuildCity(player, v));
    }

    @Test
    void testCanBuildRoad_Failure_NotAdjacent() {
        Vertex v1 = new Vertex(5);
        Vertex v2 = new Vertex(6);
        // Vertices are NOT linked, so road building should fail
        assertFalse(validator.canBuildRoad(player, v1, v2));
    }

    @Test
    void testCanBuildRoad_Boundary_FirstMove() {
        Vertex v1 = new Vertex(7);
        Vertex v2 = new Vertex(8);
        v1.getAdjacentVertices().add(v2);
        
        // Boundary: The very first road needs no existing connections
        assertTrue(validator.canBuildRoad(player, v1, v2));
    }
}