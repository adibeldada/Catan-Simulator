package CatanSimulatorDomainModel.catanUML.model;

import CatanSimulatorDomainModel.catanUML.enums.ResourceType;
import java.util.ArrayList;
import java.util.List;
/**
 * Represents the game board with tiles, vertices, and roads.
 * 
 * The board uses a specific identification system (R1.1):
 * - Tiles: 0 (center), 1-6 (inner ring), 7-18 (outer ring)
 * - Vertices: 0-53 (following the same ring pattern)
 * 
 * This implementation uses a hard-coded default map for consistency.
 */
public class Board {
    private List<Tile> tiles;
    private List<Vertex> vertices;
    private List<Road> roads;

    /**
     * Constructs an empty Board.
     */
    public Board() {
        this.tiles = new ArrayList<>();
        this.vertices = new ArrayList<>();
        this.roads = new ArrayList<>();
    }

    /**
     * Initializes a default Catan map following the specification.
     * R1.1: Uses the specified tile and vertex identification system.
     * 
     * This method sets up:
     * - 54 vertices with adjacency relationships
     * - 19 tiles with resource types and number tokens
     * - Tile-to-vertex adjacency mappings
     */
    public void initializeDefaultMap() {
        // Create vertices (0-53 based on standard Catan board)
        for (int i = 0; i < 54; i++) {
            vertices.add(new Vertex(i));
        }

        // Setup adjacency relationships for vertices
        setupVertexAdjacencies();

        // Create tiles with resources and number tokens
        // Tile 0 (center)
        tiles.add(new Tile(0, ResourceType.WHEAT, 6));

        // Inner ring (tiles 1-6)
        tiles.add(new Tile(1, ResourceType.ORE, 5));
        tiles.add(new Tile(2, ResourceType.SHEEP, 10));
        tiles.add(new Tile(3, ResourceType.BRICK, 8));
        tiles.add(new Tile(4, ResourceType.WOOD, 3));
        tiles.add(new Tile(5, ResourceType.WHEAT, 4));
        tiles.add(new Tile(6, ResourceType.SHEEP, 9));

        // Outer ring (tiles 7-18)
        tiles.add(new Tile(7, ResourceType.WOOD, 11));
        tiles.add(new Tile(8, ResourceType.BRICK, 4));
        tiles.add(new Tile(9, ResourceType.SHEEP, 9));
        tiles.add(new Tile(10, ResourceType.WHEAT, 12));
        tiles.add(new Tile(11, ResourceType.ORE, 6));
        tiles.add(new Tile(12, ResourceType.DESERT, 0));
        tiles.add(new Tile(13, ResourceType.WOOD, 5));
        tiles.add(new Tile(14, ResourceType.BRICK, 10));
        tiles.add(new Tile(15, ResourceType.ORE, 3));
        tiles.add(new Tile(16, ResourceType.SHEEP, 8));
        tiles.add(new Tile(17, ResourceType.WHEAT, 11));
        tiles.add(new Tile(18, ResourceType.WOOD, 2));

        // Setup tile-vertex adjacencies
        setupTileVertexAdjacencies();
    }

    /**
     * Sets up adjacency relationships between vertices.
     * This follows the standard Catan board layout.
     * 
     * Note: This is a simplified implementation. A full production version
     * would need all 54 vertices properly connected.
     */
 // Partial fix for Board.java to allow more movement
    private void setupVertexAdjacencies() {
        // This now covers a much larger portion of the 54 vertices to prevent "dead ends"
        for (int i = 0; i <= 5; i++) {
            addVertexConnection(i, (i + 1) % 6); // Center Ring
            addVertexConnection(i, i + 7);       // Connections to Inner Ring
        }
        
        // Connecting Inner Ring (6-23)
        for (int i = 6; i <= 23; i++) {
            if (i < 23) addVertexConnection(i, i + 1);
            else addVertexConnection(23, 6);
            
            // Connections to Outer Ring (24-53)
            if (i % 2 == 0) addVertexConnection(i, i + 18); 
        }
        
        // Connecting Outer Ring (24-53)
        for (int i = 24; i < 53; i++) {
            addVertexConnection(i, i + 1);
        }
        addVertexConnection(53, 24);
    }

    /**
     * Adds a bidirectional connection between two vertices.
     * 
     * @param v1Id ID of first vertex
     * @param v2Id ID of second vertex
     */
    private void addVertexConnection(int v1Id, int v2Id) {
        if (v1Id < vertices.size() && v2Id < vertices.size()) {
            Vertex v1 = vertices.get(v1Id);
            Vertex v2 = vertices.get(v2Id);
            v1.addAdjacentVertex(v2);
            v2.addAdjacentVertex(v1);
        }
    }

    /**
     * Sets up which vertices are adjacent to which tiles.
     * This is used for resource production.
     */
    private void setupTileVertexAdjacencies() {
        // Tile 0 (center) - vertices 0-5 (hexagon)
        addTileVertices(0, new int[]{0, 1, 2, 3, 4, 5});

        // Inner ring tiles to vertices mapping
        // Tile 1
        addTileVertices(1, new int[]{0, 1, 6, 7, 8, 9});
        // Tile 2
        addTileVertices(2, new int[]{1, 2, 9, 10, 11, 12});
        // Tile 3
        addTileVertices(3, new int[]{2, 3, 12, 13, 14, 15});
        // Tile 4
        addTileVertices(4, new int[]{3, 4, 15, 16, 17, 18});
        // Tile 5
        addTileVertices(5, new int[]{4, 5, 18, 19, 20, 21});
        // Tile 6
        addTileVertices(6, new int[]{5, 0, 21, 22, 23, 6});
        
        // Outer ring tiles (examples - full implementation needed)
        // Tile 7
        addTileVertices(7, new int[]{6, 7, 8, 24, 25, 26});
        // Tile 8
        addTileVertices(8, new int[]{8, 9, 10, 26, 27, 28});
        
        // Continue for remaining tiles...
        // (In production, all 19 tiles would be properly mapped)
    }

    /**
     * Helper method to add multiple vertices to a tile.
     * 
     * @param tileId ID of the tile
     * @param vertexIds Array of vertex IDs adjacent to this tile
     */
    private void addTileVertices(int tileId, int[] vertexIds) {
        if (tileId < tiles.size()) {
            Tile tile = tiles.get(tileId);
            for (int vertexId : vertexIds) {
                if (vertexId < vertices.size()) {
                    tile.addAdjacentVertex(vertices.get(vertexId));
                }
            }
        }
    }

    /**
     * Gets a tile by its ID.
     * 
     * @param id The tile ID to find
     * @return The tile with the given ID, or null if not found
     */
    public Tile getTile(int id) {
        for (Tile tile : tiles) {
            if (tile.getId() == id) {
                return tile;
            }
        }
        return null;
    }

    /**
     * Gets a vertex by its ID.
     * 
     * @param id The vertex ID to find
     * @return The vertex with the given ID, or null if not found
     */
    public Vertex getVertex(int id) {
        if (id >= 0 && id < vertices.size()) {
            return vertices.get(id);
        }
        return null;
    }

    /**
     * Places a road on the board.
     * 
     * @param road The road to place
     */
    public void placeRoad(Road road) {
        roads.add(road);
    }

    /**
     * Places a building on the board (handled through vertex).
     * This method exists for consistency with the UML diagram.
     * 
     * @param building The building to place
     */
    public void placeBuilding(Buildings building) {
        // Building placement is handled through the vertex
        // This method is included for UML compliance
    }

    // Getters
    public List<Tile> getTiles() { return tiles; }
    public List<Vertex> getVertices() { return vertices; }
    public List<Road> getRoads() { return roads; }
}