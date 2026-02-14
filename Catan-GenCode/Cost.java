package model;

/**
 * Represents the resource cost for building structures in Catan.
 * 
 * Provides factory methods for standard building costs:
 * - Road: 1 wood, 1 brick
 * - Settlement: 1 wood, 1 brick, 1 wheat, 1 sheep
 * - City: 2 wheat, 3 ore
 */
public class Cost {
    private int wood;
    private int brick;
    private int wheat;
    private int sheep;
    private int ore;

    /**
     * Constructs a Cost with specified resource amounts.
     */
    public Cost(int wood, int brick, int wheat, int sheep, int ore) {
        this.wood = wood;
        this.brick = brick;
        this.wheat = wheat;
        this.sheep = sheep;
        this.ore = ore;
    }

    /**
     * Checks if the given resource hand can afford this cost.
     * 
     * @param hand The resource hand to check
     * @return true if the hand has enough resources
     */
    public boolean isAffordableBy(ResourceHand hand) {
        return hand.hasEnough(this);
    }

    // Getters
    public int getWood() { return wood; }
    public int getBrick() { return brick; }
    public int getWheat() { return wheat; }
    public int getSheep() { return sheep; }
    public int getOre() { return ore; }

    /**
     * Factory method: Returns the cost of a road.
     * @return Cost object for a road (1 wood, 1 brick)
     */
    public static Cost roadCost() {
        return new Cost(1, 1, 0, 0, 0);
    }

    /**
     * Factory method: Returns the cost of a settlement.
     * @return Cost object for a settlement (1 wood, 1 brick, 1 wheat, 1 sheep)
     */
    public static Cost settlementCost() {
        return new Cost(1, 1, 1, 1, 0);
    }

    /**
     * Factory method: Returns the cost of a city.
     * @return Cost object for a city (2 wheat, 3 ore)
     */
    public static Cost cityCost() {
        return new Cost(0, 0, 2, 0, 3);
    }
}