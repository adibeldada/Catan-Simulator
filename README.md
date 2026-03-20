# Settlers of Catan
**McMaster University | SFWRENG 2AA4 - Assignment 3**
**Contributors:** Adib El Dada (eldada1) | Youssef Elshafei (elshafey) | Youssef Khafagy (khafagyy) | Riken Allen (allenr16)

---

## Project Overview
This project extends the Assignment 2 Java-based Catan simulator with undo/redo functionality and a rule-based machine intelligence agent. The focus of A3 is applying **design patterns** to achieve clean, extensible software: each new feature is introduced through a deliberately chosen pattern, improving structure without requiring wholesale rewrites of existing code.

---

## New Features in Assignment 3

### Undo / Redo (R3.1)
- Every game action (build road, build settlement, build city, buy development card, pass) can be **undone and redone** during a session.
- The undo/redo history is maintained per-turn and is available to both human and AI players.
- Implemented via the **Command Pattern** (see Design Patterns section).

### Rule-Based Machine Intelligence (R3.2 – R3.3)
The AI agent evaluates a pre-defined set of rules each turn and selects the highest-value action.

#### Value scoring (R3.2)
| Action | Value |
|---|---|
| Earns a Victory Point | 1.0 |
| Builds without earning a VP (road, dev card) | 0.8 |
| Spends cards leaving fewer than 5 in hand | 0.5 |
| Tie between actions | Random selection |

#### Constraints — resolved before value-added actions (R3.3)
| Constraint | Response |
|---|---|
| Hand size > 7 cards | Must spend cards immediately |
| Two road segments =< 2 units apart | Buy roads to connect them |
| Opponent's longest road =< 1 shorter than agent's | Buy a connected road segment |

---

## Design Patterns

### Task 1 — Command Pattern (Undo/Redo, R3.1)

**Why:** Undo/redo is the canonical use case for the Command pattern. Each game action (build road, build settlement, etc.) is encapsulated as a `Command` object carrying both an `execute()` and an `undo()` method. Two stacks — an undo stack and a redo stack — hold these objects. Undoing pops from the undo stack, calls `undo()`, and pushes to the redo stack; redoing reverses that flow.

**Structure:**
```
<<interface>> Command
  + execute(): void
  + undo(): void

BuildRoadCommand implements Command
BuildSettlementCommand implements Command
BuildCityCommand implements Command
BuyDevCardCommand implements Command
PassCommand implements Command

GameHistory
  - undoStack: Deque<Command>
  - redoStack: Deque<Command>
  + push(Command): void
  + undo(): void
  + redo(): void
```

**Design benefit:** No existing game logic needs to change — each move class simply gains an `undo()` counterpart. The `GameHistory` manager is entirely new, satisfying the Open/Closed principle. Adding new undoable actions in the future requires only a new `Command` implementation, not changes to the history manager.

---

### Task 2 — Strategy Pattern (Rule-Based AI, R3.2)

**Why:** The AI agent's rules are naturally independent behaviours that share a common interface: each rule evaluates the current game state and returns a scored action. The Strategy pattern lets rules be added, removed, or swapped without touching the agent or the evaluation loop.

**Structure:**
```
<<interface>> AIRule
  + isApplicable(GameState): boolean
  + evaluate(GameState): ScoredAction

EarnVPRule implements AIRule
BuildRoadRule implements AIRule
BuyDevCardRule implements AIRule
SpendExcessCardsRule implements AIRule

AIAgent
  - rules: List<AIRule>
  + chooseBestAction(GameState): Action
```

**Evaluation loop (R3.2):**
```java
List<ScoredAction> candidates = rules.stream()
    .filter(r -> r.isApplicable(state))
    .map(r -> r.evaluate(state))
    .collect(toList());
// pick highest score; random tiebreak
```

**Design benefit:** Constraints (R3.3) are implemented as high-priority `AIRule` implementations with a fixed score ceiling above normal value-added rules, ensuring they are always chosen first when applicable — without any `if`-chains in the agent itself.

---

### Task 3 — Observer Pattern (Game Event Notifications)

**Why:** Multiple components need to react to game events (a 7 is rolled → trigger robber; a VP is earned → check win condition; a card is spent → update hand display). Rather than hardwiring these reactions into the event sources, the Observer pattern decouples producers from consumers.

**Structure:**
```
<<interface>> GameEventListener
  + onEvent(GameEvent): void

VictoryConditionChecker implements GameEventListener
RobberTrigger implements GameEventListener
HandDisplayUpdater implements GameEventListener

GameEventBus
  + subscribe(EventType, GameEventListener): void
  + publish(GameEvent): void
```

**Design benefit:** New reactions to existing events (e.g., logging, achievements, AI notifications) are added by registering a new listener — zero changes to the event-publishing code. This cleanly replaces the ad-hoc method calls that previously scattered event-handling logic across `GameMaster`.

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
   - **Eclipse:** Right-click → **Run As** → **Java Application**
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
- **Command objects:** Every `Move` subclass now implements `Command`, gaining an `undo()` method alongside `execute()`. The `GameHistory` class manages the undo/redo stacks.
- **AIRule interface:** The monolithic AI decision block is decomposed into individual `AIRule` implementations evaluated by a scoring loop inside `AIAgent`.
- **GameEventBus:** Centralises event dispatch; `GameMaster` now publishes events rather than calling subsystem methods directly.
- **Constraint rules:** R3.3 constraints are `AIRule` implementations that return a score above the normal value ceiling, guaranteeing priority without branching logic.

### SOLID Principles Applied
- **Single Responsibility:** `GameHistory`, `AIAgent`, and `GameEventBus` each have one clearly defined role.
- **Open/Closed:** New moves, AI rules, and event listeners are added by extension — no modification to existing classes.
- **Liskov Substitution:** `AIAgent` and `HumanPlayer` remain interchangeable in the turn loop.
- **Interface Segregation:** `Command`, `AIRule`, and `GameEventListener` are narrow, focused interfaces.
- **Dependency Inversion:** `GameMaster` depends on abstractions (`Command`, `AIRule`, `GameEventListener`), not concrete implementations.

---

## Testing

### Unit Tests
- Existing test suite from A2 is retained and extended.
- New tests cover: undo/redo correctness (execute → undo → state restored), redo after undo, AI rule scoring, constraint priority over value-added actions, and observer notification delivery.

### Running Tests
```bash
cd Catan-Code
mvn test
```

---

## Console Output Format
```
[TurnID] / [PlayerID]: [Action]
```
Example:
```
24 / AIAgent: Built road (rule: ConnectSegmentsRule, constraint priority)
25 / Human: Built settlement at node 12
25 / Human: Undo — settlement at node 12 removed
```

---

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=adibeldada_Catan-Simulator&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=adibeldada_Catan-Simulator)
