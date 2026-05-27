package fr.mwet.snake.inputs

import com.badlogic.gdx.Input.Keys.*
import fr.mwet.snake.DI
import fr.mwet.snake.Game
import fr.mwet.snake.entities.Direction
import fr.mwet.snake.entities.Snake
import fr.mwet.snake.screens.MainMenuScreen
import ktx.app.KtxInputAdapter

class GameInputProcessor(private val snake: Snake) : KtxInputAdapter {
    val game = DI.inject<Game>()

    override fun keyDown(keycode: Int): Boolean {
        when (keycode) {
            UP, W -> snake.setDirection(Direction.UP)
            RIGHT, D -> snake.setDirection(Direction.RIGHT)
            DOWN, S -> snake.setDirection(Direction.DOWN)
            LEFT, A -> snake.setDirection(Direction.LEFT)
            ESCAPE -> game.setScreen<MainMenuScreen>()
        }
        return true
    }
}
