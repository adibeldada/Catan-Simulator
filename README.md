# Settlers of Catan - Simulator & Demonstrator
**McMaster University | SFWRENG 2AA4 - Assignment 1**

**Contributers:** Adib El Dada (eldada1) | Youssef Elshafei (elshafey) | Youssef Khafagy (khafagyy) | Riken Allen (allenr16)

## Project Overview
This project is a Java-based discrete event simulator for the board game *Settlers of Catan*. It was developed by translating a conceptual UML domain model into a functional software system. The simulator handles complex hex-grid geometry, resource production, and automated player logic.


## Key Engineering Features (Rulebook Compliance)
This implementation satisfies all mandatory requirements specified in the Assignment 1 Rulebook:

* **R1.1 (Map Setup):** Implements the full 19-tile identification system (0-18) and 54-vertex grid (0-53).
* **R1.2 (Agent Initialization):** Supports exactly 4 players initialized with unique IDs.
* **R1.3 (Catan Rules):** Implements the standard setup phase (2 settlements/roads) and dice-roll-based resource production (excluding trading).
* **R1.4 (Simulation Duration):** Supports user-defined turn counts via configuration, up to 8,192 rounds (32,768 turns).
* **R1.5 (Termination):** Terminates automatically upon reaching the turn limit or when a player achieves 10 Victory Points.
* **R1.6 (Invariants):** Enforces strict rules: settlements must be 2 vertices apart, and cities must replace existing settlements.
* **R1.7 (Action Logs):** Every turn generates a log entry in the format `[Round] / [Player]: [Action]`, including "Passed turn" entries.
* **R1.8 (Forced Building):** Agents with >7 cards are programmed to spend resources until they are under the limit or out of moves.
* **R1.9 (Demonstration):** A complete demonstrator class is provided to showcase all system functionality.

---

## How to Run the Simulation

### 1. Configuration
The simulation duration is controlled by `config.txt` located in the **project root directory**.
* Open `config.txt`.
* Set the turns (e.g., `turns: 100` for 25 rounds). 
* The system supports up to `32768` turns.

### 2. Execution Steps
1. Open your IDE (Eclipse/IntelliJ/VS Code).
2. Navigate to the folder: **`Catan-Code`**.
3. Open the source folder and locate:
   `src/CatanSimulatorDomainModel/catanUML/Demonstrator.java`
4. **Run the program:**
   * **Eclipse:** Right-click `Demonstrator.java` > **Run As** > **Java Application**.
   * **IntelliJ:** Click the green **Run** icon next to the `main` method.
5. Review the simulation results and final standings in the **Console** view.

---

## System Architecture
* **Command Pattern:** Used for `PlayerAction` hierarchy to ensure modular execution of roads, settlements, and cities.
* **Domain Model:** The code is 1:1 mapped to the Papyrus UML Class Diagram.
* **Fair-Start Heuristic:** Includes a custom placement algorithm to ensure all players start with access to Wood, Brick, and Wheat to prevent no-trade deadlocks.
