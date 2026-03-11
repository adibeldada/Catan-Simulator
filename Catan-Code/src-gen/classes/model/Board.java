package classes.model;

import classes.enums.ResourceType;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the game board with tiles, vertices, and roads.
 * The board uses the specific 5-row layout and spiral numbering provided:
 * - Row 1 (Top): 13, 14, 15
 * - Row 2: 12, 4, 5, 16
 * - Row 3: 11, 3, 0, 6, 17
 * - Row 4: 10, 2, 1, 18
 * - Row 5: 9, 8, 7
 */
public class Board {
	
	protected static final int[][] VERTEX_ADJACENCY = {
	        {1,5,20}, {0,6,2}, {1,9,3}, {2,12,4}, {3,15,5}, {4,16,0},
	        {1,7,23}, {6,8,24}, {7,27,9}, {8,10,2}, {9,11,29}, {10,12,32},
	        {11,13,3}, {12,14,34}, {13,15,37}, {14,17,4}, {5,18,21}, {15,18,39},
	        {17,16,40}, {46,20,21}, {19,22,0}, {19,43,16}, {20,23,49}, {22,6,52},
	        {7,25,53}, {24,26}, {25,27}, {26,8,28}, {27,29}, {28,10,30},
	        {29,31}, {30,32}, {31,11,33}, {32,34}, {33,13,35}, {34,36},
	        {35,37}, {36,14,38}, {37,39}, {38,17,41}, {44,18,42}, {39,42},
	        {41,40}, {21,44,45}, {43,40}, {43,47}, {47,48,19}, {45,46},
	        {46,49}, {48,50,22}, {49,51}, {50,52}, {51,23,53}, {52,24}
	    };
	
    private List<Tile> tiles;
    private List<Vertex> vertices;
    private List<Road> roads;
    private Robber robber;

    public Board() {
        this.tiles = new ArrayList<>();
        this.vertices = new ArrayList<>();
        this.roads = new ArrayList<>();
    }

    public void initializeDefaultMap() {
        // Initialize 54 vertices (0-53)
        for (int i = 0; i < 54; i++) {
            vertices.add(new Vertex(i));
        }

        // Create 19 tiles with specific IDs, resources, and tokens as described
        // Row 1 (Top)
        tiles.add(new Tile(13, ResourceType.BRICK, 9));
        tiles.add(new Tile(14, ResourceType.BRICK, 8));
        tiles.add(new Tile(15, ResourceType.WHEAT, 4));

        // Row 2
        tiles.add(new Tile(12, ResourceType.WOOD, 5));
        tiles.add(new Tile(4, ResourceType.SHEEP, 11));
        tiles.add(new Tile(5, ResourceType.SHEEP, 5));
        tiles.add(new Tile(16, ResourceType.DESERT, 0)); // Desert has no number

        // Row 3 (Middle)
        tiles.add(new Tile(11, ResourceType.WHEAT, 9));
        tiles.add(new Tile(3, ResourceType.ORE, 3));
        tiles.add(new Tile(0, ResourceType.WOOD, 10)); // Center tile
        tiles.add(new Tile(6, ResourceType.SHEEP, 12));
        tiles.add(new Tile(17, ResourceType.WOOD, 17)); // Requested token 17

        // Row 4
        tiles.add(new Tile(10, ResourceType.ORE, 6));
        tiles.add(new Tile(2, ResourceType.BRICK, 8));
        tiles.add(new Tile(1, ResourceType.WHEAT, 11));
        tiles.add(new Tile(18, ResourceType.SHEEP, 10));

        // Row 5 (Bottom)
        tiles.add(new Tile(9, ResourceType.WOOD, 4));
        tiles.add(new Tile(8, ResourceType.ORE, 6));
        tiles.add(new Tile(7, ResourceType.WHEAT, 3));
        
        
     // Establish which vertices belong to which tile
        setupTileVertexAdjacencies();
        connectVerticesFromManualList();
        
        Tile desert = getTile(16);
        this.robber = new Robber(desert);

      }

    private void connectVerticesFromManualList() {
        // i is the ID of the vertex we are looking at
        for (int i = 0; i < VERTEX_ADJACENCY.length; i++) {
            for (int neighborId : VERTEX_ADJACENCY[i]) {
                addVertexConnection(i, neighborId);
            }
        }
    }

    private void setupTileVertexAdjacencies() {
        // Center Tile 0
        addTileVertices(0, new int[]{0, 1, 2, 3, 4, 5});

        // Inner ring tiles (1-6)
        addTileVertices(1, new int[]{2, 1, 6, 7, 8, 9});
        addTileVertices(2, new int[]{3, 2, 9, 10, 11, 12});
        addTileVertices(3, new int[]{4, 3, 12, 13, 14, 15});
        addTileVertices(4, new int[]{5, 4, 15, 16, 17, 18});
        addTileVertices(5, new int[]{0, 5, 16, 19, 20, 21});
        addTileVertices(6, new int[]{1, 0, 20, 22, 23, 6});
        
        // Outer ring tiles (7-18)
        addTileVertices(7, new int[]{27, 7, 8, 24, 25, 26});
        addTileVertices(8, new int[]{8, 9, 10, 29, 27, 28});
        addTileVertices(9, new int[]{10, 11, 31, 32, 29, 30});
        addTileVertices(10, new int[]{11, 12, 13, 32, 33, 34});
        addTileVertices(11, new int[]{13, 14, 37, 34, 35, 36});
        addTileVertices(12, new int[]{14, 15, 17, 37, 38, 39});
        addTileVertices(13, new int[]{17, 18, 39, 40, 41, 42});
        addTileVertices(14, new int[]{16, 18, 21, 40, 43, 44});
        addTileVertices(15, new int[]{19, 21, 43, 45, 46, 47});
        addTileVertices(16, new int[]{19, 20, 22, 46, 48, 49});
        addTileVertices(17, new int[]{22, 23, 49, 50, 51, 52}); 
        addTileVertices(18, new int[]{6, 7, 23, 24, 52, 53});
    }

    private void addTileVertices(int tileId, int[] vertexIds) {
        Tile tile = getTile(tileId);
        if (tile != null) {
            for (int id : vertexIds) {
                Vertex v = getVertex(id);
                if (v != null) {
                    tile.addAdjacentVertex(v); // Populates the tile's vertex list
                }
            }
        }
    }

    private void addVertexConnection(int v1Id, int v2Id) {
        Vertex v1 = vertices.get(v1Id);
        Vertex v2 = vertices.get(v2Id);
        if (!v1.getAdjacentVertices().contains(v2)) {
            v1.addAdjacentVertex(v2);
            v2.addAdjacentVertex(v1);
        }
    }

    public Tile getTile(int id) {
        for (Tile tile : tiles) {
            if (tile.getId() == id) { return tile; }
        }
        return null;
    }

    public Vertex getVertex(int id) {
        if (id >= 0 && id < vertices.size()) { return vertices.get(id); }
        return null;
    }
    
    public Robber getRobber() {
        return robber;
    }
    public void placeRoad(Road road) { roads.add(road); }
    public List<Tile> getTiles() { return tiles; }
    public List<Vertex> getVertices() { return vertices; }
    public List<Road> getRoads() { return roads; }
}