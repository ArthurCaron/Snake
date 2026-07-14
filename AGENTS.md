# Snake Agent Notes

This is a Kotlin/libGDX/KTX Snake game. Prefer project-specific context from this file and `docs/codex-project-memory.md` before doing a broad repository scan.

## Reusable Skill

- Reusable libGDX/KTX guidance lives at `F:\Workspace\codex\skills\libgdx-kotlin-game`.
- When available in Codex, use `$libgdx-kotlin-game` for cross-project architecture, save/web/assets, lifecycle, and review guidance.
- Keep Snake-specific facts in this repo. Promote only stable reusable lessons into the skill.

## What To Read First

- `docs/codex-project-memory.md` for architecture decisions, current direction, and review preferences.
- `core/src/main/kotlin/fr/mwet/snake/game` for gameplay state and rules.
- `core/src/main/kotlin/fr/mwet/snake/screens` for screen flow and UI behavior.
- `core/src/main/kotlin/fr/mwet/snake/save` for save/settings/metadata serialization.
- `core/src/main/kotlin/fr/mwet/snake/inputs` and `core/src/main/kotlin/fr/mwet/snake/events` for commands and events.
- `core/src/main/kotlin/fr/mwet/snake/assets` and `core/src/main/kotlin/fr/mwet/snake/render` for asset access and rendering.
- `DI.kt`, `Game.kt`, and `Main.kt` when lifecycle, ownership, or dependency wiring matters.

## What To Skip By Default

- All `build/`, `.gradle/`, `.idea/`, and generated output folders.
- `assets/textures/textures*.png` and `assets/textures/textures.atlas` unless debugging packed assets.
- `assets/included-in-template/` unless debugging default libGDX/KTX UI assets.
- `lwjgl3/` and `android/` unless the issue is platform-launcher-specific.
- Binary/source art files under `sourceAssets/xcfFiles/` and `sourceAssets/piskelFiles/` unless the task is about art assets.
- `teavm/build/` output. Read `teavm/src` and `teavm/build.gradle` only for web/TeaVM issues.

## Commands

- Compile/check core and web: `.\gradlew.bat core:compileKotlin teavm:buildJavaScript`
- Run desktop: `.\gradlew.bat lwjgl3:run`
- Run web: `.\gradlew.bat teavm:run`
- The user has WSL2 available, but using `gradlew.bat` from PowerShell is preferred unless there is a reason not to.

## Review Preferences

- The user is an experienced Kotlin developer. Focus on architecture, game-state correctness, lifecycle, serialization, TeaVM/web constraints, and libGDX/KTX conventions.
- Be explicit when existing code is good and should be kept.
- Avoid broad refactors unless they clearly reduce risk or match the project's current direction.
- The user cares about avoiding unnecessary allocation and GC pressure in gameplay code. Prefer mutable scratch objects and pools where that fits libGDX style, but call out ownership risks clearly.


