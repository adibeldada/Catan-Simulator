package classes.moves;

/**
 * Visitor interface for evaluating the value of a player action.
 * Part of the Visitor design pattern (Task 2, R3.2).
 *
 * Each concrete visitor implements a different evaluation strategy.
 * The visit methods are called by the accept() method of each action.
 */
public interface ActionVisitor {
    double visit(BuildSettlementAction action);
    double visit(BuildRoadAction action);
    double visit(BuildCityAction action);
    double visit(PassAction action);
}