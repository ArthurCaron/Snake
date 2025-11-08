package fr.mwet.snake.inputs

import com.badlogic.gdx.Input.Keys.*
import fr.mwet.snake.entities.Snake
import fr.mwet.snake.DI
import fr.mwet.snake.Game
import fr.mwet.snake.screens.MainMenuScreen
import ktx.app.KtxInputAdapter

class GameInputProcessor(private val snake: Snake) : KtxInputAdapter {
    val game = DI.inject<Game>()

    override fun keyDown(keycode: Int): Boolean {
        when (keycode) {
            UP, W -> snake.setDirection(0)
            RIGHT, D -> snake.setDirection(1)
            DOWN, S -> snake.setDirection(2)
            LEFT, A -> snake.setDirection(3)
            ESCAPE -> game.setScreen<MainMenuScreen>()
        }
        return true
    }
}
