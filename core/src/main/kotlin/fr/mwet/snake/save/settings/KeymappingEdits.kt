package fr.mwet.snake.save.settings

import fr.mwet.snake.inputs.GameCommand
import fr.mwet.snake.inputs.GeneralCommand

fun Keymappings.rebindGame(action: GameCommand, key: Int) = Keymappings(
    game = game.map { if (it.action == action) it.copy(keys = listOf(key)) else it.copy(keys = it.keys - key) },
    general = general,
)

fun Keymappings.rebindGeneral(action: GeneralCommand, key: Int) = Keymappings(
    game = game,
    general = general.map { if (it.action == action) it.copy(keys = listOf(key)) else it.copy(keys = it.keys - key) },
)
