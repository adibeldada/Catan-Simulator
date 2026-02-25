package classes.model;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

public class BoardTest {
    private Board board;

    @BeforeEach
    void setUp() {
        board = new Board();
        board.initializeDefaultMap(); // Sets up the 54-vertex map
    }

    @Test
    void testVertexCountBoundary() {
        // R1.1: Map must have exactly 54 vertices (0-53)
        assertEquals(54, board.getVertices().size(), "Board should have 54 vertices");
    }

    @Test
    void testVertexIdBoundary() {
        // Boundary: Check the very last valid vertex ID
        assertNotNull(board.getVertex(53), "Vertex 53 should exist");
        
        // Boundary: Check the first invalid vertex ID
        assertNull(board.getVertex(54), "Vertex 54 should not exist");
    }

    @Test
    void testTileCountBoundary() {
        // R1.1: Map must have 19 tiles (0-18)
        assertEquals(19, board.getTiles().size(), "Board should have 19 tiles");
    }
}