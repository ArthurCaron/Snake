package fr.mwet.snake.render

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
    private val animation = Animation(FOOD_FRAME_DURATION, textureHandler.game.skin.fruitBob, Animation.PlayMode.LOOP_PINGPONG)
    private val glow = textureHandler.game.skin.glow

    fun render(batch: SpriteBatch, delta: Float) {
        eTime += delta
        if (Palette.fruitGlow.a > 0f) drawGlow(batch)
        batch.draw(animation.getKeyFrame(eTime), food.position.x, food.position.y, 1f, 1f)
    }

    private fun drawGlow(batch: SpriteBatch) {
        val size = GLOW_SIZE * (1f + MathUtils.sin(eTime * FOOD_GLOW_PULSE_SPEED) * FOOD_GLOW_PULSE)
        batch.color = Palette.fruitGlow
        batch.additive {
            batch.draw(glow, food.position.x + (1f - size) / 2, food.position.y + (1f - size) / 2, size, size)
        }
        batch.resetColor()
    }

    fun reset() {
        eTime = 0f
    }
}
