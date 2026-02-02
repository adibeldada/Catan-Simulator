package model;

/**
 * Represents a city building.
 * 
 * Cities are upgraded settlements in Catan.
 * Cost: 2 wheat, 3 ore (plus the existing settlement)
 * Victory Points: 2
 * Cities produce twice as many resources as settlements.
 */
public class City extends Building {
    
    /**
     * Constructs a City owned by the specified player.
     * 
     * @param owner The player who owns this city
     */
    public City(Player owner) {
        super(owner);
    }

    /**
     * Returns the victory points for a city.
     * 
     * @return 2 victory points
     */
    @Override
    public int getVictoryPoints() {
        return 2;
    }

    @Override
    public String toString() {
        return "City (VP:2)";
    }
}