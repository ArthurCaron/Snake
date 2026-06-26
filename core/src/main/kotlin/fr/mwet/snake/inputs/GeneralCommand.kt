package fr.mwet.snake.inputs

import fr.mwet.snake.DI
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
    fun execute()
}

object NullGeneralCommand : GeneralCommand {
    override fun execute() {}
}

object StartGameCommand : GeneralCommand {
    override fun execute() {
        DI.inject<MenuEventBus>().emit(PlayGameClicked)
    }
}
