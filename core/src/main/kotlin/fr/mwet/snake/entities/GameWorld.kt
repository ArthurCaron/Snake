package fr.mwet.snake.entities

import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import fr.mwet.snake.DI
import fr.mwet.snake.Game
import fr.mwet.snake.inputs.GameInputProcessor
import fr.mwet.snake.screens.MainMenuScreen


val FOOD_COLORS = arrayOf(Color.GREEN, Color.BLUE, Color.RED, Color.ORANGE, Color.MAGENTA, Color.CYAN)

class GameWorld {
    private val game = DI.inject<Game>()
    private val inputMultiplexer by lazy {
        DI.inject<InputMultiplexer>().apply {
            addProcessor(GameInputProcessor(snake))
        }
    }
    private val food by lazy { Food() }
    private val snake by lazy { Snake(this) }

    fun show() {
        inputMultiplexer
        newGame()
    }

    fun newGame() {
        food.reset(snake)
        snake.reset()
//        snake.setSpeed(3f)
    }

    fun lost() {
        game.setScreen<MainMenuScreen>()
    }

    fun render(batch: SpriteBatch, delta: Float) {
        food.render(batch, delta)
        if (snake.hitFoodTest(food)) {
            food.newFood(snake)
        }
        snake.render(batch, delta)
    }
}
