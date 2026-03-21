package classes.moves;

import classes.model.ResourceHand;

/**
 * Concrete Visitor that evaluates the immediate value of a player action.
 * Implements R3.2 value scoring:
 *   - Earning a VP (settlement, city): 1.0
 *   - Building without earning a VP (road): 0.8
 *   - Spending cards such that fewer than 5 remain: 0.5
 */
public class ValueEvaluator implements ActionVisitor {

    private final ResourceHand hand;

    public ValueEvaluator(ResourceHand hand) {
        this.hand = hand;
    }

    @Override
    public double visit(BuildSettlementAction action) {
        // settlement costs 4 cards
        return (hand.totalCards() - 4 < 5) ? 0.5 : 1.0;
    }

    @Override
    public double visit(BuildCityAction action) {
        // city costs 5 cards
        return (hand.totalCards() - 5 < 5) ? 0.5 : 1.0;
    }

    @Override
    public double visit(BuildRoadAction action) {
        // Roads cost 1 wood + 1 brick = 2 cards
        // If hand drops below 5 after building → 0.5, otherwise 0.8
        return (hand.totalCards() - 2 < 5) ? 0.5 : 0.8;
    }

    @Override
    public double visit(PassAction action) {
        // Passing has no value
        return 0.0;
    }
    
    @Override
    public double visit(RollAction action) { 
        return 0.0;
    }
}