package fr.mwet.snake.game

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.Timer
import fr.mwet.snake.Game
import fr.mwet.snake.events.GameEvent
import fr.mwet.snake.events.GameEventBus
import fr.mwet.snake.events.GameEventListener
import fr.mwet.snake.render.DisintegratingSnakeRenderer
import fr.mwet.snake.render.FoodRenderer
import fr.mwet.snake.render.SnakeRenderer
import fr.mwet.snake.screens.MainMenuScreen

class GameWorld(gameEventBus: GameEventBus) : GameEventListener {
    val food = Food()
    val snake = Snake(gameEventBus)
    val foodRenderer = FoodRenderer(food)
    val snakeRenderer = SnakeRenderer(snake)
    val disintegratingSnakeRenderer = DisintegratingSnakeRenderer(snake)

    private var gameOver = false

    fun show() {
        newGame()
    }

    private fun newGame() {
        gameOver = false
        snake.reset()
        snake.setSpeed(3f)
        snakeRenderer.reset()
        food.newFood(snake) // Should give a list of possible places instead, or forbidden places
        foodRenderer.reset()
        disintegratingSnakeRenderer.reset()
    }

    fun update(delta: Float) {
        if (gameOver) return

        snake.update(delta)
        if (snake.hitFoodTest(food)) {
            food.newFood(snake)
        }
    }

    fun render(batch: SpriteBatch, delta: Float) {
        if (gameOver) {
            disintegratingSnakeRenderer.render(batch, delta)
            return
        }
        foodRenderer.render(batch, delta)
        snakeRenderer.render(batch, delta)
    }

    override fun onEvent(event: GameEvent) {
        when (event) {
            GameEvent.GameOver -> gameOver()
            GameEvent.FoodEaten -> {}
            GameEvent.SnakeMoved -> {}
        }
    }

    private fun gameOver() {
        gameOver = true
        disintegratingSnakeRenderer.disintegrate(snake)
        Timer.schedule(object : Timer.Task() {
            override fun run() {
                Game.setScreen<MainMenuScreen>()
            }
        }, 1.5f)
    }
}
