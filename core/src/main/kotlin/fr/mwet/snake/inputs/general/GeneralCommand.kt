package fr.mwet.snake.inputs.general

import fr.mwet.snake.DI
import fr.mwet.snake.Game
import fr.mwet.snake.events.MenuEvent.PlayGameClicked
import fr.mwet.snake.events.MenuEventBus
import fr.mwet.snake.screens.MainMenuScreen

interface GeneralCommand {
    fun execute()
}

class GoBackToMainMenu : GeneralCommand {
    override fun execute() {
        Game.setScreen<MainMenuScreen>()
    }
}

class StartGame : GeneralCommand {
    override fun execute() {
        DI.inject<MenuEventBus>().emit(PlayGameClicked)
    }
}
