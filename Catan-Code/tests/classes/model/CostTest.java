package classes.model;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import classes.enums.ResourceType;

public class CostTest {

    @Test
    void testRoadCostValues() {
        Cost road = Cost.roadCost();
        // Partition: Road must cost exactly 1 wood and 1 brick
        assertEquals(1, road.getWood());
        assertEquals(1, road.getBrick());
        assertEquals(0, road.getWheat());
    }

    @Test
    void testIsAffordableByPartition() {
        ResourceHand hand = new ResourceHand();
        Cost settlement = Cost.settlementCost();
        
        // Partition 1: Empty hand cannot afford settlement
        assertFalse(settlement.isAffordableBy(hand));
        
        // Partition 2: Hand with exact resources can afford it
        hand.add(ResourceType.WOOD, 1);
        hand.add(ResourceType.BRICK, 1);
        hand.add(ResourceType.WHEAT, 1);
        hand.add(ResourceType.SHEEP, 1);
        assertTrue(settlement.isAffordableBy(hand));
    }
}