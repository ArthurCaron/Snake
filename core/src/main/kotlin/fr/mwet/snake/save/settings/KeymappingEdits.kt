package fr.mwet.snake.save.settings

import fr.mwet.snake.inputs.GameCommand
import fr.mwet.snake.inputs.GeneralCommand

fun Keymappings.addGameKey(action: GameCommand, key: Int) = Keymappings(
    game = game.map { if (it.action == action) it.copy(keys = it.keys - key + key) else it.copy(keys = it.keys - key) },
    general = general.map { it.copy(keys = it.keys - key) },
)

fun Keymappings.removeGameKey(action: GameCommand, key: Int) = Keymappings(
    game = game.map { if (it.action == action) it.copy(keys = it.keys - key) else it },
    general = general,
)

fun Keymappings.addGeneralKey(action: GeneralCommand, key: Int) = Keymappings(
    game = game.map { it.copy(keys = it.keys - key) },
    general = general.map { if (it.action == action) it.copy(keys = it.keys - key + key) else it.copy(keys = it.keys - key) },
)

fun Keymappings.removeGeneralKey(action: GeneralCommand, key: Int) = Keymappings(
    game = game,
    general = general.map { if (it.action == action) it.copy(keys = it.keys - key) else it },
)
