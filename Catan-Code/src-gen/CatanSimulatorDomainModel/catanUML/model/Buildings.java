package CatanSimulatorDomainModel.catanUML.model;

/**
 * Abstract base class for buildings (settlements and cities).
 * 
 * Buildings are placed on vertices and provide victory points.
 * This class enforces the common interface for all building types.
 */
public abstract class Building {
    protected Player owner;
    protected Vertex location;

    /**
     * Constructs a Building owned by the specified player.
     * 
     * @param owner The player who owns this building
     */
    public Building(Player owner) {
        this.owner = owner;
    }

    /**
     * Returns the victory points this building provides.
     * Must be implemented by subclasses.
     * 
     * @return Number of victory points
     */
    public abstract int getVictoryPoints();

    /**
     * Places this building on a vertex.
     * Updates both the building's location and the vertex's building reference.
     * 
     * @param vertex The vertex where the building is placed
     */
    public void placeOn(Vertex vertex) {
        this.location = vertex;
        vertex.placeBuilding(this);
    }

    // Getters
    public Player getOwner() { return owner; }
    public Vertex getLocation() { return location; }
}