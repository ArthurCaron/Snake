package fr.mwet.snake.backdrop

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import fr.mwet.snake.assets.TextureHandler
import fr.mwet.snake.render.Background
import fr.mwet.snake.render.WorldDrawable
import fr.mwet.snake.tuning.BackdropTuning.DiagonalStripes.BAND
import fr.mwet.snake.tuning.BackdropTuning.DiagonalStripes.CELLS_PER_SECOND
import fr.mwet.snake.tuning.BackdropTuning.DiagonalStripes.COLOR
import fr.mwet.snake.tuning.BackdropTuning.DiagonalStripes.SPACING
import fr.mwet.snake.tuning.GameplayTuning.WORLD_HEIGHT
import fr.mwet.snake.tuning.GameplayTuning.WORLD_WIDTH
import fr.mwet.snake.utils.resetColor
import kotlin.math.sqrt

// Each band is the 1x1 pixel stretched to BAND_LENGTH x BAND, then rotated by ANGLE_DEGREES around its
// bottom-left corner.
private const val ANGLE_DEGREES = 45f
private val SQRT_2 = sqrt(2f)
// One band more than the screen needs, for the slide.
private const val EXTRA_BAND = 1
private const val PIXEL_SIZE = 1
private const val UNSCALED = 1f
// After the rotation, the short side BAND points up and to the left, along (-1, 1) / sqrt(2).
// Its x and y components are both BAND / sqrt(2).
private val PERPENDICULAR_COMPONENT = BAND / SQRT_2
// Width of a band, measured along a horizontal line.
private val HORIZONTAL_THICKNESS = 2 * PERPENDICULAR_COMPONENT
// A 45 degree band that starts at x reaches x + H at the top, so the bands must cover W + H along x.
private val BAND_COUNT = ((WORLD_WIDTH + WORLD_HEIGHT + HORIZONTAL_THICKNESS) / SPACING).toInt() + EXTRA_BAND
// Puts the upper corner of the short side at y = 0: no gap at the bottom edge of the screen.
private val START_Y = -PERPENDICULAR_COMPONENT
// Far enough to the left that the top end of the first band is off screen.
private val FIRST_X = -WORLD_HEIGHT - PERPENDICULAR_COMPONENT
// The diagonal of an H x H square crosses the full height at 45 degrees. BAND keeps the square ends
// of the band off screen.
private val BAND_LENGTH = WORLD_HEIGHT * SQRT_2 + BAND

// Faint bands at 45 degrees slide slowly to the right.
class DiagonalStripes(textures: TextureHandler, private val background: Background) : WorldDrawable {
    private val pixel = textures.rectangleTexture(PIXEL_SIZE, PIXEL_SIZE, Color.WHITE)
    private var time = 0f

    override fun draw(batch: SpriteBatch, delta: Float) {
        time += delta
        background.draw(batch, delta)
        // After a shift of SPACING, each band is where the next one was: the offset loops in [0, SPACING).
        val offset = (time * CELLS_PER_SECOND) % SPACING
        batch.color = COLOR
        for (i in 0 until BAND_COUNT) {
            val startX = FIRST_X + i * SPACING + offset
            batch.draw(pixel, startX, START_Y, 0f, 0f, BAND_LENGTH, BAND, UNSCALED, UNSCALED, ANGLE_DEGREES, 0, 0, PIXEL_SIZE, PIXEL_SIZE, false, false)
        }
        batch.resetColor()
    }
}
