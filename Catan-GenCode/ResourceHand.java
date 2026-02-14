package util;

import java.util.Random;

/**
 * Simulates rolling two six-sided dice.
 * 
 * Used by GameMaster to determine resource production each turn.
 */
public class Dice {
    private int die1;
    private int die2;
    private Random random;

    /**
     * Constructs a new Dice object with a random number generator.
     */
    public Dice() {
        this.random = new Random();
        this.die1 = 1;
        this.die2 = 1;
    }

    /**
     * Rolls both dice and returns the sum.
     * 
     * @return The sum of two six-sided dice (2-12)
     */
    public int roll() {
        die1 = random.nextInt(6) + 1;
        die2 = random.nextInt(6) + 1;
        return die1 + die2;
    }

    /**
     * Returns the sum of the last roll.
     * 
     * @return The last rolled sum
     */
    public int getLastRoll() {
        return die1 + die2;
    }

    public int getDie1() {
        return die1;
    }

    public int getDie2() {
        return die2;
    }
}