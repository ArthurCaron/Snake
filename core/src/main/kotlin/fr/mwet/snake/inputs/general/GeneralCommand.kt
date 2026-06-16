package fr.mwet.snake.inputs.general

import fr.mwet.snake.DI
import fr.mwet.snake.events.MenuEvent.PlayGameClicked
import fr.mwet.snake.events.MenuEventBus

interface GeneralCommand {
    fun execute()
}

class StartGame : GeneralCommand {
    override fun execute() {
        DI.inject<MenuEventBus>().emit(PlayGameClicked)
    }
}
