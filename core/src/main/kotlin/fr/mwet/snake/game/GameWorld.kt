package fr.mwet.snake.game

import fr.mwet.snake.events.GameEvent
import fr.mwet.snake.events.GameEvent.*
import fr.mwet.snake.events.GameEventBus
import fr.mwet.snake.events.GameEventListener
import fr.mwet.snake.utils.free

class GameWorld(private val gameEventBus: GameEventBus) : GameEventListener {
    val ticker = Ticker()
    val cells = Cells()
    val snake = Snake()
    val food = Food()

    var gameStopped = false

    fun newGame() {
        gameStopped = false
        ticker.reset()
        snake.reset()
        resetFood()
    }

    fun update(delta: Float) {
        if (gameStopped) return
        if (!ticker.update(delta)) return

        snake.move()
        gameEventBus.emit(SnakeMoved)

        if (snake.collidesWithBoundaries() || snake.collidesWithOwnBody()) {
            gameEventBus.emit(Lost)
            return
        }

        val nextHeadPosition = snake.computeNextHeadPosition()
        val willEat = food.isAt(nextHeadPosition)
        nextHeadPosition.free()
        if (willEat) snake.eatFood()

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
            Lost, Won -> gameStopped = true
            FoodEaten, SnakeMoved, GoBackToMainMenu, Pause -> {}
        }
    }
}

class Ticker {
    private val ticksPerSecond = 5f
    private val secondsPerTick = 1f / ticksPerSecond
    private var secondsSinceLastTick: Float = 0f

    fun reset() {
        secondsSinceLastTick = 0f
    }

    fun update(delta: Float): Boolean {
        secondsSinceLastTick += delta
        if (secondsSinceLastTick < secondsPerTick) return false

        secondsSinceLastTick -= secondsPerTick
        return true
    }
}
