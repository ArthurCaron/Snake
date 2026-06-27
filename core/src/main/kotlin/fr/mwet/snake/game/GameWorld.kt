package fr.mwet.snake.game

import com.badlogic.gdx.math.Vector2
import fr.mwet.snake.events.GameEvent
import fr.mwet.snake.events.GameEvent.*
import fr.mwet.snake.events.GameEventBus
import fr.mwet.snake.events.GameEventListener
import fr.mwet.snake.utils.WORLD_HEIGHT
import fr.mwet.snake.utils.WORLD_WIDTH

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
        if (ticker.update(delta) == UpdateResult.KeepWaiting) return

        val willEatFood = food.isAt(snake.nextHeadPosition)
        val willBeOutsideWorld = isOutsideWorld(snake.nextHeadPosition)
        val willCollideWithBody = snake.wouldCollideWithBodyAfterMoving(willEatFood = willEatFood)

        if (willBeOutsideWorld || willCollideWithBody) {
            gameEventBus.emit(Lost)
            return
        }

        snake.move(eatFood = willEatFood)
        gameEventBus.emit(SnakeMoved)

        if (willEatFood) {
            gameEventBus.emit(FoodEaten)
            resetFood()
        }
    }

    private fun isOutsideWorld(position: Vector2): Boolean {
        if (position.x < 0) return true
        if (position.y < 0) return true
        if (position.x >= WORLD_WIDTH) return true
        if (position.y >= WORLD_HEIGHT) return true
        return false
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
