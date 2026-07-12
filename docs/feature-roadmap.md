# Snake Feature Roadmap

This roadmap tracks player-facing features and supporting systems needed for Snake to feel like a polished reusable game foundation.

Status:

- `[x]` implemented enough to keep as a foundation
- `[ ]` not done
- `[~]` partially implemented or needs a second pass

## Current Foundation

- [x] Shared gameplay code lives in `core`.
- [x] Desktop and TeaVM/web targets exist.
- [x] Main menu, loading screen, and game screen exist.
- [x] Game loop uses a tick-based `GameWorld`/`Ticker` flow.
- [x] Snake movement is food-agnostic and decided by `GameWorld`.
- [x] Event buses exist for game/menu reactions.
- [x] Settings, metadata, and save text-store layers exist.
- [x] Desktop uses file-backed save storage and WebGL uses preferences-backed storage.
- [x] Keymapping data supports multiple keys per action.
- [x] Basic sounds play from events.
- [x] Basic i18n bundles exist.
- [~] Settings screen exists as an empty shell.
- [~] High-score data is persisted, but gameplay score does not feed it yet.
- [~] Music is loaded, but there is no player/settings flow for it yet.
- [~] Game save repository and snapshot types exist but are empty.

## P0 - Core Play Session

- [ ] Add a `GameSession` or equivalent state model for `NotStarted`, `Playing`, `Paused`, `Lost`, and `Won`.
- [ ] Make `GameScreen.show()` support both starting a new game and resuming an existing game.
- [ ] Replace `Pause -> MainMenuScreen` with a real pause flow.
- [ ] Add pause menu actions: resume, restart, settings, main menu.
- [ ] Add game-over flow with score display, restart, and main menu.
- [ ] Add win flow for a full board.
- [ ] Add score state to gameplay.
- [ ] Emit score-relevant events or update score directly when food is eaten.
- [ ] Persist high scores after a completed run.
- [ ] Show current score during gameplay.
- [ ] Show high scores from metadata in a menu screen.

Done means: the player can start, pause, resume, lose/win, restart, and see a saved high score without relying on debug state or console output.

## P0 - Game Save And Continue

- [ ] Define `GameSnapshotSave` DTOs with schema version.
- [ ] Snapshot snake segment positions.
- [ ] Snapshot current and future direction.
- [ ] Snapshot food position.
- [ ] Snapshot score.
- [ ] Snapshot session state.
- [ ] Snapshot ticker progress only if resume timing needs to be exact.
- [ ] Implement `GameSaveRepository.load`, `save`, `exists`, and `delete`.
- [ ] Add runtime conversion between `GameWorld` and save DTOs.
- [ ] Save the current game from pause/exit points.
- [ ] Offer `Continue` on the main menu when a valid save exists.
- [ ] Delete or archive the game save after loss/win/restart.
- [ ] Verify desktop file save and WebGL preferences save.

Done means: closing and reopening the game can continue an in-progress run on desktop and web.

## P1 - Settings Menu

- [ ] Build a real `SettingsScreen`.
- [ ] Add navigation from main menu to settings.
- [ ] Add navigation from pause menu to settings.
- [ ] Add `Back`/`Apply`/`Cancel` behavior.
- [ ] Persist settings through `SettingsRepository`.
- [ ] Apply changed settings without restarting when practical.
- [ ] Add restore-defaults action.
- [ ] Add confirmation for destructive resets.

Done means: player-facing settings are reachable, editable, saved, and reversible.

## P1 - Key Rebinding

- [ ] Build keybinding settings UI grouped by context: game and general/menu.
- [ ] Display human-readable key names.
- [ ] Capture the next key press for rebinding.
- [ ] Support multiple keys per action.
- [ ] Allow removing a key from an action.
- [ ] Detect and explain conflicts.
- [ ] Decide whether conflicts are allowed across contexts.
- [ ] Prevent unplayable mappings, such as no direction keys.
- [ ] Save mappings and rebuild input processors after changes.
- [ ] Add reset-to-default mappings.

Done means: a player can change controls in-game, restart the app, and keep the new mappings.

## P1 - Audio

- [ ] Introduce audio settings: master volume, music volume, sound volume, mute.
- [ ] Add audio settings DTO fields and defaults.
- [ ] Create an audio controller/mixer above `SoundHandler` and `MusicHandler`.
- [ ] Route sound effects through the audio controller.
- [ ] Start/stop/pause/resume music through the audio controller.
- [ ] Apply volume changes live.
- [ ] Add menu sounds for hover/click where appropriate.
- [ ] Add lost/won sounds.
- [ ] Handle browser audio unlock after user gesture.

Done means: sound and music feel intentional, can be adjusted independently, and behave on web.

## P1 - Language And Text

- [ ] Fix font support for French accents and future locales.
- [ ] Stop forcing English in `I18NHandler`.
- [ ] Load bundles from saved locale.
- [ ] Add language selector to settings.
- [ ] Apply language changes to visible UI.
- [ ] Ensure all player-facing strings are in i18n bundles.
- [ ] Add missing strings for pause, settings, score, high scores, game over, win, continue, reset, and keybinding.

Done means: language can be changed from settings and all visible UI follows it.

## P2 - Main Menu And Navigation Polish

- [ ] Add main menu buttons: Play, Continue, Settings, High Scores, Quit where supported.
- [ ] Add keyboard/controller-style menu navigation.
- [ ] Add focused/hover/pressed/disabled visual states.
- [ ] Add consistent transitions between screens.
- [ ] Add confirmation when abandoning an in-progress game.
- [ ] Keep layout responsive across desktop and web canvas sizes.
- [ ] Add visual indication when Continue is unavailable.

Done means: menus feel like a finished game UI, not a test harness.

## P2 - Gameplay Polish

- [ ] Add countdown before a run starts or resumes.
- [ ] Add brief feedback when food is eaten.
- [ ] Add score popup or subtle score animation.
- [ ] Add speed/difficulty options.
- [ ] Decide whether speed increases over time or by score.
- [ ] Add optional grid visibility setting.
- [ ] Add optional snake/fruit theme selection if multiple asset sets remain.
- [ ] Improve body rendering for turns if using segmented art.
- [ ] Add idle/background animation polish.

Done means: gameplay has feedback, pacing, and presentation worthy of a portfolio project.

## P2 - Web And Release Polish

- [ ] Keep favicon/web assets copied from `graphics/web`.
- [ ] Add web loading/fallback text for asset failures.
- [ ] Add fullscreen or fit-to-window behavior if desired.
- [ ] Make web storage failures visible and recoverable.
- [ ] Document browser limitations for audio and save storage.
- [ ] Add desktop release packaging notes.
- [ ] Add a simple release checklist for desktop and web builds.

Done means: desktop and browser builds can be shared without special developer knowledge.

## P2 - Testing And Quality

- [ ] Add unit tests for snake movement edge cases.
- [ ] Test moving into the tail cell with and without growth.
- [ ] Test win condition when no cells remain.
- [ ] Test keymapping grouping and conflict rules.
- [ ] Test settings save/load/default merge.
- [ ] Test corrupt save backup behavior.
- [ ] Test game snapshot round trip.
- [ ] Add a standard validation command list to docs.
- [ ] Run TeaVM build for save/reflection/asset changes.

Done means: risky behavior has cheap regression coverage.

## P3 - Reusable Template Extraction

- [ ] Promote stable Snake architecture lessons into `$libgdx-kotlin-game`.
- [ ] Document the preferred package layout.
- [ ] Document the preferred save/settings pattern.
- [ ] Document the preferred event/input pattern.
- [ ] Extract reusable Gradle/web asset notes.
- [ ] Decide whether to create a starter template from Snake once the structure stabilizes.

Done means: the next Kotlin/libGDX project starts from a proven structure, not from memory.

## Open Design Questions

- [ ] Should pause save automatically, or only when leaving the game?
- [ ] Should Escape pause, go back, or open a confirm dialog depending on context?
- [ ] Should key conflicts be allowed when they trigger multiple commands intentionally?
- [ ] Should high scores be local only, or designed with future online support in mind?
- [ ] Should language changes reload the current screen or update widgets in place?
- [ ] Should game speed be a setting, a difficulty mode, or part of scoring progression?
