package classes.model;

import classes.enums.ResourceType;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;

/**
 * Tracks the resources held by a player.
 * * Manages addition, removal, and querying of resource cards.
 * Used by Player class to manage their hand of resources.
 */
public class ResourceHand {
    private int wood;
    private int brick;
    private int wheat;
    private int sheep;
    private int ore;

    public ResourceHand() {
        this.wood = 0;
        this.brick = 0;
        this.wheat = 0;
        this.sheep = 0;
        this.ore = 0;
    }

    /**
     * Adds a specified method to return the count of a specific resource type.
     */
    public int getCount(ResourceType resource) {
        return switch (resource) {
            case WOOD -> wood;
            case BRICK -> brick;
            case WHEAT -> wheat;
            case SHEEP -> sheep;
            case ORE -> ore;
            default -> 0;
        };
    }

    public void add(ResourceType resource, int amount) {
        switch (resource) {
            case WOOD: wood += amount; break;
            case BRICK: brick += amount; break;
            case WHEAT: wheat += amount; break;
            case SHEEP: sheep += amount; break;
            case ORE: ore += amount; break;
            case DESERT: break;
        }
    }

    /**
     * EXISTING METHOD: Note the name is 'remove', not 'removeResource'.
     */
    public void remove(ResourceType resource, int amount) {
        switch (resource) {
            case WOOD: wood = Math.max(0, wood - amount); break;
            case BRICK: brick = Math.max(0, brick - amount); break;
            case WHEAT: wheat = Math.max(0, wheat - amount); break;
            case SHEEP: sheep = Math.max(0, sheep - amount); break;
            case ORE: ore = Math.max(0, ore - amount); break;
            case DESERT: break;
        }
    }

    public int totalCards() {
        return wood + brick + wheat + sheep + ore;
    }

    public boolean hasEnough(Cost cost) {
        return wood >= cost.getWood() &&
               brick >= cost.getBrick() &&
               wheat >= cost.getWheat() &&
               sheep >= cost.getSheep() &&
               ore >= cost.getOre();
    }

    public int getWood() { return wood; }
    public int getBrick() { return brick; }
    public int getWheat() { return wheat; }
    public int getSheep() { return sheep; }
    public int getOre() { return ore; }
    
    public ResourceType removeRandomCard() {
        if (totalCards() == 0) return null;
        List<ResourceType> available = new ArrayList<>();
        if (wood > 0) available.add(ResourceType.WOOD);
        if (brick > 0) available.add(ResourceType.BRICK);
        if (wheat > 0) available.add(ResourceType.WHEAT);
        if (sheep > 0) available.add(ResourceType.SHEEP);
        if (ore > 0) available.add(ResourceType.ORE);
        
        ResourceType selected = available.get(new Random().nextInt(available.size()));
        remove(selected, 1);
        return selected;
    }

    public void discardRandomCards(int count) {
        for (int i = 0; i < count; i++) {
            removeRandomCard();
        }
    }

    @Override
    public String toString() {
        return String.format("Wood:%d Brick:%d Wheat:%d Sheep:%d Ore:%d (Total:%d)", 
                           wood, brick, wheat, sheep, ore, totalCards());
    }
}