package fr.mwet.snake.render

import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import fr.mwet.snake.DI
import fr.mwet.snake.assets.TextureHandler
import fr.mwet.snake.entities.Food

class FoodRenderer(private val food: Food) {
    private val textureHandler = DI.inject<TextureHandler>()
    private var eTime = 0f
    private val animation = Animation(1f / 2f, textureHandler.strawberryAnimation, Animation.PlayMode.LOOP_PINGPONG)

    fun render(batch: SpriteBatch, delta: Float) {
        eTime += delta
        batch.draw(animation.getKeyFrame(eTime), food.x.toFloat(), food.y.toFloat(), 1f, 1f)
    }

    fun reset() {
        eTime = 0f
    }
}
