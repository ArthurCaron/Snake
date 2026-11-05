package fr.mwet.snake.backdrop

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.MathUtils
import fr.mwet.snake.assets.TextureHandler
import fr.mwet.snake.render.Background
import fr.mwet.snake.render.WorldDrawable
import fr.mwet.snake.tuning.BackdropTuning.DriftingGrid.CELLS_PER_SECOND
import fr.mwet.snake.tuning.BackdropTuning.DriftingGrid.COLOR
import fr.mwet.snake.tuning.BackdropTuning.DriftingGrid.MAX_ALPHA
import fr.mwet.snake.tuning.BackdropTuning.DriftingGrid.MIN_ALPHA
import fr.mwet.snake.tuning.BackdropTuning.DriftingGrid.PULSE_SECONDS
import fr.mwet.snake.tuning.GameplayTuning.WORLD_HEIGHT
import fr.mwet.snake.tuning.GameplayTuning.WORLD_WIDTH
import fr.mwet.snake.utils.resetColor
import fr.mwet.snake.utils.setTint

private const val CELL_SIZE = 1f
private const val PATTERN_PERIOD = CELL_SIZE
// One extra cell on each side fills the strip that the drift uncovers at the left and top edges.
private const val MARGIN_CELLS = 1

// The board grid drifts down and to the right while its opacity pulses slowly.
class DriftingGrid(textures: TextureHandler, private val background: Background) : WorldDrawable {
    private val cell = textures.skin.gridCell
    private var time = 0f

    override fun draw(batch: SpriteBatch, delta: Float) {
        time += delta
        background.draw(batch, delta)
        // The grid looks the same after a shift of one whole cell, so the offset loops in [0, PATTERN_PERIOD)
        // with no visible jump.
        val offset = (time * CELLS_PER_SECOND) % PATTERN_PERIOD
        // time / PULSE_SECONDS turns, times 2 * PI radians: pulse goes from 0 to 1 and back once every
        // PULSE_SECONDS.
        val pulse = unitSin(time * MathUtils.PI2 / PULSE_SECONDS)
        // Linear mix: MIN_ALPHA when pulse = 0, MAX_ALPHA when pulse = 1.
        batch.setTint(COLOR, MIN_ALPHA + (MAX_ALPHA - MIN_ALPHA) * pulse)
        for (x in -MARGIN_CELLS..WORLD_WIDTH.toInt()) {
            for (y in -MARGIN_CELLS..WORLD_HEIGHT.toInt()) {
                // Every cell moves by (+offset, -offset): to the right and down.
                batch.draw(cell, x + offset, y - offset, CELL_SIZE, CELL_SIZE)
            }
        }
        batch.resetColor()
    }
}
