package CatanSimulatorDomainModel.catanUML.model;

/**
 * Represents a settlement building.
 * 
 * Settlements are the basic building type in Catan.
 * Cost: 1 wood, 1 brick, 1 wheat, 1 sheep
 * Victory Points: 1
 * Can be upgraded to a City.
 */
public class Settlement extends Building {
    
    /**
     * Constructs a Settlement owned by the specified player.
     * 
     * @param owner The player who owns this settlement
     */
    public Settlement(Player owner) {
        super(owner);
    }

    /**
     * Returns the victory points for a settlement.
     * 
     * @return 1 victory point
     */
    @Override
    public int getVictoryPoints() {
        return 1;
    }

    @Override
    public String toString() {
        return "Settlement (VP:1)";
    }
}