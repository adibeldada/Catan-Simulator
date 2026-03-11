# Settlers of Catan - Human Gameplay & Evolution
**McMaster University | SFWRENG 2AA4 - Assignment 2**
**Contributors:** Adib El Dada (eldada1) | Youssef Elshafei (elshafey) | Youssef Khafagy (khafagyy) | Riken Allen (allenr16)

## Project Overview
This project extends the Assignment 1 Java-based Catan simulator with human player support, board visualization, trading, and the Robber mechanism. The focus of A2 is software evolution: adapting an existing codebase to new requirements while maintaining design quality through OO and SOLID principles, unit testing, and UML-driven design updates.

## New Features in Assignment 2

### Human Player (R2.1 – R2.4)
- A human player replaces one of the computer agents and interacts via the command line on their turn.
- The following commands are supported:

| Command | Description |
|---|---|
| `Roll` | Roll the dice and collect resources |
| `Go` | End your turn (proceed to next agent) |
| `List` | Display all cards currently in your hand |
| `Build settlement <nodeId>` | Build a settlement at the specified node |
| `Build city <nodeId>` | Upgrade a settlement to a city |
| `Build road <fromNodeId, toNodeId>` | Build a road between two nodes |

- All human input is parsed using **regular expressions** for robust command handling.
- A **"step forward"** mode (R2.4) pauses the simulation between turns and waits for the `Go` command before proceeding.

### Visualization Integration (R2.2 – R2.3)
- The simulator writes the live game state to an external **JSON file** after each turn.
- This JSON feeds the visualizer provided by the instructor team:
  > https://github.com/ssm-lab/2aa4-2026-base/tree/main/assignments/visualize
- The visualizer is a Python script. Run it manually or use live visualization mode (see the visualizer's README for setup).

### Robber Mechanism (R2.5)
- When a **7** is rolled, the following occurs:
  1. Any player with more than 7 resource cards discards half (rounded down).
  2. The Robber is placed on a **randomly selected tile**.
  3. A qualifying player (one with a settlement or city adjacent to the Robber's new tile) is **randomly chosen** to pass one random card to the player who rolled the 7.

---

## How to Run the Simulation

### 1. Configuration
The simulation duration is controlled by `config.txt` in the **project root directory**.
```
turns: 100
```
Supports values from `1` to `8192` turns (1 turn = 1 player acting).

### 2. Running the Java Simulator
1. Open your IDE (Eclipse / IntelliJ / VS Code).
2. Navigate to the **`Catan-Code`** folder.
3. Locate and run the Demonstrator:
   ```
   src/CatanSimulatorDomainModel/catanUML/Demonstrator.java
   ```
   - **Eclipse:** Right-click > **Run As** > **Java Application**
   - **IntelliJ:** Click the green **Run** icon next to `main`

4. When it is your turn, enter one of the supported commands at the console prompt.

### 3. Running the Visualizer (Python)
Ensure Python is installed, then follow the instructions in the visualizer README:
```bash
cd assignments/visualize
python visualize.py --file <path-to-game-state.json>
```
For live mode, refer to the `--live` flag documented in the visualizer repo.

---

## System Architecture

### Key Design Changes from A1
- **Human Player abstraction:** A `HumanPlayer` class extends the existing `Agent` hierarchy, keeping the turn-loop logic unchanged.
- **Command Parser:** A dedicated parser class uses regular expressions to tokenize and validate human input, decoupled from game logic.
- **Game State Serializer:** A new component serializes the board and player state to JSON after every turn, satisfying the visualizer's expected format.
- **Robber component:** Encapsulated as its own class, triggered by the dice roll event and operating on the existing tile/vertex model.
- **Automaton-based turn model:** Each agent's action space within a turn is modelled as a finite automaton, clearly defining which actions are valid in which states (e.g., must Roll before Build, Go ends the turn).

### SOLID & OO Principles Applied
- **Single Responsibility:** Parser, serializer, and game logic are separated into distinct classes.
- **Open/Closed:** New player types (human vs. agent) are added by extension, not modification.
- **Liskov Substitution:** `HumanPlayer` is a drop-in replacement for any `Agent` in the turn loop.
- **Dependency Inversion:** High-level game flow depends on the `Agent` abstraction, not concrete player types.

---

## Testing

### Unit Tests (Task 1 – JUnit)
- **10–20 unit tests** are implemented before any new code was written, covering core game logic from A1.
- Tests use **partition testing** and **boundary testing** where applicable and are organized into **test suites**.

### Parser Tests (Task 3)
- **5–10 additional tests** validate the correctness of the regular-expression-based command parser, covering valid inputs, invalid inputs, edge cases, and boundary conditions.

---

## Console Output Format
```
[TurnID] / [PlayerID]: [Action]
```
Example:
```
12 / Player2: Built settlement at node 34
13 / Human: Rolled 7 - Robber placed on tile 5
```

---

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=adibeldada_Catan-Simulator&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=adibeldada_Catan-Simulator)
