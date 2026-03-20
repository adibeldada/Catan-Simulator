# Settlers of Catan
**McMaster University | SFWRENG 2AA4 - Assignment 3**
**Contributors:** Adib El Dada (eldada1) | Youssef Elshafei (elshafey) | Youssef Khafagy (khafagyy) | Riken Allen (allenr16)

---

## Project Overview
This project extends the Assignment 2 Java-based Catan simulator with undo/redo functionality and a rule-based machine intelligence agent. The focus of A3 is applying design patterns to achieve clean, extensible software. Each new feature is introduced through a deliberately chosen Gang of Four pattern, improving structure without requiring wholesale rewrites of existing code.

---

## New Features in Assignment 3

### Undo / Redo (R3.1)
- Every game action (build road, build settlement, build city, pass) can be undone and redone during a session.
- Implemented using the **Command Pattern** via a new `CommandManager` class with undo and redo stacks.
- No changes were made to any core game logic classes (`Board`, `Player`, `RuleValidator`, `Tile`, `Vertex`).

### Rule-Based Machine Intelligence (R3.2 and R3.3)
The AI agent evaluates a pre-defined set of rules each turn and selects the highest-value action.

#### Value scoring (R3.2)
| Action | Value |
|---|---|
| Earns a Victory Point | 1.0 |
| Builds without earning a VP (road, dev card) | 0.8 |
| Spends cards leaving fewer than 5 in hand | 0.5 |
| Tie between actions | Random selection |

#### Constraints resolved before value-added actions (R3.3)
| Constraint | Response |
|---|---|
| Hand size > 7 cards | Must spend cards immediately |
| Two road segments at most 2 units apart | Buy roads to connect them |
| Opponent's longest road is at most 1 shorter than the agent's | Buy a connected road segment |

---

## Design Patterns

### Task 1 - Command Pattern (Undo/Redo, R3.1)

**Pattern components:**
- `PlayerAction` (abstract) -- the Command interface, extended with an abstract `undo()` method
- `BuildRoadAction`, `BuildSettlementAction`, `BuildCityAction`, `PassAction` -- Concrete Commands, each storing a reference to the object placed (e.g. `placedSettlement`) so the action can be reversed
- `CommandManager` -- the Invoker, holding an undo stack and a redo stack with no knowledge of game rules
- `GameMaster` -- the Client, exposing `executeAction()`, `undoLastAction()`, and `redoLastAction()`

**Why this pattern:** Undo/redo requires reversible, self-contained actions. Encapsulating each move as an object with `execute()` and `undo()` lets `CommandManager` manage history without knowing anything about what a road or a settlement is. The build actions become independently testable units, and the rest of the system interacts with a clean three-method interface.

**Changes made:** `CommandManager` was added as a new class. `undo()` was made abstract in `PlayerAction`. Three action classes were updated to implement `undo()` and store placement references. `GameMaster` gained three new methods and one new field. `HumanPlayer` was updated to route builds through `executeAction()`. `CommandParser` gained two new patterns. Core game logic classes were untouched.

---

### Task 2 - Template Method Pattern (Rule-Based AI, R3.2 and R3.3)

**Pattern components:**
- `RuleBasedAIPlayer` (abstract) -- defines `takeTurn()` as a `final` template method with the skeleton: roll, `resolveConstraint()`, `pickBestValueMove()`, repeat until pass
- `resolveConstraint()` -- abstract hook method for R3.3 constraint logic
- `pickBestValueMove()` -- abstract hook method for R3.2 value scoring
- `AIPlayer` -- extends `RuleBasedAIPlayer` and implements both hooks
- `ScoredAction` -- helper class binding a `PlayerAction` with its calculated `double` value

**Why this pattern:** The AI turn has a fixed skeleton but variable steps. Template Method defines the algorithm once in the abstract class as a `final` method so no subclass can accidentally break the ordering, while the constraint and scoring logic are explicitly separated into overrideable hooks. This also provides a natural extension point: a future aggressive or defensive AI personality only needs to extend `RuleBasedAIPlayer` and implement the two hooks.

**Changes made:** `RuleBasedAIPlayer` was added as a new abstract class. `AIPlayer` was changed to extend `RuleBasedAIPlayer` instead of `Player`, and `takeTurn()` and `decideMove()` were replaced by the two hook methods. `ScoredAction` was added inside `AIPlayer`. `GameMaster`, `HumanPlayer`, `Board`, `Player`, and `RuleValidator` were not changed.

---

### Task 3 - Chain of Responsibility Pattern (Rule Validation, design only)

**Motivation:** The existing `RuleValidator` class has three compounding problems. First, it is a god class handling roads, connectivity, settlements, cities, and distances all in one place, violating the Single Responsibility Principle. Second, each validation method is an if-statement chain where adding or reordering a rule requires editing the method directly, violating the Open/Closed Principle. Third, the class has hidden dependencies on concrete implementations of `Board`, `Player`, `Vertex`, `Road`, and `Buildings`, violating the Dependency Inversion Principle.

**Pattern structure (design only, not implemented):**
- `RoadBuildRequest` -- request object that makes all validation inputs explicit, removing hidden dependencies
- `Result` -- structured return type replacing `boolean`, including pass/fail status and a custom error message
- `ValidationHandler` (interface) -- each rule implements this, accepting a request and returning a `Result`
- Concrete handlers: `RoadCostRule`, `RoadAdjacencyRule`, `RoadOccupancyRule`, `RoadConnectivityRule`
- `RoadRuleChain` -- the pipeline, holding an ordered list of handlers and stopping on the first failure

**Why this pattern:** CoR turns each validation rule into an independent, single-responsibility class. Rules become reusable across different action types, reorderable without touching existing logic, and individually testable in isolation. Validation logic depends on the `ValidationHandler` abstraction rather than concrete domain classes.

---

## How to Run the Simulation

### 1. Configuration
Simulation duration is set in `config.txt` in the project root:
```
turns: 100
```
Supports values from `1` to `8192` (1 turn = 1 player acting).

### 2. Running the Java Simulator
1. Open your IDE (Eclipse / IntelliJ / VS Code).
2. Navigate to the **`Catan-Code`** folder.
3. Run the Demonstrator:
   ```
   src/CatanSimulatorDomainModel/Catan-Code/src-gen/classes/Demonstrator.java
   ```
   - **Eclipse:** Right-click > **Run As** > **Java Application**
   - **IntelliJ:** Click the green **Run** icon next to `main`

### 3. Human Player Commands

| Command | Description |
|---|---|
| `Roll` | Roll the dice and collect resources |
| `Go` | End your turn |
| `List` | Display all cards in your hand |
| `Build settlement <nodeId>` | Build a settlement |
| `Build city <nodeId>` | Upgrade a settlement to a city |
| `Build road <fromNodeId, toNodeId>` | Build a road |
| `Undo` | Undo the last action this turn |
| `Redo` | Redo a previously undone action |

### 4. Running the Visualizer (Python)
```bash
cd 2aa4-2026-base/assignments/visualize
python light_visualizer.py base_map.json state.json
```
Use the `--watch` flag for live updates between turns.

---

## System Architecture

### Key Design Changes from A2
- **Command objects:** `PlayerAction` now declares an abstract `undo()` method. Each build action stores what it placed so it can be reversed. `CommandManager` owns the two history stacks.
- **Template Method for AI:** The monolithic `decideMove()` is replaced by a `final` `takeTurn()` skeleton in `RuleBasedAIPlayer` with two abstract hooks implemented by `AIPlayer`.
- **ScoredAction helper:** Binds a `PlayerAction` to its computed value score so the AI evaluation loop stays clean.
- **Constraint priority:** `resolveConstraint()` is called before `pickBestValueMove()` in the template method, guaranteeing constraints are handled first without any extra conditional logic in the agent.

### SOLID Principles Applied
- **Single Responsibility:** `CommandManager`, `RuleBasedAIPlayer`, and `ScoredAction` each have one clearly defined role.
- **Open/Closed:** New moves are added by creating a new `PlayerAction` subclass. New AI behaviours are added by extending `RuleBasedAIPlayer`. No existing classes need modification.
- **Liskov Substitution:** `AIPlayer` and `HumanPlayer` remain interchangeable in the turn loop.
- **Dependency Inversion:** `CommandManager` depends on the `PlayerAction` abstraction, not concrete move classes.

---

## Testing

### Running Tests
```bash
cd Catan-Code
mvn test
```

Tests cover undo/redo correctness (execute, undo, verify state restored), redo after undo, AI value scoring, constraint priority over value-added actions, and existing A2 test cases.

---

## Console Output Format
```
[TurnID] / [PlayerID]: [Action]
```
Example:
```
24 / AIAgent: Built road (constraint: connect segments)
25 / Human: Built settlement at node 12
25 / Human: Undo -- settlement at node 12 removed
```

---

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=adibeldada_Catan-Simulator&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=adibeldada_Catan-Simulator)
