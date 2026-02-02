package CatanSimulatorDomainModel.catanUML.model;

/**
 * Represents a road connecting two vertices.
 * 
 * Roads allow players to expand their network and build settlements.
 * Cost: 1 wood, 1 brick
 * Victory Points: 0
 * 
 * Roads must be connected to existing roads or settlements (R1.6).
 */
public class Road {
    private Player owner;
    private Vertex start;
    private Vertex end;

    /**
     * Constructs a Road owned by the specified player.
     * 
     * @param owner The player who owns this road
     * @param start The starting vertex
     * @param end The ending vertex
     */
    public Road(Player owner, Vertex start, Vertex end) {
        this.owner = owner;
        this.start = start;
        this.end = end;
    }

    /**
     * Checks if this road is connected to any of the player's structures.
     * Used for validating road placement.
     * 
     * @param player The player to check
     * @return true if connected to player's structures
     */
    public boolean isConnectedTo(Player player) {
        return (start.getOwner() == player || end.getOwner() == player);
    }

    /**
     * Checks if this road connects the two given vertices.
     * 
     * @param v1 First vertex
     * @param v2 Second vertex
     * @return true if this road connects v1 and v2
     */
    public boolean connects(Vertex v1, Vertex v2) {
        return (start == v1 && end == v2) || (start == v2 && end == v1);
    }

    // Getters
    public Player getOwner() { return owner; }
    public Vertex getStart() { return start; }
    public Vertex getEnd() { return end; }

    @Override
    public String toString() {
        return String.format("Road[%d-%d]", start.getId(), end.getId());
    }
}