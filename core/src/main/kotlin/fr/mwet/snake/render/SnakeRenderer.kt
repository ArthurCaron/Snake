package fr.mwet.snake.render

import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion
import fr.mwet.snake.assets.TextureHandler
import fr.mwet.snake.game.Snake
import fr.mwet.snake.game.SnakeSegment
import fr.mwet.snake.utils.Direction

class SnakeRenderer(textureHandler: TextureHandler, private val snake: Snake) {
    private val snakeTail = textureHandler.snakeTail
    private val snakeBody = textureHandler.snakeBody
    private val animation =
        Animation(1f / 2f, textureHandler.snakeHeadAnimation, Animation.PlayMode.LOOP_PINGPONG)
    private val animationFlipped =
        Animation(1f / 2f, textureHandler.snakeHeadFlippedAnimation, Animation.PlayMode.LOOP_PINGPONG)

    private var eTime = 0f

    fun render(batch: SpriteBatch, delta: Float) {
        eTime += delta

        var snakeSegment: SnakeSegment? = snake.head
        while (snakeSegment != null) {
            when (snakeSegment) {
                snake.tail -> drawSegment(batch, snakeSegment, snakeTail)
                snake.head -> {
                    if (snake.currentDirection == Direction.LEFT) {
                        drawHead(batch, snake.head, animation)
                    } else {
                        drawHead(batch, snake.head, animationFlipped)
                    }
                }

                else -> drawSegment(batch, snakeSegment, snakeBody)
            }
            snakeSegment = snakeSegment.next
        }
    }

    fun reset() {
        eTime = 0f
    }

    private fun drawHead(batch: SpriteBatch, snakeSegment: SnakeSegment, animation: Animation<AtlasRegion>) {
        batch.draw(
            animation.getKeyFrame(eTime),
            snakeSegment.position.x,
            snakeSegment.position.y,
            0.5f,
            0.5f,
            1f,
            1f,
            1f,
            1f,
            getAngleFromDirection(snake.currentDirection)
        )
    }

    private fun drawSegment(batch: SpriteBatch, snakeSegment: SnakeSegment, texture: AtlasRegion) {
        batch.draw(
            texture,
            snakeSegment.position.x,
            snakeSegment.position.y,
            0.5f,
            0.5f,
            1f,
            1f,
            1f,
            1f,
            snakeSegment.getAngleFromPrevious()
        )
    }

    fun SnakeSegment.getAngleFromPrevious(): Float {
        val direction = if (previous == null) Direction.UP
        else if (position.x < previous!!.position.x) Direction.RIGHT
        else if (position.x > previous!!.position.x) Direction.LEFT
        else if (position.y > previous!!.position.y) Direction.DOWN
        else if (position.y < previous!!.position.y) Direction.UP
        else /* WTF */ Direction.UP

        return getAngleFromDirection(direction)
    }

    fun getAngleFromDirection(direction: Direction): Float {
        return when (direction) {
            Direction.UP -> 270f
            Direction.RIGHT -> 180f
            Direction.DOWN -> 90f
            Direction.LEFT -> 0f
        }
    }
}
