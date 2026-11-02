package fr.mwet.snake.backdrop

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.MathUtils
import fr.mwet.snake.assets.TextureHandler
import fr.mwet.snake.render.Background
import fr.mwet.snake.render.WorldDrawable
import fr.mwet.snake.tuning.BackdropTuning.WanderingSnakes.COLOR
import fr.mwet.snake.tuning.BackdropTuning.WanderingSnakes.HEAD_ALPHA
import fr.mwet.snake.tuning.BackdropTuning.WanderingSnakes.INSET
import fr.mwet.snake.tuning.BackdropTuning.WanderingSnakes.MAX_LENGTH
import fr.mwet.snake.tuning.BackdropTuning.WanderingSnakes.MAX_STEP_SECONDS
import fr.mwet.snake.tuning.BackdropTuning.WanderingSnakes.MIN_LENGTH
import fr.mwet.snake.tuning.BackdropTuning.WanderingSnakes.MIN_STEP_SECONDS
import fr.mwet.snake.tuning.BackdropTuning.WanderingSnakes.SNAKE_COUNT
import fr.mwet.snake.tuning.BackdropTuning.WanderingSnakes.TURN_CHANCE
import fr.mwet.snake.tuning.GameplayTuning.WORLD_HEIGHT
import fr.mwet.snake.tuning.GameplayTuning.WORLD_WIDTH
import fr.mwet.snake.utils.Direction
import fr.mwet.snake.utils.resetColor
import fr.mwet.snake.utils.setTint

private const val COLUMNS = WORLD_WIDTH.toInt()
private const val ROWS = WORLD_HEIGHT.toInt()
private const val INSET_SIZE = 1f - 2 * INSET

// Short snakes move cell by cell, turn at random and wrap around the edges.
class WanderingSnakes(textures: TextureHandler) : WorldDrawable {
    private val background = Background(textures)
    private val pixel = textures.rectangleTexture(1, 1, Color.WHITE)
    // Each body is a ring buffer of MAX_LENGTH cells, and head is the index of the head. A move writes the
    // new head over the oldest cell, so nothing moves in memory.
    private val xs = Array(SNAKE_COUNT) { IntArray(MAX_LENGTH) }
    private val ys = Array(SNAKE_COUNT) { IntArray(MAX_LENGTH) }
    private val length = IntArray(SNAKE_COUNT) { MathUtils.random(MIN_LENGTH, MAX_LENGTH) }
    private val head = IntArray(SNAKE_COUNT)
    private val direction = Array(SNAKE_COUNT) { Direction.entries.random() }
    // Each snake moves at its own pace.
    private val stepSeconds = FloatArray(SNAKE_COUNT) { MathUtils.random(MIN_STEP_SECONDS, MAX_STEP_SECONDS) }
    private val sinceStep = FloatArray(SNAKE_COUNT)

    init {
        for (s in 0 until SNAKE_COUNT) {
            val x = MathUtils.random(COLUMNS - 1)
            val y = MathUtils.random(ROWS - 1)
            // Every cell of the buffer holds the start position: the snake unrolls from one point.
            xs[s].fill(x)
            ys[s].fill(y)
        }
    }

    override fun draw(batch: SpriteBatch, delta: Float) {
        background.draw(batch, delta)
        for (s in 0 until SNAKE_COUNT) {
            sinceStep[s] += delta
            if (sinceStep[s] >= stepSeconds[s]) {
                sinceStep[s] -= stepSeconds[s]
                move(s)
            }
            for (k in 0 until length[s]) {
                // k cells behind the head, wrapped back to the end of the ring buffer.
                val index = (head[s] - k + MAX_LENGTH) % MAX_LENGTH
                // Linear fade from HEAD_ALPHA at the head toward 0 at the tail.
                val fade = 1f - k.toFloat() / length[s]
                batch.setTint(COLOR, HEAD_ALPHA * fade)
                batch.draw(pixel, xs[s][index] + INSET, ys[s][index] + INSET, INSET_SIZE, INSET_SIZE)
            }
        }
        batch.resetColor()
    }

    private fun move(s: Int) {
        // With the probability TURN_CHANCE, draw a direction and keep it unless it is a U-turn. The draw can
        // return the current direction, so the real turn rate is about TURN_CHANCE / 2.
        if (MathUtils.random() < TURN_CHANCE) {
            val turned = Direction.entries[MathUtils.random(Direction.entries.size - 1)]
            if (!turned.isOpposite(direction[s])) direction[s] = turned
        }
        val current = head[s]
        // The new head overwrites the oldest cell.
        val nextIndex = (current + 1) % MAX_LENGTH
        // + COLUMNS keeps the value positive at the left edge, % COLUMNS wraps it at the right edge.
        xs[s][nextIndex] = (xs[s][current] + direction[s].directionX + COLUMNS) % COLUMNS
        ys[s][nextIndex] = (ys[s][current] + direction[s].directionY + ROWS) % ROWS
        head[s] = nextIndex
    }
}
