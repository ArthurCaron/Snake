package fr.mwet.snake.render

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion
import com.badlogic.gdx.math.MathUtils
import fr.mwet.snake.assets.TextureHandler
import fr.mwet.snake.game.Food
import fr.mwet.snake.tuning.AnimationTuning.FOOD_FRAME_DURATION
import fr.mwet.snake.tuning.Palette
import fr.mwet.snake.utils.resetColor
import fr.mwet.snake.tuning.AnimationTuning.FOOD_GLOW_PULSE
import fr.mwet.snake.tuning.AnimationTuning.FOOD_GLOW_PULSE_SPEED
import fr.mwet.snake.tuning.RenderTuning.Food.GLOW_SIZE

private class FruitAnimation(sprites: FruitSprites) {
    val base = Animation<AtlasRegion>(FOOD_FRAME_DURATION, sprites.bob, Animation.PlayMode.LOOP_PINGPONG)
    val detail = Animation<AtlasRegion>(FOOD_FRAME_DURATION, sprites.bobDetail, Animation.PlayMode.LOOP_PINGPONG)
}

class FoodRenderer(textureHandler: TextureHandler, private val food: Food) {
    private var eTime = 0f
    private val animations = FruitLook.fruits.map { FruitAnimation(it.kind.sprites(textureHandler.game.skin)) }
    private val glow = textureHandler.game.skin.glow
    private var fruitIndex = 0

    val color: Color get() = FruitLook.fruits[fruitIndex].color

    fun render(batch: SpriteBatch, delta: Float) {
        eTime += delta
        if (FruitLook.glow > 0f) drawGlow(batch)
        val animation = animations[fruitIndex]
        batch.color = color
        batch.draw(animation.base.getKeyFrame(eTime), food.position.x, food.position.y, 1f, 1f)
        batch.resetColor()
        batch.draw(animation.detail.getKeyFrame(eTime), food.position.x, food.position.y, 1f, 1f)
    }

    fun nextFruit() {
        if (FruitLook.fruits.size < 2) return
        fruitIndex = (fruitIndex + MathUtils.random(1, FruitLook.fruits.size - 1)) % FruitLook.fruits.size
    }

    private fun drawGlow(batch: SpriteBatch) {
        val size = GLOW_SIZE * (1f + MathUtils.sin(eTime * FOOD_GLOW_PULSE_SPEED) * FOOD_GLOW_PULSE)
        batch.setColor(color.r, color.g, color.b, FruitLook.glow)
        batch.additive {
            batch.draw(glow, food.position.x + (1f - size) / 2, food.position.y + (1f - size) / 2, size, size)
        }
        batch.resetColor()
    }

    fun reset() {
        eTime = 0f
        fruitIndex = MathUtils.random(FruitLook.fruits.size - 1)
    }
}
