package fr.mwet.snake.backdrop

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import fr.mwet.snake.tuning.BackdropTuning.Ripples.COLOR
import com.badlogic.gdx.math.MathUtils
import fr.mwet.snake.assets.TextureHandler
import fr.mwet.snake.render.Background
import fr.mwet.snake.render.WorldDrawable
import fr.mwet.snake.tuning.GameplayTuning.WORLD_HEIGHT
import fr.mwet.snake.tuning.GameplayTuning.WORLD_WIDTH
import fr.mwet.snake.utils.hypotenuse
import fr.mwet.snake.utils.resetColor
import fr.mwet.snake.utils.setTint
import fr.mwet.snake.tuning.BackdropTuning.Ripples.INSET
import fr.mwet.snake.tuning.BackdropTuning.Ripples.MAX_ALPHA
import fr.mwet.snake.tuning.BackdropTuning.Ripples.MIN_VISIBLE_ALPHA
import fr.mwet.snake.tuning.BackdropTuning.Ripples.RING_FREQUENCY
import fr.mwet.snake.tuning.BackdropTuning.Ripples.RING_SPEED
import fr.mwet.snake.tuning.BackdropTuning.Ripples.SOURCE_SPEED

// The sources move on Lissajous curves around the centre, up to SOURCE_REACH_* of the board on each side.
// The frequencies differ between x and y, and between the two sources, so the paths do not repeat quickly.
private const val BOARD_CENTER = 0.5f
private const val SOURCE_REACH_X = 0.35f
private const val SOURCE_REACH_Y = 0.3f
private const val SOURCE_A_SPEED_Y = 1.3f
private const val SOURCE_B_SPEED_X = 0.8f
private const val SOURCE_B_SPEED_Y = 1.1f
private const val CELL_CENTER = 0.5f
// Each ring is a sine in [-1, 1], so the sum of two rings is in [-MAX_WAVE, MAX_WAVE].
private const val MAX_WAVE = 2f
private const val INSET_SIZE = 1f - 2 * INSET

// Rings spread from two sources that wander over the board. Cells light up where two crests meet.
class Ripples(textures: TextureHandler) : WorldDrawable {
    private val background = Background(textures)
    private val pixel = textures.rectangleTexture(1, 1, Color.WHITE)
    private var time = 0f

    override fun draw(batch: SpriteBatch, delta: Float) {
        time += delta
        background.draw(batch, delta)
        // Source b uses the opposite signs, so the two sources stay apart most of the time.
        val ax = WORLD_WIDTH * (BOARD_CENTER + SOURCE_REACH_X * MathUtils.sin(time * SOURCE_SPEED))
        val ay = WORLD_HEIGHT * (BOARD_CENTER + SOURCE_REACH_Y * MathUtils.cos(time * SOURCE_SPEED * SOURCE_A_SPEED_Y))
        val bx = WORLD_WIDTH * (BOARD_CENTER - SOURCE_REACH_X * MathUtils.sin(time * SOURCE_SPEED * SOURCE_B_SPEED_X))
        val by = WORLD_HEIGHT * (BOARD_CENTER - SOURCE_REACH_Y * MathUtils.cos(time * SOURCE_SPEED * SOURCE_B_SPEED_Y))
        for (x in 0 until WORLD_WIDTH.toInt()) {
            for (y in 0 until WORLD_HEIGHT.toInt()) {
                val cx = x + CELL_CENTER
                val cy = y + CELL_CENTER
                // Interference: the two rings add up.
                val wave = ring(cx - ax, cy - ay) + ring(cx - bx, cy - by)
                // [-MAX_WAVE, MAX_WAVE] -> [0, 1]
                val strength = (wave + MAX_WAVE) / (2 * MAX_WAVE)
                // strength^4 keeps only the peaks (0.5^4 = 0.06, 0.9^4 = 0.66): only the cells where two crests
                // meet stay bright.
                val contrast = strength * strength * strength * strength
                val alpha = MAX_ALPHA * contrast
                // Skipping the invisible cells removes most of the draw calls.
                if (alpha < MIN_VISIBLE_ALPHA) continue
                batch.setTint(COLOR, alpha)
                batch.draw(pixel, x + INSET, y + INSET, INSET_SIZE, INSET_SIZE)
            }
        }
        batch.resetColor()
    }

    // d is the distance to the source. At a fixed time, the crests are at a constant d: circles.
    // A given crest has d = (phase + time * RING_SPEED) / RING_FREQUENCY, which grows with time:
    // the rings spread outward at RING_SPEED / RING_FREQUENCY cells per second.
    private fun ring(dx: Float, dy: Float): Float =
        MathUtils.sin(hypotenuse(dx, dy) * RING_FREQUENCY - time * RING_SPEED)
}
