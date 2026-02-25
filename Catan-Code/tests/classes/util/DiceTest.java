package classes.util;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

public class DiceTest {

    @Test
    @DisplayName("Boundary: Dice sum is between 2 and 12")
    void testRollRange() {
        Dice dice = new Dice();
        for (int i = 0; i < 1000; i++) {
            int roll = dice.roll();
            // These are your boundaries
            assertTrue(roll >= 2, "Roll sum was too low: " + roll);
            assertTrue(roll <= 12, "Roll sum was too high: " + roll);
        }
    }

    @Test
    @DisplayName("Boundary: Individual dice are 1-6")
    void testIndividualDice() {
        Dice dice = new Dice();
        dice.roll();
        assertTrue(dice.getDie1() >= 1 && dice.getDie1() <= 6);
        assertTrue(dice.getDie2() >= 1 && dice.getDie2() <= 6);
    }
    
    @Test
    @DisplayName("Boundary: Sum of Dice")
    void testGetLastRoll() {
        Dice dice = new Dice();
        dice.roll();
        assertTrue(dice.getLastRoll() <= 12);
    }
}