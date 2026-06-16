package fr.mwet.snake.render

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion
import fr.mwet.snake.DI
import fr.mwet.snake.assets.TextureHandler
import fr.mwet.snake.game.Segment
import fr.mwet.snake.game.Snake
import fr.mwet.snake.utils.WORLD_WIDTH
import kotlin.random.Random

class DisintegratingSnakeRenderer(val snake: Snake) {
    private val textureHandler = DI.inject<TextureHandler>()
    private var eTime = 0f
    private val bodyParts = mutableListOf<DisintegratingSegment>()

    fun disintegrate(snake: Snake) {
        bodyParts.add(DisintegratingSegment(snake.head.x, snake.head.y, textureHandler.snakeHead))
        bodyParts.add(DisintegratingSegment(snake.tail.x, snake.tail.y, textureHandler.snakeTail))

        var segment: Segment? = snake.tail.next
        while (segment != null) {
            val next = segment.next
            if (next != null) {
                bodyParts.add(DisintegratingSegment(segment.x, segment.y, textureHandler.snakeBody))
            }
            segment = next
        }
    }

    fun render(batch: SpriteBatch, delta: Float) {
        eTime += delta

        bodyParts.forEach {
            it.x += (it.tx - it.x) * delta * 2f
            it.y += (it.ty - it.y) * delta * 2f

            batch.draw(
                it.texture,
                it.x,
                it.y,
                0.5f,
                0.5f,
                1f,
                1f,
                1f,
                1f,
                it.angle
            )
        }
    }

    fun reset() {
        eTime = 0f
        bodyParts.clear()
    }
}

data class DisintegratingSegment(var x: Float, var y: Float, val texture: AtlasRegion) {
    val tx: Float = Random.nextInt(0, WORLD_WIDTH.toInt() - 1).toFloat()
    val ty: Float = Random.nextInt(0, WORLD_WIDTH.toInt() - 1).toFloat()
    val angle: Float = Random.nextInt(0, 360).toFloat()
}
