# Change History

---

## 2026-05-30

### Initial project setup
- Created `.gitignore` for Android Studio / Kotlin project
- Created `AGENTS.md` with full application plan (Issue #1)
- Created `README.md` with project overview
- Created `CHANGE_HISTORY.md`

### Bug Fixes
- Fixed `IndexOutOfBoundsException` in `SudokuGrid` when rendering the board with an empty list during initial screen composition. Added a safety check and centered progress indicator to prevent layout shift.
- Upgraded Dagger Hilt to `2.59.2` to resolve compilation/sync issues with Kotlin `2.2.10` caused by metadata version incompatibilities.
- Migrated project from `kapt` to `KSP` (Kotlin Symbol Processing) to resolve `[Hilt] Expected @AndroidEntryPoint to have a value` errors. (Issue #4)

**Scope of Issue #1 plan:**
- Standard 9×9 Sudoku, 3 difficulty levels
- Pencil/notes mode, hint system, undo, mistake counter
- Wrong number highlighted in red
- Dark/Light mode with DataStore persistence
- MVVM + Hilt + Jetpack Compose + Material3

### Implemented Issue #1
- Structured Android Gradle project supporting Gradle 8.7, Kotlin, Hilt DI, Jetpack Compose, and Material3.
- Implemented core data models (`SudokuCell`, `SudokuBoard`, `Difficulty`, `GameState`).
- Implemented backtracking generator & uniqueness solver in `SudokuGenerator`.
- Implemented domain use cases (`GeneratePuzzleUseCase`, `ValidateMoveUseCase`, `GetHintUseCase`, `CheckCompletionUseCase`).
- Implemented `GameViewModel` & `HomeViewModel` for state management, timer control, and mistake validation.
- Built Datastore-backed theme preference repository.
- Designed premium user interface layouts:
  - `HomeScreen` with custom difficulty cards and clean typography.
  - `GameScreen` with timer, mistake count, responsive grid with region lines, controls (Undo, Notes, Hint), and a split number pad.
  - `ResultScreen` displaying completion stats and action paths (Play Again / Home).
- Connected navigation destinations via `AppNavigation`.
- Verified debug compilation with zero warnings and errors.
