package fr.mwet.snake.game

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import fr.mwet.snake.DI
import fr.mwet.snake.Game
import fr.mwet.snake.events.GameEventBus
import fr.mwet.snake.render.FoodRenderer
import fr.mwet.snake.render.SnakeRenderer
import fr.mwet.snake.screens.MainMenuScreen

class GameWorld {
    val gameEventBus = DI.inject<GameEventBus>()
    val food = Food()
    val foodRenderer = FoodRenderer(food)
    val snake = Snake(this, gameEventBus)
    val snakeRenderer = SnakeRenderer(snake)

    fun show() {
        newGame()
    }

    private fun newGame() {
        snake.reset()
        snake.setSpeed(3f)
        snakeRenderer.reset()
        food.newFood(snake) // Should give a list of possible places instead, or forbidden places
        foodRenderer.reset()
    }

    fun lost() {
        Game.setScreen<MainMenuScreen>()
    }

    fun render(batch: SpriteBatch, delta: Float) {
        if (snake.hitFoodTest(food)) {
            food.newFood(snake)
        }
        foodRenderer.render(batch, delta)
        snake.update(delta)
        snakeRenderer.render(batch, delta)
    }
}
