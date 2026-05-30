# Sudoku

A modern Android Sudoku game built with Kotlin, Jetpack Compose, and Material3.

## Features

- **Standard 9×9 Sudoku** grid with 3×3 box regions
- **Three difficulty levels** — Easy, Medium, Hard
- **Pencil / notes mode** — jot down candidates in a mini 3×3 grid inside each cell
- **Hint system** — reveal the correct answer for a selected cell when stuck
- **Wrong number feedback** — incorrect inputs are shown in red
- **Conflict detection** — entering a number that already exists in the row, column, or box is blocked and flashes red
- **Instant fill mode** — pick a number first, then tap cells to fill them all at once
- **Number counters** — each number button shows how many more can be placed; greys out at zero
- **Undo** — step back through your move history
- **Mistake counter** — tracks how many wrong numbers you've entered (game over at 3)
- **Dark / Light mode** — toggle anytime, preference saved across sessions

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material3 |
| Architecture | MVVM — ViewModel + StateFlow, feature-first packages |
| DI | None — plain Jetpack `viewModelFactory` + `APPLICATION_KEY` |
| Navigation | Compose Navigation |
| Concurrency | Kotlin Coroutines + Flow |
| Persistence | Jetpack DataStore (theme preference) |
| Min SDK | 26 (Android 8.0) |

## Architecture

Feature-first packages with Clean Architecture layers inside each feature:

```
core/               Shared theme, navigation routes, DataStore
feature/
  home/             Difficulty picker + theme toggle
    presentation/   HomeScreen, HomeViewModel, HomeUiState
  game/             Main game logic
    domain/         SudokuCell, GameState, SudokuRepository (interface), use cases
    data/           SudokuGenerator, SudokuRepositoryImpl
    presentation/   GameScreen, GameViewModel, GameUiState, components
  result/           Win / Game Over screen
    presentation/   ResultScreen
```

Unidirectional data flow: `UI → ViewModel → UseCase → Repository → ViewModel (StateFlow) → UI`

## Getting Started

1. Clone the repository.
2. Open in Android Studio (latest stable).
3. Run on an emulator or physical device (API 26+).

---

## Release Signing Setup

The keystore file and credentials are **not committed to git**. New developers need to create them locally before building a release APK.

### Step 1 — Create the keystore directory

```bat
mkdir app\keystore
```

### Step 2 — Generate the keystore

```bat
keytool -genkey -v ^
  -keystore app\keystore\release.jks ^
  -keyalg RSA ^
  -keysize 2048 ^
  -validity 9125 ^
  -alias sudoku-key ^
  -storetype JKS ^
  -storepass YOUR_STORE_PASSWORD ^
  -keypass YOUR_KEY_PASSWORD ^
  -dname "CN=Your Name,O=Your Org,L=City,ST=State,C=XX"
```

**Keystore details:**

| Field | Value |
|---|---|
| File | `app/keystore/release.jks` |
| Alias | `sudoku-key` |
| Validity | 25 years |

### Step 3 — Create keystore.properties

Create a file named `keystore.properties` in the **project root** (it is already gitignored):

```properties
storeFile=app/keystore/release.jks
storePassword=YOUR_STORE_PASSWORD
keyAlias=sudoku-key
keyPassword=YOUR_KEY_PASSWORD
```

### Step 4 — Add signingConfigs to app/build.gradle.kts

```kotlin
// At the top of app/build.gradle.kts, before android {}
val keystoreProperties = java.util.Properties().apply {
    val file = rootProject.file("keystore.properties")
    if (file.exists()) load(file.inputStream())
}

android {
    // ... existing config ...

    signingConfigs {
        create("release") {
            storeFile = file(keystoreProperties["storeFile"] as String)
            storePassword = keystoreProperties["storePassword"] as String
            keyAlias = keystoreProperties["keyAlias"] as String
            keyPassword = keystoreProperties["keyPassword"] as String
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}
```

### Step 5 — Build the release APK

```bat
./gradlew.bat assembleRelease
```

Output: `app/build/outputs/apk/release/app-release.apk`

---

## Changelog

See [CHANGE_HISTORY.md](CHANGE_HISTORY.md).
