package fr.mwet.snake.backdrop

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector2
import fr.mwet.snake.assets.TextureHandler
import fr.mwet.snake.events.GameEventBusImpl
import fr.mwet.snake.game.GameWorld
import fr.mwet.snake.game.Segment
import fr.mwet.snake.render.BoardRenderer
import fr.mwet.snake.tuning.BackdropTuning.DemoSnake.DIM_COLOR
import fr.mwet.snake.render.WorldDrawable
import fr.mwet.snake.tuning.GameplayTuning.WORLD_HEIGHT
import fr.mwet.snake.tuning.GameplayTuning.WORLD_WIDTH
import fr.mwet.snake.utils.Direction
import fr.mwet.snake.utils.resetColor
import kotlin.math.abs
import fr.mwet.snake.tuning.BackdropTuning.DemoSnake.RESTART_DELAY

private const val NO_HEAD = -1f

// A real GameWorld plays by itself behind the menu, steered by a greedy bot, and restarts after each game.
class DemoSnake(textures: TextureHandler) : WorldDrawable {
    // Its own event bus, with no listener: no sound, no score, nothing outside this class reacts.
    private val world = GameWorld(GameEventBusImpl())
    private val board = BoardRenderer(textures, world)
    private val pixel = textures.rectangleTexture(1, 1, Color.WHITE)
    // The head position at the last decision. NO_HEAD forces a decision on the first frame.
    private val lastHead = Vector2(NO_HEAD, NO_HEAD)
    private var overSince = 0f

    init {
        restart()
    }

    override fun draw(batch: SpriteBatch, delta: Float) {
        // After a loss or a win, let the end animation play for RESTART_DELAY seconds.
        if (world.isOver) {
            overSince += delta
            if (overSince > RESTART_DELAY) restart()
        } else if (world.snake.head.position != lastHead) {
            // Decide once per cell, when the head reaches a new cell. The first decision also starts the game:
            // GameWorld.setDirection starts it.
            lastHead.set(world.snake.head.position)
            world.setDirection(bestDirection())
        }
        world.update(delta)

        board.draw(batch, delta)
        // Dim the board with the background colour, partly opaque.
        batch.color = DIM_COLOR
        batch.draw(pixel, 0f, 0f, WORLD_WIDTH, WORLD_HEIGHT)
        batch.resetColor()
    }

    private fun restart() {
        world.newGame()
        board.reset()
        lastHead.set(NO_HEAD, NO_HEAD)
        overSince = 0f
    }

    private fun bestDirection(): Direction {
        val snake = world.snake
        val head = snake.head.position
        val food = world.food.position
        // If no cell is free, keep the direction: the snake dies.
        var best = snake.currentDirection
        var bestDistance = Float.MAX_VALUE
        // Greedy: among the free cells next to the head, go to the one nearest to the food. The bot looks only
        // one cell ahead, so it traps itself sometimes: that shows the disintegration.
        for (direction in Direction.entries) {
            if (direction.isOpposite(snake.currentDirection)) continue
            val x = head.x + direction.directionX
            val y = head.y + direction.directionY
            if (!isFree(x, y)) continue
            // Manhattan distance: the snake moves only along the axes.
            val distance = abs(food.x - x) + abs(food.y - y)
            if (distance < bestDistance) {
                bestDistance = distance
                best = direction
            }
        }
        return best
    }

    private fun isFree(x: Float, y: Float): Boolean {
        if (x < 0 || y < 0 || x >= WORLD_WIDTH || y >= WORLD_HEIGHT) return false
        var segment: Segment? = world.snake.head
        while (segment != null) {
            // The tail moves away during the next move, so its cell counts as free.
            if (segment !== world.snake.tail && segment.position.x == x && segment.position.y == y) return false
            segment = segment.next
        }
        return true
    }
}
