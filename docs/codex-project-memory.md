# Snake Project Memory

This file is durable context for future Codex chats. Read it before doing a whole-project review or architecture work.

## Project Shape

- Kotlin/libGDX/KTX Snake game generated from a gdx-liftoff style setup.
- Main gameplay code lives in `core/src/main/kotlin/fr/mwet/snake`.
- The important custom modules are `core`, `teavm`, and a little `lwjgl3` launcher code.
- `android` and most launcher resources are still mostly generated/default setup.
- Web support matters. TeaVM reflection, asset copying, and browser storage constraints should be considered when touching save/serialization/assets.

## Current Architecture

- `Main` delegates the libGDX `ApplicationListener` lifecycle to the singleton `Game` and initializes `DI`.
- `Game` is a `KtxGame<KtxScreen>` and tracks viewports for resize.
- `DI` owns singleton wiring and disposal. Treat `DI` as the main owner of registered singleton resources.
- `GameWorld` coordinates gameplay decisions: ticker, snake, food, cells, win/loss, and events.
- `Snake` owns snake body state and movement/body-collision rules. It should not know about food, screens, sound, saves, or UI.
- `Ticker` owns grid-step timing. Prefer making game mutation happen only on actual ticks.
- `Food` is just position/state plus simple helpers.
- `Cells` owns the board-cell cache and currently exposes `randomAvailableCell(snake)`.
- Events are used for cross-system reactions such as sound and screen/game state changes.

## Gameplay Direction

- Keep movement atomic per grid step:
  1. If no tick is due, return.
  2. Read `snake.nextHeadPosition`.
  3. Determine whether the snake will eat.
  4. Determine wall/body collision using the current snake state and whether the tail will move.
  5. Mutate the snake once.
  6. Emit `SnakeMoved`, `FoodEaten`, `Lost`, or `Won` as appropriate.
- `GameWorld` should remain the decision-maker for food and win/loss.
- `Snake` should expose movement/collision primitives, not game orchestration.
- Moving into the current tail cell is legal only when the snake is not growing.
- Avoid duplicating the full snake for normal movement simulation; a predicted next-head position is enough.

## Save System

- Save code is split between runtime models and serializable DTOs.
- `JsonStore` sits over `TextStore`.
- Desktop uses `FileTextStore` with local files.
- WebGL uses `PreferencesTextStore`, because browser builds cannot rely on normal desktop filesystem semantics.
- Settings and metadata already use the save system.
- Game save is still the next major save-system feature.
- Future game snapshots should include schema version, snake segment positions, current/future direction, food position, score, game state, and enough tick/pause state to resume cleanly.
- Be careful with TeaVM reflection when adding serializable save DTOs.

## Assets And Web

- `sourceAssets` is the human-edited asset root. Treat `assets` as generated runtime output.
- Packed gameplay textures are generated from `sourceAssets/textures` into `assets/textures`.
- Fonts, i18n bundles, music, and sounds are synced from `sourceAssets` into matching folders under `assets` by Gradle.
- Root runtime assets such as `LoadingAssets.png` and `SplashScreen.png` live in `sourceAssets/root` and are copied to the root of `assets` by Gradle.
- Web/favicon custom assets live under `sourceAssets/web` and are copied by Gradle rather than moved into generated folders.
- Pixel art should use crisp nearest-neighbor filtering unless intentionally smoothed.
- `assets/included-in-template` contains default template UI assets; do not read or analyze it unless the task is specifically about those assets.

## Reusable Tooling Direction

- The user definitely wants to turn `tools/asset-catalog-generator` into reusable project infrastructure later, but not right now.
- Do not suggest a long-term copy/paste workflow for future projects. The preferred future shape is a reusable Gradle plugin that can be imported/configured by each game project.
- A future plugin should package the generator and Gradle wiring together: asset sync, TexturePacker integration, generated source directory setup, task dependencies, and configurable catalog specs.
- Expected plugin configuration should cover source/runtime asset directories, generated package name, texture atlas path, and file catalogs such as sounds, music, and fonts.
- Local reuse should probably start with a Gradle composite build from something like `F:\Workspace\io.karon\libgdx-kotlin-tooling`, then later move to `mavenLocal()` or a real package registry if useful.
- Wait until the Snake project structure is more solid, likely when moving on from this project, before extracting this into a reusable plugin.

## Current Known Next Work

- Pause/resume flow before full game save.
- Game snapshot save/load.
- Score and high-score integration.
- Language switching after font/accent support is solved.
- Settings screen/key remapping UI.
- Possible event-bus unsubscribe/scope support if listeners stop being singleton-lifetime objects.
- Tests for movement edge cases, save serialization, corrupt-save fallback, keymapping grouping, and TeaVM-sensitive DTOs.
- Later, extract `tools/asset-catalog-generator` into a reusable Gradle plugin rather than copying generator modules/build snippets between projects.

## Reading Strategy For Future Chats

Start with:

- `AGENTS.md`
- this file
- `core/src/main/kotlin/fr/mwet/snake/game`
- `core/src/main/kotlin/fr/mwet/snake/screens`
- `core/src/main/kotlin/fr/mwet/snake/save`
- `core/src/main/kotlin/fr/mwet/snake/inputs`
- `core/src/main/kotlin/fr/mwet/snake/events`
- `core/src/main/kotlin/fr/mwet/snake/assets`
- `core/src/main/kotlin/fr/mwet/snake/render`
- `DI.kt`, `Game.kt`, `Main.kt`

Skip unless relevant:

- `build/`, `teavm/build/`, `.gradle/`, `.idea/`
- `assets/textures/textures*.png`
- `assets/textures/textures.atlas`
- `assets/included-in-template/`
- `lwjgl3/src/main/resources/libgdx*.png`
- `android/` default launcher/resources
- `sourceAssets/xcfFiles/`
- `sourceAssets/piskelFiles/`

Use `rg --files` and targeted `rg` searches before opening many files. Do not recursively inspect generated libGDX folders unless the user asks about them or the issue points there.

## User Preferences

- The user is a senior Kotlin developer and knows how to code.
- They want architectural feedback, not hand-holding.
- They like finishing related improvements in batches.
- They care about GC pressure and object allocation in gameplay code.
- They prefer clear opinions, including "keep doing this" when something is already good.
- They are open to libGDX/KTX/game-architecture guidance.
