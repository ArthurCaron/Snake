package fr.mwet.snake.render

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion
import com.badlogic.gdx.math.Vector2
import fr.mwet.snake.assets.TextureHandler
import fr.mwet.snake.game.Segment
import fr.mwet.snake.game.Snake
import fr.mwet.snake.utils.WORLD_HEIGHT
import fr.mwet.snake.utils.WORLD_WIDTH
import fr.mwet.snake.utils.VectorPool
import kotlin.random.Random

class DisintegratingSnakeRenderer(textureHandler: TextureHandler, val snake: Snake) {
    private var eTime = 0f
    private val vectors = VectorPool()

    private val snekHeadAll = textureHandler.snekHeadAll
    private val snakeBody = textureHandler.snekBodyStraight
    private val snakeTail = textureHandler.snekTail
    private val bodyParts = mutableListOf<DisintegratingSegment>()

    fun disintegrate() {
        bodyParts.add(DisintegratingSegment(vectors.copy(snake.head.position), snekHeadAll))
        bodyParts.add(DisintegratingSegment(vectors.copy(snake.tail.position), snakeTail))

        var segment: Segment? = snake.head.next
        while (segment != null) {
            if (segment != snake.tail) {
                bodyParts.add(DisintegratingSegment(vectors.copy(segment.position), snakeBody))
            }
            segment = segment.next
        }
    }

    fun render(batch: SpriteBatch, delta: Float) {
        eTime += delta

        bodyParts.forEach {
            it.position.x += (it.tx - it.position.x) * delta * 2f
            it.position.y += (it.ty - it.position.y) * delta * 2f

            batch.draw(
                it.texture,
                it.position.x,
                it.position.y,
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
        bodyParts.forEach { vectors.free(it.position) }
        bodyParts.clear()
    }
}

data class DisintegratingSegment(var position: Vector2, val texture: AtlasRegion) {
    val tx: Float = Random.nextInt(0, WORLD_WIDTH.toInt() - 1).toFloat()
    val ty: Float = Random.nextInt(0, WORLD_HEIGHT.toInt() - 1).toFloat()
    val angle: Float = Random.nextInt(0, 360).toFloat()
}
