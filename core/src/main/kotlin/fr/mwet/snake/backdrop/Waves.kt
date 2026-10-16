package fr.mwet.snake.backdrop

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import fr.mwet.snake.tuning.BackdropTuning.Waves.COLOR
import com.badlogic.gdx.math.MathUtils
import fr.mwet.snake.assets.TextureHandler
import fr.mwet.snake.render.Background
import fr.mwet.snake.render.WorldDrawable
import fr.mwet.snake.tuning.GameplayTuning.WORLD_HEIGHT
import fr.mwet.snake.tuning.GameplayTuning.WORLD_WIDTH
import fr.mwet.snake.utils.resetColor
import fr.mwet.snake.utils.setTint
import fr.mwet.snake.tuning.BackdropTuning.Waves.MAX_ALPHA
import fr.mwet.snake.tuning.BackdropTuning.Waves.ROWS_PER_WAVE
import fr.mwet.snake.tuning.BackdropTuning.Waves.SPEED

private const val ROW_HEIGHT = 1f

// Horizontal bands of light travel slowly up the board, one bright band every ROWS_PER_WAVE rows.
class Waves(textures: TextureHandler) : WorldDrawable {
    private val background = Background(textures)
    private val pixel = textures.rectangleTexture(1, 1, Color.WHITE)
    private var time = 0f

    override fun draw(batch: SpriteBatch, delta: Float) {
        time += delta
        background.draw(batch, delta)
        for (y in 0 until WORLD_HEIGHT.toInt()) {
            // Travelling wave, phase = time * SPEED - y * 2 * PI / ROWS_PER_WAVE.
            // At a fixed time, the phase drops by 2 * PI every ROWS_PER_WAVE rows: one bright band per period.
            // A given phase is at y = (time * SPEED - phase) * ROWS_PER_WAVE / (2 * PI). This y grows with time:
            // the bands move up at SPEED * ROWS_PER_WAVE / (2 * PI) rows per second.
            val wave = unitSin(time * SPEED - y * MathUtils.PI2 / ROWS_PER_WAVE)
            // Squaring makes the bright part of each band narrower.
            val sharpWave = wave * wave
            batch.setTint(COLOR, MAX_ALPHA * sharpWave)
            // One full-width band per row.
            batch.draw(pixel, 0f, y.toFloat(), WORLD_WIDTH, ROW_HEIGHT)
        }
        batch.resetColor()
    }
}
