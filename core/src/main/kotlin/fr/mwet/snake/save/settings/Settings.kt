package fr.mwet.snake.save.settings

import fr.mwet.snake.inputs.game.GameCommand
import fr.mwet.snake.inputs.game.NullGameCommand
import fr.mwet.snake.inputs.general.GeneralCommand
import fr.mwet.snake.inputs.general.NullGeneralCommand

data class Settings(
    val userPreferences: UserPreferences,
    val keymappings: Keymappings,
)

data class UserPreferences(
    val version: Int,
    val locale: String,
)

data class Keymappings(
    var game: List<GameKeymapping> = listOf(),
    var general: List<GeneralKeymapping> = listOf(),
)

data class GameKeymapping(
    var action: GameCommand = NullGameCommand,
    var keys: List<Int> = listOf(),
)

data class GeneralKeymapping(
    var action: GeneralCommand = NullGeneralCommand,
    var keys: List<Int> = listOf(),
)
