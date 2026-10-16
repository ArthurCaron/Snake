package fr.mwet.snake.backdrop

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.MathUtils
import fr.mwet.snake.assets.TextureHandler
import fr.mwet.snake.render.Background
import fr.mwet.snake.render.WorldDrawable
import fr.mwet.snake.tuning.BackdropTuning.Fireflies.COLOR
import fr.mwet.snake.tuning.BackdropTuning.Fireflies.COUNT
import fr.mwet.snake.tuning.BackdropTuning.Fireflies.MAX_ALPHA
import fr.mwet.snake.tuning.BackdropTuning.Fireflies.MAX_BLINK_SPEED
import fr.mwet.snake.tuning.BackdropTuning.Fireflies.MAX_DRIFT
import fr.mwet.snake.tuning.BackdropTuning.Fireflies.MIN_BLINK_SPEED
import fr.mwet.snake.tuning.BackdropTuning.Fireflies.SIZE
import fr.mwet.snake.tuning.GameplayTuning.WORLD_HEIGHT
import fr.mwet.snake.tuning.GameplayTuning.WORLD_WIDTH
import fr.mwet.snake.utils.resetColor
import fr.mwet.snake.utils.setTint

private const val HALF_SIZE = SIZE / 2

// Small squares drift over the board and blink slowly, each one at its own pace.
class Fireflies(textures: TextureHandler, private val background: Background) : WorldDrawable {
    private val pixel = textures.rectangleTexture(1, 1, Color.WHITE)
    // Each firefly gets a random position, velocity, blink speed and phase once, in parallel arrays:
    // nothing is allocated per frame.
    private val x = FloatArray(COUNT) { MathUtils.random(0f, WORLD_WIDTH) }
    private val y = FloatArray(COUNT) { MathUtils.random(0f, WORLD_HEIGHT) }
    private val vx = FloatArray(COUNT) { MathUtils.random(-MAX_DRIFT, MAX_DRIFT) }
    private val vy = FloatArray(COUNT) { MathUtils.random(-MAX_DRIFT, MAX_DRIFT) }
    private val blink = FloatArray(COUNT) { MathUtils.random(MIN_BLINK_SPEED, MAX_BLINK_SPEED) }
    private val phase = FloatArray(COUNT) { MathUtils.random(MathUtils.PI2) }
    private var time = 0f

    override fun draw(batch: SpriteBatch, delta: Float) {
        time += delta
        background.draw(batch, delta)
        for (i in 0 until COUNT) {
            // Move by v * delta. wrap() brings a firefly that leaves one edge back at the opposite edge.
            x[i] = wrap(x[i] + vx[i] * delta, WORLD_WIDTH)
            y[i] = wrap(y[i] + vy[i] * delta, WORLD_HEIGHT)
            // In [0, 1]. The random phase keeps the fireflies out of sync.
            val glow = unitSin(time * blink[i] + phase[i])
            // Squaring keeps the peak at 1 and lowers the rest (0.5^2 = 0.25): the firefly stays dim most of
            // the time and flashes briefly.
            val sharpGlow = glow * glow
            batch.setTint(COLOR, MAX_ALPHA * sharpGlow)
            // Centred on (x, y).
            batch.draw(pixel, x[i] - HALF_SIZE, y[i] - HALF_SIZE, SIZE, SIZE)
        }
        batch.resetColor()
    }
}
