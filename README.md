# Settlers of Catan
**McMaster University | SFWRENG 2AA4 - Assignment 3**
**Contributors:** Adib El Dada (eldada1) | Youssef Elshafei (elshafey) | Youssef Khafagy (khafagyy) | Riken Allen (allenr16)

---

## Project Overview
This project extends the Assignment 2 Java-based Catan simulator with undo/redo functionality and a rule-based machine intelligence agent. The focus of A3 is applying design patterns to achieve clean, extensible software. Each new feature is introduced through a deliberately chosen Gang of Four pattern, improving structure without requiring wholesale rewrites of existing code.

---

## New Features in Assignment 3

### Undo / Redo (R3.1)
- Every game action (build road, build settlement, build city, roll, pass) can be undone and redone within a turn.
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

## How to Run the Simulation

### 1. Configuration
Simulation duration is set in `config.txt` as well as the players in game in the project root; can change the amount of human players and ai players by typing in the config.txt file:
```
turns: 100
player 1: HUMAN
player 2: AI
player 3: AI
player 4: AI
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
- **Visitor for scoring:** `ValueEvaluator` visits each candidate action via `accept()`, replacing hardcoded instanceof checks with double dispatch.
- **Constraint priority:** `resolveConstraint()` is called before `pickBestValueMove()` in the template method, guaranteeing constraints are handled first.

### SOLID Principles Applied
- **Single Responsibility:** `CommandManager`, `RuleBasedAIPlayer`, `ValueEvaluator`, and `ScoredAction` each have one clearly defined role.
- **Open/Closed:** New moves are added by creating a new `PlayerAction` subclass. New AI behaviours are added by extending `RuleBasedAIPlayer`. New scoring strategies are added by implementing `ActionVisitor`.
- **Liskov Substitution:** `AIPlayer` and `HumanPlayer` remain interchangeable in the turn loop.
- **Dependency Inversion:** `CommandManager` depends on the `PlayerAction` abstraction, not concrete move classes.

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
