package CatanSimulatorDomainModel.catanUML.model;

import CatanSimulatorDomainModel.catanUML.enums.ResourceType;
import java.util.ArrayList;
import java.util.List;
/**
 * Import CatanSimulatorDomainModel.catanUML.ts a resource tile on the board.
 * 
 * Each tile has a resource type and a number token (2-12).
 * When the number is rolled, the tile produces resources for
 * players with buildings on adjacent vertices.
 * 
 * Tiles are identified by ID: 0 (center), 1-6 (inner ring), 7-18 (outer ring).
 */
public class Tile {
    private int id;
    private ResourceType resourceType;
    private int numberToken;
    private List<Vertex> adjacentVertices;

    /**
     * Constructs a Tile with specified properties.
     * 
     * @param id Unique identifier for this tile
     * @param resourceType The type of resource this tile produces
     * @param numberToken The number (2-12) that triggers production
     */
    public Tile(int id, ResourceType resourceType, int numberToken) {
        this.id = id;
        this.resourceType = resourceType;
        this.numberToken = numberToken;
        this.adjacentVertices = new ArrayList<>();
    }

    /**
     * Checks if this tile produces resources on the given roll.
     * DESERT tiles never produce resources.
     * 
     * @param roll The dice roll (2-12)
     * @return true if this tile produces on the given roll
     */
    public boolean producesOnRoll(int roll) {
        return numberToken == roll && resourceType != ResourceType.DESERT;
    }

    /**
     * Adds an adjacent vertex to this tile.
     * Used during board initialization.
     * 
     * @param vertex The vertex adjacent to this tile
     */
    public void addAdjacentVertex(Vertex vertex) {
        if (!adjacentVertices.contains(vertex)) {
            adjacentVertices.add(vertex);
        }
    }

    // Getters
    public int getId() { return id; }
    public ResourceType getResourceType() { return resourceType; }
    public int getNumberToken() { return numberToken; }
    public List<Vertex> getAdjacentVertices() { return adjacentVertices; }

    @Override
    public String toString() {
        return String.format("Tile[%d:%s(%d)]", id, resourceType, numberToken);
    }
}