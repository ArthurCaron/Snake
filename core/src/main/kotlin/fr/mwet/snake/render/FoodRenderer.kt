package fr.mwet.snake.render

import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import fr.mwet.snake.assets.TextureHandler
import fr.mwet.snake.game.Food
import fr.mwet.snake.tuning.AnimationTuning.FOOD_FRAME_DURATION

class FoodRenderer(textureHandler: TextureHandler, private val food: Food) {
    private var eTime = 0f
    private val animation = Animation(FOOD_FRAME_DURATION, textureHandler.strawberryAnimation, Animation.PlayMode.LOOP_PINGPONG)

    fun render(batch: SpriteBatch, delta: Float) {
        eTime += delta
        batch.draw(animation.getKeyFrame(eTime), food.position.x, food.position.y, 1f, 1f)
    }

    fun reset() {
        eTime = 0f
    }
}
