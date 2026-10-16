package fr.mwet.snake.backdrop

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.MathUtils
import fr.mwet.snake.assets.TextureHandler
import fr.mwet.snake.render.Background
import fr.mwet.snake.render.WorldDrawable
import fr.mwet.snake.tuning.BackdropTuning.GameOfLife.COLOR
import fr.mwet.snake.tuning.BackdropTuning.GameOfLife.FADE_PER_SECOND
import fr.mwet.snake.tuning.BackdropTuning.GameOfLife.INSET
import fr.mwet.snake.tuning.BackdropTuning.GameOfLife.MAX_ALPHA
import fr.mwet.snake.tuning.BackdropTuning.GameOfLife.MAX_GENERATIONS
import fr.mwet.snake.tuning.BackdropTuning.GameOfLife.MIN_BRIGHTNESS
import fr.mwet.snake.tuning.BackdropTuning.GameOfLife.MIN_POPULATION
import fr.mwet.snake.tuning.BackdropTuning.GameOfLife.SEED_DENSITY
import fr.mwet.snake.tuning.BackdropTuning.GameOfLife.STEP_SECONDS
import fr.mwet.snake.tuning.GameplayTuning.WORLD_HEIGHT
import fr.mwet.snake.tuning.GameplayTuning.WORLD_WIDTH
import fr.mwet.snake.utils.resetColor
import fr.mwet.snake.utils.setTint

private const val COLUMNS = WORLD_WIDTH.toInt()
private const val ROWS = WORLD_HEIGHT.toInt()
private const val ALIVE_BRIGHTNESS = 1f
private const val DEAD_BRIGHTNESS = 0f
// Fraction of the gap closed in one frame, clamped so that a long frame cannot overshoot the target.
private const val MAX_FADE_STEP = 1f
private const val INSET_SIZE = 1f - 2 * INSET
// A dead cell with exactly BIRTH_NEIGHBOURS is born. A live cell with SURVIVAL_NEIGHBOURS or
// BIRTH_NEIGHBOURS survives. Every other cell dies.
private const val BIRTH_NEIGHBOURS = 3
private const val SURVIVAL_NEIGHBOURS = 2

// Conway's Game of Life on the board, with wrapped edges. Cells fade in and out, and the grid reseeds
// when it dies down.
class GameOfLife(textures: TextureHandler, private val background: Background) : WorldDrawable {
    private val pixel = textures.rectangleTexture(1, 1, Color.WHITE)
    // Flat grid: the cell (x, y) is at y * COLUMNS + x. next receives the next generation.
    private var cells = BooleanArray(COLUMNS * ROWS)
    private var next = BooleanArray(COLUMNS * ROWS)
    // Displayed value of each cell. It follows cells with a delay.
    private val brightness = FloatArray(COLUMNS * ROWS)
    private var sinceStep = 0f
    private var generation = 0

    init {
        seed()
    }

    override fun draw(batch: SpriteBatch, delta: Float) {
        sinceStep += delta
        if (sinceStep >= STEP_SECONDS) {
            sinceStep -= STEP_SECONDS
            step()
        }
        background.draw(batch, delta)
        for (i in cells.indices) {
            val target = if (cells[i]) ALIVE_BRIGHTNESS else DEAD_BRIGHTNESS
            // Exponential approach: each frame closes a fraction FADE_PER_SECOND * delta of the gap to the
            // target, so the cells fade in and out instead of popping.
            brightness[i] += (target - brightness[i]) * minOf(MAX_FADE_STEP, FADE_PER_SECOND * delta)
            if (brightness[i] < MIN_BRIGHTNESS) continue
            batch.setTint(COLOR, MAX_ALPHA * brightness[i])
            batch.draw(pixel, i % COLUMNS + INSET, i / COLUMNS + INSET, INSET_SIZE, INSET_SIZE)
        }
        batch.resetColor()
    }

    private fun seed() {
        for (i in cells.indices) cells[i] = MathUtils.random() < SEED_DENSITY
        generation = 0
    }

    private fun step() {
        var population = 0
        for (y in 0 until ROWS) {
            for (x in 0 until COLUMNS) {
                val neighbours = neighbours(x, y)
                val alive = cells[y * COLUMNS + x]
                // BIRTH_NEIGHBOURS also covers the survival with 3 neighbours.
                val nextAlive = neighbours == BIRTH_NEIGHBOURS || (alive && neighbours == SURVIVAL_NEIGHBOURS)
                // Written into next, never into cells: every cell reads its neighbours from the same generation.
                next[y * COLUMNS + x] = nextAlive
                if (nextAlive) population++
            }
        }
        // Double buffer: next becomes the current generation, the old one is reused as the next buffer.
        val swap = cells
        cells = next
        next = swap
        generation++
        // Reseed when the grid dies down, or after MAX_GENERATIONS so that stable patterns and oscillators
        // do not stay forever.
        if (population < MIN_POPULATION || generation > MAX_GENERATIONS) seed()
    }

    private fun neighbours(x: Int, y: Int): Int {
        var count = 0
        for (dy in -1..1) {
            for (dx in -1..1) {
                if (dx == 0 && dy == 0) continue
                // The edges wrap: the grid is a torus. + COLUMNS keeps the index positive at the left edge,
                // % COLUMNS wraps it at the right edge.
                val nx = (x + dx + COLUMNS) % COLUMNS
                val ny = (y + dy + ROWS) % ROWS
                if (cells[ny * COLUMNS + nx]) count++
            }
        }
        return count
    }
}
