package classes.model;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import classes.enums.ResourceType;

public class ResourceHandTest {
    private ResourceHand hand;

    @BeforeEach
    void setUp() {
        // Boundary: Starts with exactly 0 of every resource
        hand = new ResourceHand();
    }

    // --- PARTITION: Adding Resources ---

    @Test
    void testAddResources() {
        hand.add(ResourceType.WOOD, 5);
        // Partition: Basic addition of a single resource type
        assertEquals(5, hand.getWood(), "Wood count should be 5");
        assertEquals(5, hand.totalCards(), "Total cards should be 5");
    }

    @Test
    void testAddMultipleResources() {
        hand.add(ResourceType.WOOD, 2);
        hand.add(ResourceType.ORE, 3);
        // Partition: Adding multiple different resource types
        assertEquals(5, hand.totalCards(), "Total cards should correctly sum multiple types");
    }

    // --- PARTITION/BOUNDARY: Removing Resources ---

    @Test
    void testRemoveExactAmount() {
        hand.add(ResourceType.BRICK, 3);
        hand.remove(ResourceType.BRICK, 3);
        // Boundary: Removing exactly the amount available results in 0
        assertEquals(0, hand.getBrick(), "Brick count should be exactly 0");
    }

    @Test
    void testRemovePartitionNegativeResult() {
        hand.add(ResourceType.SHEEP, 2);
        hand.remove(ResourceType.SHEEP, 5);
        // Partition/Boundary: Logic ensures resource count does not go below zero
        assertEquals(0, hand.getSheep(), "Resource count should bottom out at 0, not negative");
    }

    // --- PARTITION: Affordability (hasEnough) ---

    @Test
    void testHasEnough_ExactlyEnough() {
        // Setup: Exactly the cost of a road (1 Wood, 1 Brick)
        hand.add(ResourceType.WOOD, 1);
        hand.add(ResourceType.BRICK, 1);
        
        // Boundary: Having exactly the required amount should return true
        assertTrue(hand.hasEnough(Cost.roadCost()), "Should afford road with exact resources");
    }

    @Test
    void testHasEnough_NotEnough() {
        // Setup: 1 card short for a road
        hand.add(ResourceType.WOOD, 1);
        
        // Partition: Lacking one type of required resource should return false
        assertFalse(hand.hasEnough(Cost.roadCost()), "Should not afford road missing a resource");
    }
}