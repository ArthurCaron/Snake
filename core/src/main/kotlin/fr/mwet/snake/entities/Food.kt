package fr.mwet.snake.entities

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import fr.mwet.snake.DI
import fr.mwet.snake.assets.AssetHandler
import fr.mwet.snake.utils.WORLD_HEIGHT
import fr.mwet.snake.utils.WORLD_WIDTH
import fr.mwet.snake.utils.resetColor
import kotlin.random.Random

class Food {
    private val assetHandler = DI.inject<AssetHandler>()

    private var eTime = 0f
    private val animation = Animation(1f / 8f, assetHandler.food, Animation.PlayMode.LOOP_PINGPONG)
    var x = 0
    var y = 0
    var color: Color = FOOD_COLORS.random()

    fun render(batch: SpriteBatch, delta: Float) {
        eTime += delta
        batch.setColor(color)
        batch.draw(animation.getKeyFrame(eTime), x.toFloat(), y.toFloat(), 1f, 1f)
        batch.resetColor()
    }

    fun reset(snake: Snake) {
        eTime = 0f
        color = FOOD_COLORS.random()
        randomizePositionNotOnSnake(snake)
    }

    fun newFood(snake: Snake) {
        randomizePositionNotOnSnake(snake)
        val previousColor = color
        while (color == previousColor) {
            color = FOOD_COLORS.random()
        }
    }

    private fun randomizePositionNotOnSnake(snake: Snake) {
        randomizePosition()
        while (snake.collides(x, y)) {
            randomizePosition()
        }
    }

    private fun randomizePosition() {
        x = Random.nextInt(0, WORLD_WIDTH.toInt() - 1)
        y = Random.nextInt(0, WORLD_HEIGHT.toInt() - 1)
    }
}
