package fr.mwet.snake.render

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion
import com.badlogic.gdx.math.Vector2
import fr.mwet.snake.DI
import fr.mwet.snake.assets.TextureHandler
import fr.mwet.snake.game.Snake
import fr.mwet.snake.game.SnakeSegment
import fr.mwet.snake.utils.WORLD_HEIGHT
import fr.mwet.snake.utils.WORLD_WIDTH
import fr.mwet.snake.utils.copyVector
import kotlin.random.Random

class DisintegratingSnakeRenderer(val snake: Snake) {
    private val textureHandler = DI.inject<TextureHandler>()
    private var eTime = 0f
    private val bodyParts = mutableListOf<DisintegratingSnakeSegment>()

    fun disintegrate() {
        bodyParts.add(DisintegratingSnakeSegment(snake.head.position.copyVector(), textureHandler.snakeHead))
        bodyParts.add(DisintegratingSnakeSegment(snake.tail.position.copyVector(), textureHandler.snakeTail))

        var snakeSegment: SnakeSegment? = snake.head.next
        while (snakeSegment != null) {
            if (snakeSegment != snake.tail) {
                bodyParts.add(DisintegratingSnakeSegment(snakeSegment.position.copyVector(), textureHandler.snakeBody))
            }
            snakeSegment = snakeSegment.next
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
        bodyParts.clear()
    }
}

data class DisintegratingSnakeSegment(var position: Vector2, val texture: AtlasRegion) {
    val tx: Float = Random.nextInt(0, WORLD_WIDTH.toInt() - 1).toFloat()
    val ty: Float = Random.nextInt(0, WORLD_HEIGHT.toInt() - 1).toFloat()
    val angle: Float = Random.nextInt(0, 360).toFloat()
}
