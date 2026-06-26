package fr.mwet.snake.game

import fr.mwet.snake.events.GameEvent
import fr.mwet.snake.events.GameEvent.*
import fr.mwet.snake.events.GameEventBus
import fr.mwet.snake.events.GameEventListener

class GameWorld(private val gameEventBus: GameEventBus) : GameEventListener {
    val food: Food = Food()
    val snake = Snake()
    val cells = Cells()

    var lost = false
    var won = false

    fun newGame() {
        lost = false
        won = false
        snake.reset()
        resetFood()
    }

    fun update(delta: Float) {
        if (lost) return
        if (won) return

        val snakeMoved = snake.update(delta)
        if (snake.collidesWithBoundaries() || snake.collidesWithOwnBody()) {
            gameEventBus.emit(Lost)
            return
        }

        if (snakeMoved) gameEventBus.emit(SnakeMoved)

        if (snake.willCollideWithFood(food)) snake.eatFood()

        if (snake.collidesWithFood(food)) {
            gameEventBus.emit(FoodEaten)
            resetFood()
        }
    }

    private fun resetFood() {
        val newCell = cells.randomAvailableCell(snake)
        if (newCell == null) {
            gameEventBus.emit(Won)
        } else {
            food.move(newCell.position)
        }
    }

    override fun onEvent(event: GameEvent) {
        when (event) {
            Lost -> lost = true
            Won -> won = true
            FoodEaten, SnakeMoved, GoBackToMainMenu, Pause -> {}
        }
    }
}
