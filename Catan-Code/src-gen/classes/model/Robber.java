package classes.model;

/**
 * R2.5: Represents the Robber in the game.
 * The Robber blocks resource production on the tile it currently occupies.
 */
public class Robber {
    private Tile currentTile;

    /**
     * Initializes the Robber on a starting tile (usually the Desert).
     * @param initialTile The tile where the Robber starts.
     */
    public Robber(Tile initialTile) {
        this.currentTile = initialTile;
    }

    /**
     * Moves the Robber to a new tile.
     * @param newTile The destination tile.
     */
    public void moveTo(Tile newTile) {
        this.currentTile = newTile;
    }

    /**
     * Gets the tile currently occupied by the Robber.
     * @return The current Tile object.
     */
    public Tile getCurrentTile() {
        return currentTile;
    }
    
    /**
     * Checks if the Robber is currently on a specific tile.
     * @param tile The tile to check.
     * @return true if the Robber is on the given tile, false otherwise.
     */
    public boolean isBlocking(Tile tile) {
        return currentTile != null && currentTile.equals(tile);
    }
}