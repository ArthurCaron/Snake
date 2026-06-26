package fr.mwet.snake.inputs

import fr.mwet.snake.events.MenuEvent.PlayGameClicked
import fr.mwet.snake.events.MenuEventBus

enum class GeneralActionId(val generalCommand: GeneralCommand) {
    Null(NullGeneralCommand),
    StartGame(StartGameCommand);

    companion object {
        fun from(generalCommand: GeneralCommand) = when (generalCommand) {
            NullGeneralCommand -> Null
            StartGameCommand -> StartGame
        }
    }
}

sealed interface GeneralCommand {
    fun execute(generalCommandContext: GeneralCommandContext)
}

class GeneralCommandContext(
    val menuEventBus: MenuEventBus,
)

object NullGeneralCommand : GeneralCommand {
    override fun execute(generalCommandContext: GeneralCommandContext) {}
}

object StartGameCommand : GeneralCommand {
    override fun execute(generalCommandContext: GeneralCommandContext) {
        generalCommandContext.menuEventBus.emit(PlayGameClicked)
    }
}
