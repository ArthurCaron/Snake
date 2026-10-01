package fr.mwet.snake.game

import com.badlogic.gdx.math.Vector2
import fr.mwet.snake.events.GameEvent.*
import fr.mwet.snake.events.GameEventBus
import fr.mwet.snake.inputs.TargetActor
import fr.mwet.snake.utils.Direction
import fr.mwet.snake.utils.WORLD_HEIGHT
import fr.mwet.snake.utils.WORLD_WIDTH

private const val START_TICKS_PER_SECOND = 5f
private const val TICKS_PER_SECOND_GAINED_PER_FOOD = 0.15f
private const val MAX_TICKS_PER_SECOND = 12f

class GameWorld(private val gameEventBus: GameEventBus) : TargetActor {
    val ticker = Ticker(START_TICKS_PER_SECOND)
    val cells = Cells()
    val snake = Snake()
    val food = Food()

    var state = GameState.Ready
        private set

    var score = 0
        private set

    val isOver get() = state == GameState.Lost || state == GameState.Won

    fun newGame() {
        state = GameState.Ready
        score = 0
        ticker.reset()
        snake.reset()
        resetFood()
    }

    fun start() {
        if (state != GameState.Ready) return
        state = GameState.Playing
        ticker.reset()
    }

    fun pause() {
        if (state != GameState.Playing && state != GameState.Ready) return
        state = GameState.Paused
    }

    fun resume() {
        if (state != GameState.Paused) return
        state = GameState.Ready
    }

    override fun setDirection(newDirection: Direction) {
        if (state != GameState.Ready && state != GameState.Playing) return
        snake.setDirection(newDirection)
        start()
    }

    fun update(delta: Float) {
        if (state != GameState.Playing) return
        if (ticker.update(delta) == UpdateResult.KeepWaiting) return

        val willEatFood = food.isAt(snake.nextHeadPosition)
        val willBeOutsideWorld = isOutsideWorld(snake.nextHeadPosition)
        val willCollideWithBody = snake.wouldCollideWithBodyAfterMoving(willEatFood = willEatFood)

        if (willBeOutsideWorld || willCollideWithBody) {
            state = GameState.Lost
            gameEventBus.emit(Lost)
            return
        }

        snake.move(eatFood = willEatFood)
        gameEventBus.emit(SnakeMoved)

        if (willEatFood) {
            score++
            gameEventBus.emit(FoodEaten)
            ticker.speedUp(TICKS_PER_SECOND_GAINED_PER_FOOD, MAX_TICKS_PER_SECOND)
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
            state = GameState.Won
            gameEventBus.emit(Won)
        } else {
            food.move(newCell.position)
        }
    }
}

enum class GameState {
    Ready,
    Playing,
    Paused,
    Lost,
    Won,
}
