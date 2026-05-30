# Change History

---

## 2026-05-30 (Issue #6 GH — Architecture Migration)

### Feature-First Architecture + src/main/kotlin Migration
Issue: #6 (GitHub #6)
Summary: Migrated from layer-first `src/main/java` structure to feature-first Clean Architecture
under `src/main/kotlin`. SudokuRepository split into interface (domain) + impl (data) with
`GameDataModule` binding. `GameUiState` extracted to own file. `HomeViewModel` now exposes single
`StateFlow<HomeUiState>`. All screen composables use `collectAsStateWithLifecycle()`.
`Route` object centralises all navigation route strings.

Files Created (32 new under `src/main/kotlin/`):
  core/theme/{Color,Type,Theme}.kt — moved + package updated
  core/navigation/{Route,AppNavigation}.kt — Route is new; AppNavigation moved
  core/model/Difficulty.kt — moved from data/model
  core/datastore/ThemePreferencesRepository.kt — moved from data/repository
  feature/game/domain/model/{SudokuCell,SudokuBoard,GameState}.kt — moved from data/model
  feature/game/domain/repository/SudokuRepository.kt — NEW interface
  feature/game/domain/usecase/*.kt — moved from domain/usecase
  feature/game/data/generator/SudokuGenerator.kt — moved
  feature/game/data/repository/SudokuRepositoryImpl.kt — renamed from SudokuRepository, implements interface
  feature/game/data/di/GameDataModule.kt — NEW @Binds module
  feature/game/presentation/GameUiState.kt — extracted from GameViewModel
  feature/game/presentation/{GameViewModel,GameScreen}.kt — moved
  feature/game/presentation/component/*.kt — moved from ui/screen/game/components (renamed dir)
  feature/home/presentation/{HomeUiState,HomeViewModel,HomeScreen}.kt — HomeUiState is new
  feature/result/presentation/ResultScreen.kt — moved
  {SudokuApplication,MainActivity}.kt, di/AppModule.kt — moved to src/main/kotlin root

Files Deleted:
  Entire app/src/main/java/ tree (27 files)

Files Modified:
  gradle/libs.versions.toml — added androidx-lifecycle-runtime-compose
  app/build.gradle.kts — added lifecycle-runtime-compose dependency

Decisions Made:
  Single Gradle module kept — multi-module adds build complexity without proportional benefit for this app size
  ResultScreen in feature/result/presentation — consistent with feature-first even without domain/data layers
  HomeViewModel: two StateFlows → one combine() → single StateFlow<HomeUiState>
  SudokuRepository: concrete class → interface in domain + impl in data + @Binds in GameDataModule

---

## 2026-05-30 (AGENTS.md Compaction)

### Compacted from AGENTS.md
Moved the following sections to CHANGE_HISTORY.md to bring AGENTS.md under 150 lines:
- Issue #1–#6 verbose history blocks
- Full "Implementation Plan (Ordered)" section (14 steps)
- "Implementation Review" table
- Verbose Features section (condensed to a table in AGENTS.md)
- Old `GameUiState` definition (replaced with updated version reflecting Issue #6 fields)

### Archived: Implementation Plan (Issue #1 Ordered Steps)
1. Project setup — Gradle deps, Hilt, Compose, Material3, Navigation ✅
2. Data models — SudokuCell, SudokuBoard, Difficulty, GameState ✅
3. Puzzle generator — backtracking + uniqueness checker ✅ (bug fixed Issue #5)
4. Repository & use cases ✅
5. Theme — Material3 light/dark, color tokens ✅
6. HomeScreen + HomeViewModel ✅
7. GameViewModel — full game logic, undo stack, mistake counter, pencil mode, hints ✅
8. SudokuGrid + SudokuCell composables — cell states, pencil mini-grid ✅
9. NumberPad + GameControls composables ✅
10. GameScreen layout ✅
11. ResultScreen ✅
12. Navigation — Home → Game → Result ✅
13. Theme persistence — DataStore for dark/light preference ✅
14. Polish — animations, accessibility content descriptions ⏳

### Archived: Completed Issues Summary
- **#1** Initial Application Plan: full MVVM Sudoku implementation
- **#2** IndexOutOfBoundsException on SudokuGrid: safety check + progress indicator
- **#3** Hilt kotlinx-metadata-jvm incompatibility: upgraded to Hilt 2.59.2
- **#4** Hilt @AndroidEntryPoint missing value (kapt): migrated to KSP
- **#5** SudokuGenerator.countSolutions terminal condition: changed `if (nextRow == 9)` to `if (r == 9)` guard at top of recursive function
- **#6** UX Improvements (lock cells, same-number highlight, conflict flash, instant fill, number counters)

---

## 2026-05-30 (Issue #6 — UX Improvements)

### Feature 1: Lock Correctly Filled Cells
- `GameViewModel.enterNumber()` — added guard: `if (cell.value != 0 && !cell.isWrong) return`
- `GameViewModel.eraseSelected()` — same guard added; only Undo can revert a correctly placed digit

### Feature 2: Same-Number Highlighting
- `Color.kt` — added `SameNumberBgLight/Dark` (amber-100/dark amber) and `SameNumberCrossBgLight/Dark` (very faint amber)
- `GameUiState` — new `selectedNumber: Int` field; updated in `selectCell()` and `setSelectedNumber()`
- `SudokuGrid.kt` — precomputes `rowsWithNumber` and `colsWithNumber` before cell loop; passes `isSameNumber` and `isSameNumberCross` booleans to each cell
- `SudokuCellComposable` — new params `isSameNumber`, `isSameNumberCross`; expanded background priority

### Feature 3: Conflict Detection + Flash
- `Color.kt` — added `ConflictCellBgLight/Dark` (red-100/dark red)
- `GameViewModel` — `findConflicts()` helper; `flashConflictCells()` sets `conflictCells` in state and clears it after 600ms via a cancellable coroutine job
- `GameUiState` — new `conflictCells: Set<Pair<Int,Int>>`
- `SudokuGrid.kt` — receives `conflictCells`, computes `isConflict` per cell
- `SudokuCellComposable` — new `isConflict` param; flashes red when set

### Feature 4: Instant Fill Mode
- `GameUiState` — new `isInstantFillMode: Boolean`
- `GameViewModel` — `toggleInstantFillMode()` (mutually exclusive with pencil mode); `setSelectedNumber()` for number-first selection; `selectCell()` routes to `enterNumber()` when mode is active
- `GameControls.kt` — fourth `ControlItem` ("Fill" with `FlashOn` icon); horizontal padding reduced to `20.dp` to accommodate 4 items
- `GameScreen.kt` — `onNumberClick` routes to `setSelectedNumber()` or `enterNumber()` based on mode

### Feature 5: Number Counters + Auto-Disable
- `GameUiState` — new `numberRemainingCounts: List<Int>` (index 0 = digit 1)
- `GameViewModel` — `computeRemainingCounts()` called in `updateCellInBoard()`, `initGame()`, and `undo()`
- `NumberPad.kt` — `NumberButton` gains `remaining` and `isActive` params; counter text below digit; dimmed at alpha 0.35 when exhausted; active border highlight (2dp primary)
- `GameScreen.kt` — passes `selectedNumber` and `numberRemainingCounts` to `NumberPad`

---

## 2026-05-30 (Review & Bug Fix)

### Bug Fix — Issue #5: SudokuGenerator.countSolutions terminal condition
- **File:** `app/src/main/java/com/lumos/sudoku/data/generator/SudokuGenerator.kt`
- **Problem:** The recursive uniqueness solver used "if next position is row 9, count" logic. When cell (8,8) was removed from the puzzle, `solveRecursive(8,8)` triggered the terminal check immediately without filling or validating cell (8,8), always reporting 1 solution regardless of how many values were valid there. This allowed `removeCells` to remove (8,8) even when the puzzle had multiple solutions.
- **Fix:** Changed the terminal condition to `if (r == 9)` at the start of the recursive function. The count is now incremented only when the solver is called with `r=9`, meaning all 81 cells were successfully placed before counting.

### Implementation Review
- All 13 of 14 plan steps verified complete.
- Only Step 14 (polish: animations, accessibility content descriptions) remains pending.
- Noted: `SudokuBoard.kt` is defined but unused — `SudokuRepository` returns `List<List<SudokuCell>>` directly.
- GitHub repository has only Issue #1 (closed). All other issues were tracked locally in AGENTS.md.

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
