package model;

import enums.ResourceType;

/**
 * Tracks the resources held by a player.
 * 
 * Manages addition, removal, and querying of resource cards.
 * Used by Player class to manage their hand of resources.
 */
public class ResourceHand {
    private int wood;
    private int brick;
    private int wheat;
    private int sheep;
    private int ore;

    /**
     * Constructs an empty resource hand.
     */
    public ResourceHand() {
        this.wood = 0;
        this.brick = 0;
        this.wheat = 0;
        this.sheep = 0;
        this.ore = 0;
    }

    /**
     * Adds a specified amount of a resource to the hand.
     * 
     * @param resource The type of resource to add
     * @param amount The amount to add
     */
    public void add(ResourceType resource, int amount) {
        switch (resource) {
            case WOOD:
                wood += amount;
                break;
            case BRICK:
                brick += amount;
                break;
            case WHEAT:
                wheat += amount;
                break;
            case SHEEP:
                sheep += amount;
                break;
            case ORE:
                ore += amount;
                break;
            case DESERT:
                // Desert doesn't produce resources
                break;
        }
    }

    /**
     * Removes a specified amount of a resource from the hand.
     * Will not go below zero.
     * 
     * @param resource The type of resource to remove
     * @param amount The amount to remove
     */
    public void remove(ResourceType resource, int amount) {
        switch (resource) {
            case WOOD:
                wood = Math.max(0, wood - amount);
                break;
            case BRICK:
                brick = Math.max(0, brick - amount);
                break;
            case WHEAT:
                wheat = Math.max(0, wheat - amount);
                break;
            case SHEEP:
                sheep = Math.max(0, sheep - amount);
                break;
            case ORE:
                ore = Math.max(0, ore - amount);
                break;
            case DESERT:
                // Desert doesn't produce resources
                break;
        }
    }

    /**
     * Returns the total number of resource cards in the hand.
     * 
     * @return Sum of all resources
     */
    public int totalCards() {
        return wood + brick + wheat + sheep + ore;
    }

    /**
     * Checks if the hand has enough resources to cover the given cost.
     * 
     * @param cost The cost to check against
     * @return true if the hand has at least the required amount of each resource
     */
    public boolean hasEnough(Cost cost) {
        return wood >= cost.getWood() &&
               brick >= cost.getBrick() &&
               wheat >= cost.getWheat() &&
               sheep >= cost.getSheep() &&
               ore >= cost.getOre();
    }

    // Getters
    public int getWood() { return wood; }
    public int getBrick() { return brick; }
    public int getWheat() { return wheat; }
    public int getSheep() { return sheep; }
    public int getOre() { return ore; }

    @Override
    public String toString() {
        return String.format("Wood:%d Brick:%d Wheat:%d Sheep:%d Ore:%d (Total:%d)", 
                           wood, brick, wheat, sheep, ore, totalCards());
    }
}