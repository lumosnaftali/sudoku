# Sudoku

A modern Android Sudoku game built with Kotlin, Jetpack Compose, and Material3.

## Features

- **Standard 9×9 Sudoku** grid with 3×3 box regions
- **Three difficulty levels** — Easy, Medium, Hard
- **Pencil / notes mode** — jot down candidates in a mini 3×3 grid inside each cell
- **Hint system** — reveal the correct answer for a selected cell when stuck
- **Wrong number feedback** — incorrect inputs are shown in red
- **Undo** — step back through your move history
- **Mistake counter** — tracks how many wrong numbers you've entered
- **Dark / Light mode** — toggle anytime, preference saved across sessions

## Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose + Material3
- **Architecture:** MVVM (ViewModel + StateFlow)
- **DI:** Hilt
- **Navigation:** Compose Navigation
- **Concurrency:** Kotlin Coroutines
- **Persistence:** Jetpack DataStore (theme preference)

## Minimum Requirements

- Android 8.0 (API 26)

## Architecture

The app follows a clean MVVM architecture with unidirectional data flow:

```
UI (Compose) → ViewModel → UseCase → Repository
      ↑               |
      └── StateFlow ←─┘
```

See [AGENTS.md](AGENTS.md) for the full architecture and implementation plan.

## Getting Started

1. Clone the repository.
2. Open in Android Studio (latest stable).
3. Run on an emulator or physical device (API 26+).

## Changelog

See [CHANGE_HISTORY.md](CHANGE_HISTORY.md).
