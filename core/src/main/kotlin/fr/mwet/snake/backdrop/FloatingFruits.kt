package fr.mwet.snake.backdrop

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.MathUtils
import fr.mwet.snake.assets.TextureHandler
import fr.mwet.snake.render.Background
import fr.mwet.snake.render.WorldDrawable
import fr.mwet.snake.tuning.BackdropTuning.FloatingFruits.COLOR
import fr.mwet.snake.tuning.BackdropTuning.FloatingFruits.FRUIT_COUNT
import fr.mwet.snake.tuning.BackdropTuning.FloatingFruits.MAX_SIZE
import fr.mwet.snake.tuning.BackdropTuning.FloatingFruits.MAX_SPEED
import fr.mwet.snake.tuning.BackdropTuning.FloatingFruits.MIN_SIZE
import fr.mwet.snake.tuning.BackdropTuning.FloatingFruits.MIN_SPEED
import fr.mwet.snake.tuning.BackdropTuning.FloatingFruits.ROTATION_DEGREES
import fr.mwet.snake.tuning.BackdropTuning.FloatingFruits.ROTATION_SPEED
import fr.mwet.snake.tuning.BackdropTuning.FloatingFruits.SWAY
import fr.mwet.snake.tuning.GameplayTuning.WORLD_HEIGHT
import fr.mwet.snake.tuning.GameplayTuning.WORLD_WIDTH
import fr.mwet.snake.utils.resetColor
import kotlin.math.sqrt

// The start y is random in [START_MIN_Y, WORLD_HEIGHT]: the screen is already full on the first frame.
private const val START_MIN_Y = -1f
// At any rotation, a fruit stays inside the circle around its centre whose diameter is its diagonal,
// size * sqrt(2). One diagonal of the biggest fruit past an edge is therefore always off screen.
private val MAX_DIAGONAL = MAX_SIZE * sqrt(2f)
// Fully above the top and fully below the bottom: a fruit never appears or disappears on screen.
private val EXIT_Y = WORLD_HEIGHT + MAX_DIAGONAL
private val RESPAWN_Y = -MAX_DIAGONAL
private const val UNSCALED = 1f

// Strawberries rise slowly, sway and tilt, and come back from the bottom when they leave the top.
class FloatingFruits(textures: TextureHandler, private val background: Background) : WorldDrawable {
    private val fruit = textures.game.skin.fruit
    private val x = FloatArray(FRUIT_COUNT) { MathUtils.random(0f, WORLD_WIDTH) }
    private val y = FloatArray(FRUIT_COUNT) { MathUtils.random(START_MIN_Y, WORLD_HEIGHT) }
    private val speed = FloatArray(FRUIT_COUNT) { MathUtils.random(MIN_SPEED, MAX_SPEED) }
    private val phase = FloatArray(FRUIT_COUNT) { MathUtils.random(MathUtils.PI2) }
    private val size = FloatArray(FRUIT_COUNT) { MathUtils.random(MIN_SIZE, MAX_SIZE) }
    private var time = 0f

    override fun draw(batch: SpriteBatch, delta: Float) {
        time += delta
        background.draw(batch, delta)
        batch.color = COLOR
        for (i in 0 until FRUIT_COUNT) {
            y[i] += speed[i] * delta
            // Out at the top: back below the bottom, at a new random x.
            if (y[i] > EXIT_Y) {
                y[i] = RESPAWN_Y
                x[i] = MathUtils.random(0f, WORLD_WIDTH)
            }
            // Sway and tilt are sines of time, shifted by the phase of the fruit. They apply only at draw time:
            // x keeps the rest position, and the fruit oscillates around it.
            val sway = MathUtils.sin(time + phase[i]) * SWAY
            val rotation = MathUtils.sin(time * ROTATION_SPEED + phase[i]) * ROTATION_DEGREES
            // Rotate around the centre of the sprite.
            val pivot = size[i] / 2
            batch.draw(fruit, x[i] + sway, y[i], pivot, pivot, size[i], size[i], UNSCALED, UNSCALED, rotation)
        }
        batch.resetColor()
    }
}
