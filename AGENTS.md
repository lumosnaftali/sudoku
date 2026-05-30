# AGENTS.md — Sudoku Application

> Living architecture document. History is kept in CHANGE_HISTORY.md.
> Always read this file before making any edits.

---

## Issue #1 — Initial Application Plan

**Status:** Completed
**Created:** 2026-05-30
**Completed:** 2026-05-30

---

## Issue #2 — IndexOutOfBoundsException on SudokuGrid

**Status:** Completed
**Created:** 2026-05-30
**Completed:** 2026-05-30

---

## Issue #3 — Dagger Hilt kotlinx-metadata-jvm Incompatibility with Kotlin 2.2.10

**Status:** Completed
**Created:** 2026-05-30
**Completed:** 2026-05-30

---

## Issue #4 — Hilt @AndroidEntryPoint Missing Value Error (Kapt Compatibility)

**Status:** Completed
**Created:** 2026-05-30
**Completed:** 2026-05-30

---

## Issue #5 — SudokuGenerator.countSolutions Terminal Condition Bug

**Status:** Completed
**Created:** 2026-05-30
**Completed:** 2026-05-30

**Root cause:** `countSolutions` used "compute next, if next is out-of-bounds then count" logic. When called with `r=8, c=8`, the function calculated `nextRow=9` and immediately incremented `solutionsCount` **without** checking or filling cell `(8,8)`. If cell `(8,8)` was empty (removed), the solver would count one "solution" regardless of how many valid values `(8,8)` could hold, causing `removeCells` to always remove `(8,8)` even when the resulting puzzle is non-unique.

**Fix:** Changed the terminal condition to `if (r == 9)` at the top of the recursive function. Solver now counts only when called with a row index past the board end (`r == 9`), meaning all 81 cells have been correctly placed before counting.

---

## Overview

A fully modern Android Sudoku game built with Kotlin, Jetpack Compose, Material3, and MVVM architecture. The board is a standard 9×9 grid. Players choose a difficulty, can take pencil notes, request hints, undo moves, and see mistakes highlighted in red.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin (latest stable) |
| UI | Jetpack Compose + Material3 |
| Architecture | MVVM — ViewModel + StateFlow |
| DI | Hilt |
| Navigation | Compose Navigation |
| Concurrency | Kotlin Coroutines |
| Build | Gradle (Kotlin DSL) |
| Min SDK | 26 (Android 8.0) |
| Target SDK | 35 |

---

## Features

### Gameplay
- Standard 9×9 Sudoku grid (3×3 box regions)
- Pre-filled cells are locked (non-editable)
- Select a cell then tap a number to input it

### Difficulty Levels
| Level | Cells Revealed |
|---|---|
| Easy | ~45–50 |
| Medium | ~35–44 |
| Hard | ~25–34 |

### Pencil / Notes Mode
- Toggle pencil mode via a pencil button in the toolbar
- Pencil numbers are rendered smaller and more transparent than regular input
- Up to 9 candidate notes per cell, displayed in a 3×3 mini-grid inside the cell
- Entering a confirmed number clears all pencil notes for that cell

### Hint System
- Tap the Hint button to reveal the correct value for the selected (empty or wrong) cell
- Hints are tracked — shown in a counter (e.g. "Hints used: 2")
- Hinted cells are styled differently to distinguish them from player inputs

### Wrong Number Indicator
- When a player inputs a number that conflicts with the solution, the number is shown in red
- Pre-filled (given) cells are never red

### Undo
- Undo button reverts the last player action (input or erase)
- Supports full undo history for the current game session

### Mistake Counter
- Tracks total wrong inputs made during the session
- Displayed prominently on the game screen (e.g. "Mistakes: 2/3")
- Optional: game over after 3 mistakes (configurable)

### Theme
- Light Mode and Dark Mode toggle
- Follows Material3 dynamic color on supported devices (Android 12+)
- Theme preference persisted across sessions (DataStore)

---

## Architecture

### MVVM + Unidirectional Data Flow

```
UI (Compose) ──► ViewModel ──► UseCase ──► Repository
     ▲                │
     └── StateFlow ◄──┘
```

### Package Structure

```
com.lumos.sudoku/
├── data/
│   ├── model/
│   │   ├── SudokuCell.kt          # Cell data: value, notes, isGiven, isWrong, isHinted
│   │   ├── SudokuBoard.kt         # 9x9 grid + solution grid
│   │   ├── Difficulty.kt          # Enum: EASY, MEDIUM, HARD
│   │   └── GameState.kt           # Sealed class: Idle, Playing, Won, GameOver
│   ├── generator/
│   │   └── SudokuGenerator.kt     # Backtracking puzzle generator + remover
│   └── repository/
│       └── SudokuRepository.kt    # Generates puzzles, holds session state
│
├── domain/
│   └── usecase/
│       ├── GeneratePuzzleUseCase.kt
│       ├── ValidateMoveUseCase.kt  # Check input against solution
│       ├── GetHintUseCase.kt       # Reveal correct value for selected cell
│       └── CheckCompletionUseCase.kt
│
├── ui/
│   ├── theme/
│   │   ├── Theme.kt               # MaterialTheme setup, dark/light
│   │   ├── Color.kt               # Brand + semantic colors
│   │   └── Type.kt                # Typography scale
│   ├── navigation/
│   │   └── AppNavigation.kt       # NavHost, routes: Home, Game, Result
│   ├── screen/
│   │   ├── home/
│   │   │   ├── HomeScreen.kt      # Difficulty picker, theme toggle, start button
│   │   │   └── HomeViewModel.kt
│   │   ├── game/
│   │   │   ├── GameScreen.kt      # Main game layout
│   │   │   ├── GameViewModel.kt   # Core game logic, undo stack, mistake count
│   │   │   └── components/
│   │   │       ├── SudokuGrid.kt  # 9x9 board composable
│   │   │       ├── SudokuCell.kt  # Individual cell composable
│   │   │       ├── NumberPad.kt   # 1–9 input buttons + erase
│   │   │       └── GameControls.kt# Pencil toggle, hint, undo buttons
│   │   └── result/
│   │       └── ResultScreen.kt    # Win/GameOver screen with stats
│   └── MainActivity.kt
│
└── di/
    └── AppModule.kt               # Hilt module bindings
```

---

## Data Models

### SudokuCell
```kotlin
data class SudokuCell(
    val row: Int,
    val col: Int,
    val value: Int,          // 0 = empty
    val solution: Int,
    val isGiven: Boolean,    // pre-filled, locked
    val isWrong: Boolean,    // value != solution && value != 0
    val isHinted: Boolean,
    val notes: Set<Int>      // pencil candidates 1..9
)
```

### GameUiState (in GameViewModel)
```kotlin
data class GameUiState(
    val board: List<List<SudokuCell>>,
    val selectedRow: Int,
    val selectedCol: Int,
    val isPencilMode: Boolean,
    val mistakes: Int,
    val hintsUsed: Int,
    val gameState: GameState,
    val difficulty: Difficulty,
    val isDarkTheme: Boolean,
    val elapsedSeconds: Long
)
```

---

## Screen Designs

### Home Screen
- App title
- Difficulty selector (segmented button: Easy / Medium / Hard)
- Dark/Light mode toggle (top-right icon button)
- Start Game button

### Game Screen
- Top bar: mistake counter, hint counter, timer, theme toggle
- 9×9 Sudoku grid (center)
- Control row: Undo | Pencil toggle | Hint
- Number pad: 1–9 grid + Erase button

### Result Screen
- Win or Game Over indicator
- Stats: time taken, mistakes made, hints used
- Buttons: Play Again (same difficulty) | Home

---

## Cell Visual States

| State | Style |
|---|---|
| Given (pre-filled) | Bold, primary color, no highlight |
| Player input (correct) | Regular weight, onSurface color |
| Player input (wrong) | Red (error color) |
| Hinted | Surface variant background, italic |
| Selected | Highlighted background |
| Same row/col/box as selected | Subtle tint background |
| Pencil notes | 3×3 mini-grid, 60% opacity, 40% font size |

---

## Puzzle Generation Algorithm

1. Start with a solved board using backtracking.
2. Shuffle rows within bands, columns within stacks, and digit mapping for randomness.
3. Remove cells one by one while ensuring the puzzle has a unique solution (backtracking solver check).
4. Stop when the target number of revealed cells for the chosen difficulty is reached.

---

## Implementation Plan (Ordered)

1. **Project setup** — Gradle deps, Hilt, Compose, Material3, Navigation
2. **Data models** — `SudokuCell`, `SudokuBoard`, `Difficulty`, `GameState`
3. **Puzzle generator** — backtracking generator + uniqueness checker
4. **Repository & use cases**
5. **Theme** — Material3 light/dark, color tokens
6. **HomeScreen + HomeViewModel**
7. **GameViewModel** — full game logic, undo stack, mistake counter, pencil mode, hints
8. **SudokuGrid + SudokuCell composables** — cell states, pencil mini-grid
9. **NumberPad + GameControls composables**
10. **GameScreen layout** — wire all composables
11. **ResultScreen**
12. **Navigation** — connect all screens
13. **Theme persistence** — DataStore for dark/light preference
14. **Polish** — animations, accessibility content descriptions

---

## Open Questions / Decisions

- Max mistakes before game over: 3 (adjustable in GameViewModel)
- Hint limit per game: unlimited (tracked for stats only) — can add a cap later
- Timer: count-up (no time limit by default)
- No network / backend required — fully offline

---

---

## Implementation Review (2026-05-30)

### Plan Completion Status

| Step | Description | Status |
|---|---|---|
| 1 | Project setup (Gradle, Hilt, Compose, Material3, Navigation) | ✅ Done |
| 2 | Data models (SudokuCell, SudokuBoard, Difficulty, GameState) | ✅ Done |
| 3 | Puzzle generator (backtracking + uniqueness checker) | ✅ Done (bug fixed in Issue #5) |
| 4 | Repository & use cases | ✅ Done |
| 5 | Theme (Material3 light/dark, color tokens) | ✅ Done |
| 6 | HomeScreen + HomeViewModel | ✅ Done |
| 7 | GameViewModel (game logic, undo stack, mistake counter, pencil mode, hints) | ✅ Done |
| 8 | SudokuGrid + SudokuCell composables (cell states, pencil mini-grid) | ✅ Done |
| 9 | NumberPad + GameControls composables | ✅ Done |
| 10 | GameScreen layout | ✅ Done |
| 11 | ResultScreen | ✅ Done |
| 12 | Navigation (Home → Game → Result) | ✅ Done |
| 13 | Theme persistence (DataStore) | ✅ Done |
| 14 | Polish (animations, accessibility content descriptions) | ⏳ Not yet done |

### Notes
- `SudokuBoard.kt` is defined but not used by any other file — `SudokuRepository` returns `List<List<SudokuCell>>` directly. This is dead code but harmless.
- GitHub repository has only Issue #1 (closed). Issues #2–#4 were tracked locally only.

*Last updated: 2026-05-30 | See CHANGE_HISTORY.md for edit log*
