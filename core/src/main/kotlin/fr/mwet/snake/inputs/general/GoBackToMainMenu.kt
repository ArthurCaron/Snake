package fr.mwet.snake.inputs.general

import fr.mwet.snake.Game
import fr.mwet.snake.screens.MainMenuScreen

class GoBackToMainMenu : GeneralCommand {
    override fun execute() {
        Game.setScreen<MainMenuScreen>()
    }
}
