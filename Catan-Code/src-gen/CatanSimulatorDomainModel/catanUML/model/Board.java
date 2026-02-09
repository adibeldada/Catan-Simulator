package CatanSimulatorDomainModel.catanUML.model;

import CatanSimulatorDomainModel.catanUML.enums.ResourceType;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the game board with tiles, vertices, and roads.
 * * The board uses a specific identification system (R1.1):
 * - Tiles: 0 (center), 1-6 (inner ring), 7-18 (outer ring)
 * - Vertices: 0-53 (following the same ring pattern)
 */
public class Board {
    private List<Tile> tiles;
    private List<Vertex> vertices;
    private List<Road> roads;

    public Board() {
        this.tiles = new ArrayList<>();
        this.vertices = new ArrayList<>();
        this.roads = new ArrayList<>();
    }

    public void initializeDefaultMap() {
        for (int i = 0; i < 54; i++) {
            vertices.add(new Vertex(i));
        }

        setupVertexAdjacencies();

        // Create tiles with resources and number tokens
        tiles.add(new Tile(0, ResourceType.WHEAT, 6)); // Center

        // Inner ring (1-6)
        tiles.add(new Tile(1, ResourceType.ORE, 5));
        tiles.add(new Tile(2, ResourceType.SHEEP, 10));
        tiles.add(new Tile(3, ResourceType.BRICK, 8));
        tiles.add(new Tile(4, ResourceType.WOOD, 3));
        tiles.add(new Tile(5, ResourceType.WHEAT, 4));
        tiles.add(new Tile(6, ResourceType.SHEEP, 9));

        // Outer ring (7-18)
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

        setupTileVertexAdjacencies();
    }

    private void setupVertexAdjacencies() {
        // Center Ring (0-5)
        for (int i = 0; i <= 5; i++) {
            addVertexConnection(i, (i + 1) % 6);
            addVertexConnection(i, i + 6); // Bridge to Inner Ring
        }
        
        // Inner Ring (6-23)
        for (int i = 6; i <= 23; i++) {
            int next = (i == 23) ? 6 : i + 1;
            addVertexConnection(i, next);
            
            // Bridge to Outer Ring (Bridge every second node for standard Catan grid)
            if (i % 2 == 0) {
                int outerNode = 24 + (i - 6) * 3 / 2;
                addVertexConnection(i, Math.min(outerNode, 53));
            }
        }
        
        // Outer Ring (24-53)
        for (int i = 24; i <= 53; i++) {
            int next = (i == 53) ? 24 : i + 1;
            addVertexConnection(i, next);
        }
    }

    private void addVertexConnection(int v1Id, int v2Id) {
        if (v1Id < vertices.size() && v2Id < vertices.size()) {
            Vertex v1 = vertices.get(v1Id);
            Vertex v2 = vertices.get(v2Id);
            v1.addAdjacentVertex(v2);
            v2.addAdjacentVertex(v1);
        }
    }

    private void setupTileVertexAdjacencies() {
        // Tile 0 (center)
        addTileVertices(0, new int[]{0, 1, 2, 3, 4, 5});

        // Inner ring (1-6)
        addTileVertices(1, new int[]{0, 1, 6, 7, 8, 9});
        addTileVertices(2, new int[]{1, 2, 9, 10, 11, 12});
        addTileVertices(3, new int[]{2, 3, 12, 13, 14, 15});
        addTileVertices(4, new int[]{3, 4, 15, 16, 17, 18});
        addTileVertices(5, new int[]{4, 5, 18, 19, 20, 21});
        addTileVertices(6, new int[]{5, 0, 21, 22, 23, 6});
        
        // Outer ring (7-18) - Fully implemented
        addTileVertices(7, new int[]{6, 7, 8, 24, 25, 26});
        addTileVertices(8, new int[]{8, 9, 10, 26, 27, 28});
        addTileVertices(9, new int[]{10, 11, 12, 28, 29, 30});
        addTileVertices(10, new int[]{12, 13, 14, 30, 31, 32});
        addTileVertices(11, new int[]{14, 15, 16, 32, 33, 34});
        addTileVertices(12, new int[]{16, 17, 18, 34, 35, 36});
        addTileVertices(13, new int[]{18, 19, 20, 36, 37, 38});
        addTileVertices(14, new int[]{20, 21, 22, 38, 39, 40});
        addTileVertices(15, new int[]{22, 23, 6, 40, 41, 42});
        addTileVertices(16, new int[]{24, 43, 44, 45, 25, 26}); // Edge mapping
        addTileVertices(17, new int[]{26, 27, 28, 46, 47, 48}); 
        addTileVertices(18, new int[]{28, 29, 30, 49, 50, 51});
    }

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

    public void placeRoad(Road road) { roads.add(road); }

    public void placeBuilding(Buildings building) {
        // Handled through Vertex.placeBuilding
    }

    public List<Tile> getTiles() { return tiles; }
    public List<Vertex> getVertices() { return vertices; }
    public List<Road> getRoads() { return roads; }
}