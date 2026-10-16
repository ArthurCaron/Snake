package fr.mwet.snake.render

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.MathUtils
import fr.mwet.snake.assets.TextureHandler
import fr.mwet.snake.game.Food
import fr.mwet.snake.tuning.AnimationTuning.FOOD_FRAME_DURATION
import fr.mwet.snake.tuning.Palette
import fr.mwet.snake.utils.resetColor
import fr.mwet.snake.tuning.AnimationTuning.FOOD_GLOW_PULSE
import fr.mwet.snake.tuning.AnimationTuning.FOOD_GLOW_PULSE_SPEED
import fr.mwet.snake.tuning.RenderTuning.Food.GLOW_SIZE

class FoodRenderer(textureHandler: TextureHandler, private val food: Food) {
    private var eTime = 0f
    private val base = Animation(FOOD_FRAME_DURATION, textureHandler.game.skin.fruitBob, Animation.PlayMode.LOOP_PINGPONG)
    private val detail = Animation(FOOD_FRAME_DURATION, textureHandler.game.skin.fruitBobDetail, Animation.PlayMode.LOOP_PINGPONG)
    private val glow = textureHandler.game.skin.glow
    private var colorIndex = 0

    val color: Color get() = Palette.fruits[colorIndex]

    fun render(batch: SpriteBatch, delta: Float) {
        eTime += delta
        if (Palette.fruitGlow > 0f) drawGlow(batch)
        batch.color = color
        batch.draw(base.getKeyFrame(eTime), food.position.x, food.position.y, 1f, 1f)
        batch.resetColor()
        batch.draw(detail.getKeyFrame(eTime), food.position.x, food.position.y, 1f, 1f)
    }

    fun nextColor() {
        if (Palette.fruits.size < 2) return
        colorIndex = (colorIndex + MathUtils.random(1, Palette.fruits.size - 1)) % Palette.fruits.size
    }

    private fun drawGlow(batch: SpriteBatch) {
        val size = GLOW_SIZE * (1f + MathUtils.sin(eTime * FOOD_GLOW_PULSE_SPEED) * FOOD_GLOW_PULSE)
        batch.setColor(color.r, color.g, color.b, Palette.fruitGlow)
        batch.additive {
            batch.draw(glow, food.position.x + (1f - size) / 2, food.position.y + (1f - size) / 2, size, size)
        }
        batch.resetColor()
    }

    fun reset() {
        eTime = 0f
        colorIndex = 0
    }
}
