# AGENTS.md — Sudoku Application

> Living architecture document. Always read **in full** before any action.
> Full history and resolved issues → `CHANGE_HISTORY.md`.

---

# 🔴 AGENT OPERATING PROTOCOL — MANDATORY

## Step 1: Read This File First
Before performing ANY action on this project — including reading other files,
writing code, running commands, or responding to task requests — you MUST:

1. Read this entire `AGENTS.md`.
2. Internalize all rules, conventions, architecture notes, and constraints.
3. Do NOT proceed until fully read and understood.

---

## Step 2: Issue-First Workflow

### Before Making Any Change
- Check if a relevant GitHub issue already exists for this task.
- If NO issue exists → **CREATE one before touching any code.**
- Issue must contain:
  - Clear title describing the intent
  - Description of the problem or improvement
  - Acceptance criteria (what "done" looks like)
  - Relevant label: `bug` / `enhancement` / `refactor` / `docs` / `chore`
- Reference the issue in all commits: `git commit -m "feat: ... (closes #42)"`

### While Working
- Post progress updates as issue comments when the approach changes.
- Note any discovered blockers or scope changes in the issue thread.

### On Merge / Completion
- Update the issue with a summary of what changed and why.
- Close the issue if fully resolved; leave open with remaining-work notes if partial.

---

## Step 3: Post-Change — Re-read AGENTS.md
After completing any set of changes, re-read this file to verify:
- Changes did not violate any project conventions.
- No new rules need to be added based on what was learned.
- Significant architecture decisions are documented.

---

## Step 4: AGENTS.md Compaction Protocol

Apply compaction when this file exceeds ~150 lines **of architecture/history content**
(the Operating Protocol section does not count toward this limit).

### Compaction Steps
1. Move verbose history, rationale, and resolved decisions →
   append to `CHANGE_HISTORY.md` with a dated entry.
2. Retain in AGENTS.md only:
   - This Operating Protocol (never compact or remove)
   - Active rules and conventions
   - Current architecture overview (short)
   - Toolchain and environment essentials
   - Any "never do X" hard constraints
3. Add a compaction log entry to `CHANGE_HISTORY.md` noting what was moved.

---

## Step 5: CHANGE_HISTORY.md Format

Append an entry for every significant change session:

```
[YYYY-MM-DD] <Short Title>
Issue: #<number> (or N/A)
Summary: <2–5 sentences describing what changed and why.>
Files Modified:
  path/to/file.ext — <reason>
Decisions Made:
  <Any architectural or design decision worth preserving>
Compacted from AGENTS.md:
  <List any sections moved here, if applicable>
```

---

## ⚠️ Hard Rules (Never Violate)

- NEVER make changes without first reading `AGENTS.md`.
- NEVER touch code without a linked GitHub issue.
- NEVER merge without updating the linked issue.
- NEVER let `AGENTS.md` architecture content grow unbounded — compact proactively.
- NEVER delete `CHANGE_HISTORY.md` entries — append only.
- ALWAYS close or update issues that are affected by a merge.

---

# Project: Sudoku Android App

## Open Issues

| # | Title | Status |
|---|---|---|
| — | Polish: animations, accessibility content descriptions | ⏳ Pending (no issue yet) |

All previous issues (#1–#6) closed. See `CHANGE_HISTORY.md`.

---

## Project Overview

Fully offline Android Sudoku game. MVVM + Jetpack Compose + Material3 + Hilt.
Min SDK 26, Target SDK 35, Kotlin, Gradle Kotlin DSL.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin (latest stable) |
| UI | Jetpack Compose + Material3 |
| Architecture | MVVM — ViewModel + StateFlow |
| DI | Hilt (KSP — **not** kapt) |
| Navigation | Compose Navigation |
| Concurrency | Kotlin Coroutines |
| Theme persistence | DataStore Preferences |
| Build | Gradle (Kotlin DSL) |

---

## Package Structure

```
com.lumos.sudoku/
├── data/
│   ├── model/          SudokuCell, SudokuBoard*, Difficulty, GameState
│   ├── generator/      SudokuGenerator (backtracking + uniqueness solver)
│   └── repository/     SudokuRepository, ThemePreferencesRepository
├── domain/usecase/     GeneratePuzzle, ValidateMove, GetHint, CheckCompletion
├── ui/
│   ├── theme/          Theme, Color, Type
│   ├── navigation/     AppNavigation (routes: home, game/{difficulty}, result/...)
│   └── screen/
│       ├── home/       HomeScreen, HomeViewModel
│       ├── game/       GameScreen, GameViewModel, components/
│       │   components/ SudokuGrid, SudokuCell, NumberPad, GameControls
│       └── result/     ResultScreen
└── di/                 AppModule (empty — @Inject @Singleton suffices)
```

> `SudokuBoard.kt` exists but is unused — `SudokuRepository` returns `List<List<SudokuCell>>` directly.

---

## Key Models

### SudokuCell
```kotlin
data class SudokuCell(
    val row: Int, val col: Int,
    val value: Int,       // 0 = empty
    val solution: Int,
    val isGiven: Boolean, // pre-filled, locked
    val isWrong: Boolean, // value != solution && value != 0
    val isHinted: Boolean,
    val notes: Set<Int>   // pencil candidates 1..9
)
```

### GameUiState (GameViewModel.kt)
```kotlin
data class GameUiState(
    val board: List<List<SudokuCell>>,
    val selectedRow: Int,                  // -1 = none
    val selectedCol: Int,
    val isPencilMode: Boolean,
    val mistakes: Int,                     // game over at 3
    val hintsUsed: Int,
    val gameState: GameState,              // Idle | Playing | Won | GameOver
    val difficulty: Difficulty,
    val elapsedSeconds: Long,
    val selectedNumber: Int,               // 0 = none; drives same-number highlight
    val isInstantFillMode: Boolean,        // mutually exclusive with pencil mode
    val conflictCells: Set<Pair<Int,Int>>, // flashes red 600ms on constraint violation
    val numberRemainingCounts: List<Int>   // index 0 = digit 1; value = 9 - placed count
)
```

---

## Cell Visual Priority

| Priority | Condition | Color token |
|---|---|---|
| 1 | isSelected | `SelectedCellBg` |
| 2 | isRelated (same row/col/box as selected) | `RelatedCellBg` |
| 3 | cell.isHinted | `HintedCellBg` |
| 4 | isSameNumber (value == selectedNumber) | `SameNumberBg` (amber) |
| 5 | isSameNumberCross (row/col of a same-number cell) | `SameNumberCrossBg` (faint amber) |
| 6 | isConflict (in conflictCells flash set) | `ConflictCellBg` (red) |
| 7 | default | `surface` |

---

## Gameplay Rules (Enforced in GameViewModel)

- `isGiven` and `isHinted` cells are always locked.
- A correctly filled cell (`value != 0 && !isWrong`) is locked — only Undo reverts it.
- Constraint violation (same row/col/box) → **blocked + flash**, not a mistake.
- Wrong value (passes constraints, differs from solution) → counted as mistake.
- 3 mistakes → GameOver.
- Pencil mode and instant fill mode are mutually exclusive.
- `numberRemainingCounts` recomputed on every board mutation.

---

## Puzzle Generation

1. Backtracking fill of a solved board.
2. Random cell removal with `countSolutions(board, limit=2)` uniqueness check.
3. Terminal condition in `solveRecursive`: `if (r == 9)` at the top — not "if next row == 9".
4. Difficulty → revealed cells: Easy 45–50, Medium 35–44, Hard 25–34.

---

*Last updated: 2026-05-30 | Full history → CHANGE_HISTORY.md*
