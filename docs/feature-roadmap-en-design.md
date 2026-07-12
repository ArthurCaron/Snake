# Design Roadmap - Snake

This document is the design-focused version of the roadmap. It is less technical than `feature-roadmap.md` and is meant to help plan the player experience, menus, sounds, mood, and polish.

The goal is not just to make Snake work. The goal is to make it feel like a small finished game.

## Game Goal

Snake should feel simple, clear, responsive, and polished.

The game should eventually include:

- a clean main menu
- a pause menu
- settings
- key rebinding
- audio settings
- language settings
- high scores
- save and continue
- game over and victory screens
- small animations and feedback
- desktop and browser support

## Existing Foundation

- The player can start a game.
- The snake moves on a grid.
- Food exists.
- The snake grows when it eats.
- The game detects loss.
- A simple main menu exists.
- Basic sounds exist.
- Save/settings infrastructure exists.
- Keybinding data exists.
- High-score data exists, but scoring is not wired into gameplay yet.
- Language files exist, but language switching is not finished.
- A settings screen exists, but it is empty.

## Menus To Design

For each menu, decide:

- what appears on screen
- which buttons exist
- where buttons are placed
- what the selected/hovered/pressed states look like
- what animation happens when the menu opens or closes
- what sound plays when the player interacts
- how the player goes back
- what must be obvious within a few seconds

### 1. Loading Screen

Purpose: show that the game is starting.

Design:

- game logo or title
- loading bar
- small loading animation
- visual mood: cute, arcade, dark, retro, colorful, minimal, etc.

Possible sounds:

- short startup sound
- music fading in after loading

### 2. Main Menu

Purpose: first real impression of the game.

Possible buttons:

- Play
- Continue
- Settings
- High Scores
- Quit

Design:

- strong title/logo
- clear primary Play button
- background art or subtle background animation
- button hover animation
- button click animation
- disabled Continue state when no save exists

Sounds needed:

- button hover
- button click
- disabled button
- start game

### 3. Pause Menu

Purpose: pause without leaving the current run.

Possible buttons:

- Resume
- Restart
- Settings
- Main Menu

Design:

- darkened game behind the menu
- clear centered pause panel
- opening/closing animation
- clear "Paused" label
- confirmation when abandoning the run

Sounds needed:

- pause
- resume
- menu navigation
- confirm
- cancel/back

### 4. Settings Menu

Purpose: hub for all player options.

Possible categories:

- Audio
- Controls
- Language
- Gameplay/Display

Design:

- tabs or category buttons
- Back button
- Apply button if needed
- Reset Defaults button
- confirmation for resetting options

Sounds needed:

- option changed
- apply/confirm
- cancel/back
- reset defaults

### 5. Audio Menu

Purpose: control sound and music.

Options:

- master volume
- music volume
- sound effects volume
- mute all

Design:

- volume sliders
- sound icons
- muted/unmuted state
- preview sound when changing volume

Sounds needed:

- test sound
- slider movement
- mute/unmute

### 6. Controls Menu

Purpose: let the player change keys.

Actions to show:

- move up
- move right
- move down
- move left
- pause
- back/main menu
- confirm/menu action

Design:

- readable list of actions
- current keys visible
- edit button per action
- "Press a key..." capture state
- conflict message if a key is already used
- reset controls button

Sounds needed:

- start rebinding
- key saved
- conflict/error
- reset controls

### 7. Language Menu

Purpose: change language.

Planned languages:

- English
- French

Design:

- language list or buttons
- active language indicator
- optional flags or plain text
- message if language applies immediately

Sounds needed:

- language changed
- confirm

### 8. High Scores

Purpose: show player progress and records.

Design:

- ranked score table
- player name or initials if added later
- score value
- date or run info if useful
- Back button
- optional clear scores action in settings

Sounds needed:

- open high-score screen
- new record
- clear scores

### 9. Game Over Screen

Purpose: make losing feel finished, not abrupt.

Design:

- game over message
- final score
- best score
- new record callout when relevant
- Restart button
- Main Menu button
- snake disintegration or defeat animation

Sounds needed:

- loss
- new record
- restart click

### 10. Victory Screen

Purpose: celebrate filling the board.

Design:

- victory message
- final score
- special animation
- Play Again button
- Main Menu button

Sounds needed:

- victory
- short fanfare

### 11. Continue Game Flow

Purpose: resume a saved run.

Design:

- Continue button in main menu
- save summary: score, date, difficulty, maybe time played
- invalid save message
- confirmation when starting a new game would replace an old save
- resuming should pause the game and have a timer before starting so the player doesn't lose immediately

Sounds needed:

- continue game
- new game
- confirm overwrite
- save error

## Sound List

### Gameplay Sounds

- snake movement
- food eaten
- loss
- victory
- new high score
- pause
- resume
- countdown tick
- countdown start

### Menu Sounds

- button hover
- button click
- back/cancel
- confirm
- option changed
- disabled action
- error/conflict
- menu open
- menu close

### Music

Possible music tracks:

- main menu music
- gameplay music
- softer pause/menu variation
- short victory music
- short defeat sting

Music should loop comfortably. It should not become annoying after a few minutes.

## Features To Imagine

These are not all required immediately, but they can make the game feel more professional.

- livelier snake animation
- eyes/head reacting to movement or food
- animated food
- effect when food is eaten
- optional grid visibility
- multiple visual themes
- multiple fruits or food types
- easy/normal/hard difficulty
- speed increasing with score
- countdown before start/resume
- very simple tutorial
- short tip after losing
- transitions between screens
- animated background
- unlockable themes or colors

## Design Tasks

### 1. Choose The Mood

Pick the overall direction:

- cute
- arcade
- dark
- funny
- retro
- colorful
- minimal
- cozy
- weird
- something else

The mood affects colors, fonts, menu animation, sounds, and music.

### 2. Design The Menus

Make rough sketches first. Paper sketches are fine.

Priority menus:

1. Main Menu
2. Pause Menu
3. Settings
4. Audio Settings
5. Controls
6. Game Over
7. High Scores

Later menus:

1. Victory
2. Language
3. Continue Game
4. Theme/Difficulty if added

### 3. Define The Sound Style

For each sound, write:

- name
- when it plays
- mood
- short or long
- soft or strong
- example/reference if there is one

Example:

- Name: button click
- When: player clicks a menu button
- Mood: clean, soft, satisfying
- Length: very short
- Volume: subtle

### 4. Propose UI Text

Write short text for buttons and messages:

- Play
- Continue
- Resume
- Restart
- Settings
- High Scores
- Game Over
- Victory
- New Record
- Press a key
- Are you sure?
- Save found
- No save found
- Controls reset

Keep text short and easy to understand.

### 5. List Bonus Ideas

Capture all ideas, even if they are too big for now:

- animations
- colors
- sounds
- jokes
- effects
- snake details
- food ideas
- theme ideas
- difficulty ideas
- menu transitions

We can sort them later.

## Design Priority

First pass:

1. Main menu layout
2. Pause menu layout
3. Settings structure
4. Audio settings
5. Controls settings
6. Game over screen
7. High scores screen

Second pass:

1. Victory screen
2. Language menu
3. Continue game flow
4. Extra animations
5. Theme ideas
6. Sound polish

## Questions To Answer

- What is the main mood of the game?
- Should the game feel cute, funny, serious, retro, or arcade?
- What colors represent the game best?
- Should menus be simple or animated?
- Should sounds be realistic, retro, electronic, soft, or silly?
- Should music be calm or energetic?
- What makes this Snake more pleasant than a basic Snake clone?
- What should a new player understand in less than 5 seconds?
- What should happen when the player loses?
- What should happen when the player gets a new high score?

## Notes

Write ideas here:

- 

## Proposed Sounds

Write sound ideas here:

- 

## Sketch Checklist

- [ ] Loading screen
- [ ] Main menu
- [ ] Pause menu
- [ ] Settings menu
- [ ] Audio settings
- [ ] Controls settings
- [ ] Language menu
- [ ] High scores
- [ ] Game over
- [ ] Victory
- [ ] Continue game flow
