package fr.mwet.snake.game

import fr.mwet.snake.events.GameEvent
import fr.mwet.snake.events.GameEvent.*
import fr.mwet.snake.events.GameEventBus
import fr.mwet.snake.events.GameEventListener

class GameWorld(private val gameEventBus: GameEventBus) : GameEventListener {
    val food: Food = Food()
    val snake = Snake(gameEventBus)
    val cells = Cells()

    var gameOver = false

    fun newGame() {
        gameOver = false
        snake.reset()
        snake.setSpeed(8f)
        val availableCells = cells.computeAvailableCells(snake)
        food.reset(availableCells)
    }

    fun update(delta: Float) {
        if (gameOver) return

        snake.update(delta)
        if (snake.hasEatenFood(food)) {
            gameEventBus.emit(FoodEaten)
            snake.ateFood()
            val availableCells = cells.computeAvailableCells(snake)
            food.reset(availableCells)
        }
    }

    override fun onEvent(event: GameEvent) {
        when (event) {
            GameOver -> gameOver = true
            FoodEaten -> {}
            SnakeMoved -> {}
        }
    }
}
