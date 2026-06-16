package fr.mwet.snake.inputs.general

import fr.mwet.snake.Game
import fr.mwet.snake.screens.MainMenuScreen

interface GeneralCommand {
    fun execute()
}

class GoBackToMainMenu : GeneralCommand {
    override fun execute() {
        Game.setScreen<MainMenuScreen>()
    }
}
