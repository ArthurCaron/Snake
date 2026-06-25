package fr.mwet.snake.game

import fr.mwet.snake.DI
import fr.mwet.snake.events.GameEvent
import fr.mwet.snake.events.GameEvent.*
import fr.mwet.snake.events.GameEventBus
import fr.mwet.snake.events.GameEventListener
import fr.mwet.snake.utils.Direction

class GameWorld(private val gameEventBus: GameEventBus) : GameEventListener {
    val food: Food = Food(DI.vectorPool.obtain(-1, -1))
    val snake = Snake(DI.vectorPool.obtain(4f, 3f), Direction.UP)
    val cells = Cells()

    var gameOver = false

    fun newGame() {
        gameOver = false
        snake.reset()
        val availableCells = cells.computeAvailableCells(snake)
        food.reset(availableCells)
    }

    fun update(delta: Float) {
        if (gameOver) return

        val snakeMoved = snake.update(delta)
        if (snakeMoved) gameEventBus.emit(SnakeMoved)
        if (snake.collidesWithBoundaries() || snake.collidesWithOwnBody()) gameEventBus.emit(GameOver)
        if (snake.willCollideWithFood(food)) snake.eatFood()
        if (snake.collidesWithFood(food)) {
            gameEventBus.emit(FoodEaten)
            val availableCells = cells.computeAvailableCells(snake)
            food.reset(availableCells)
        }
    }

    override fun onEvent(event: GameEvent) {
        when (event) {
            GameOver -> gameOver = true
            FoodEaten -> {}
            SnakeMoved -> {}
            GoBackToMainMenu -> {}
            Pause -> {}
        }
    }
}
