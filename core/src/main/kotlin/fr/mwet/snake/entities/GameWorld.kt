package fr.mwet.snake.entities

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import fr.mwet.snake.Game
import fr.mwet.snake.screens.MainMenuScreen

val FOOD_COLORS = arrayOf(Color.GREEN, Color.BLUE, Color.RED, Color.ORANGE, Color.MAGENTA, Color.CYAN)

class GameWorld {
    val food = Food()
    val snake = Snake(this)

    fun show() {
        newGame()
    }

    private fun newGame() {
        food.reset(snake) // Should give a list of possible places instead, or forbidden places
        snake.reset()
        snake.setSpeed(3f)
    }

    fun lost() {
        Game.setScreen<MainMenuScreen>()
    }

    fun render(batch: SpriteBatch, delta: Float) {
        food.render(batch, delta)
        if (snake.hitFoodTest(food)) {
            food.newFood(snake)
        }
        snake.render(batch, delta)
    }
}
