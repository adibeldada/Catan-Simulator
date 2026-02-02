package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a vertex on the board where buildings can be placed.
 * 
 * Vertices are intersection points on the board.
 * Each vertex has a unique ID (0-53 in standard Catan).
 * Vertices can hold one building (Settlement or City).
 * Adjacent vertices are stored to enforce the distance rule.
 */
public class Vertex {
    private int id;
    private Player owner;
    private Building building;
    private List<Vertex> adjacentVertices;

    /**
     * Constructs a Vertex with the given ID.
     * 
     * @param id Unique identifier for this vertex
     */
    public Vertex(int id) {
        this.id = id;
        this.owner = null;
        this.building = null;
        this.adjacentVertices = new ArrayList<>();
    }

    /**
     * Checks if this vertex is occupied by a building.
     * 
     * @return true if a building is present
     */
    public boolean isOccupied() {
        return building != null;
    }

    /**
     * Checks if the player can build at this vertex.
     * Must be unoccupied and respect the distance rule.
     * 
     * @param player The player attempting to build
     * @return true if building is allowed
     */
    public boolean canBuild(Player player) {
        if (isOccupied()) {
            return false;
        }
        
        // Check distance rule: adjacent vertices must be empty
        for (Vertex adjacent : adjacentVertices) {
            if (adjacent.isOccupied()) {
                return false;
            }
        }
        
        return true;
    }

    /**
     * Places a building at this vertex.
     * 
     * @param building The building to place
     */
    public void placeBuilding(Building building) {
        this.building = building;
        this.owner = building.getOwner();
    }

    /**
     * Adds an adjacent vertex to this vertex's adjacency list.
     * 
     * @param vertex The vertex to add as adjacent
     */
    public void addAdjacentVertex(Vertex vertex) {
        if (!adjacentVertices.contains(vertex)) {
            adjacentVertices.add(vertex);
        }
    }

    // Getters and setters
    public int getId() { return id; }
    public Player getOwner() { return owner; }
    public Building getBuilding() { return building; }
    public List<Vertex> getAdjacentVertices() { return adjacentVertices; }
    
    public void setOwner(Player owner) { this.owner = owner; }
    public void setBuilding(Building building) { this.building = building; }
}