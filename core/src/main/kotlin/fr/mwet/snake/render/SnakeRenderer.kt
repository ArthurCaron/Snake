package fr.mwet.snake.render

import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion
import fr.mwet.snake.DI
import fr.mwet.snake.assets.TextureHandler
import fr.mwet.snake.entities.Direction
import fr.mwet.snake.entities.Segment
import fr.mwet.snake.entities.Snake

class SnakeRenderer(private val snake: Snake) {
    private val textureHandler = DI.inject<TextureHandler>()

    private var eTime = 0f
    private val originalAnimation =
        Animation(1f / 8f, textureHandler.snakeSegmentAnimation, Animation.PlayMode.LOOP_PINGPONG)
    private val animation =
        Animation(1f / 2f, textureHandler.snakeHeadAnimation, Animation.PlayMode.LOOP_PINGPONG)
    private val animationFlipped =
        Animation(1f / 2f, textureHandler.snakeHeadFlippedAnimation, Animation.PlayMode.LOOP_PINGPONG)

    fun render(batch: SpriteBatch, delta: Float) {
        eTime += delta

        if (snake.isDisintegrating) {
            playDisintegrateEffect(delta, batch)
        } else {
            var segment: Segment? = snake.tail
            while (segment != null) {
                when (segment) {
                    snake.tail -> drawSegment(batch, segment, textureHandler.snakeTail)
                    snake.head -> {}
                    else -> drawSegment(batch, segment, textureHandler.snakeBody)
                }
                segment = segment.next
            }

            if (snake.currentDirection == Direction.LEFT) {
                drawHead(batch, snake.head, animation)
            } else {
                drawHead(batch, snake.head, animationFlipped)
            }
        }
    }

    fun reset() {
        eTime = 0f
    }

    private fun drawHead(batch: SpriteBatch, segment: Segment, animation: Animation<AtlasRegion>) {
        batch.draw(
            animation.getKeyFrame(eTime),
            segment.x,
            segment.y,
            0.5f,
            0.5f,
            1f,
            1f,
            1f,
            1f,
            getAngleFromDirection(snake.currentDirection)
        )
    }

    private fun drawSegment(batch: SpriteBatch, segment: Segment, texture: AtlasRegion) {
        batch.draw(
            texture,
            segment.x,
            segment.y,
            0.5f,
            0.5f,
            1f,
            1f,
            1f,
            1f,
            segment.getAngleFromNext()
        )
    }

    private fun playDisintegrateEffect(delta: Float, batch: SpriteBatch) {
        var segment: Segment? = snake.tail
        while (segment != null) {
            segment.x += (segment.tx - segment.x) * delta * 2f
            segment.y += (segment.ty - segment.y) * delta * 2f
            batch.draw(
                originalAnimation.getKeyFrame(eTime),
                segment.x,
                segment.y,
                0.5f,
                0.5f,
                1f,
                1f,
                1f,
                1f,
                segment.angle
            )
            segment = segment.next
        }
    }

    fun Segment.getAngleFromNext(): Float {
        val direction = if (next == null) Direction.UP
        else if (ox < next!!.ox) Direction.RIGHT
        else if (ox > next!!.ox) Direction.LEFT
        else if (oy > next!!.oy) Direction.DOWN
        else if (oy < next!!.oy) Direction.UP
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
