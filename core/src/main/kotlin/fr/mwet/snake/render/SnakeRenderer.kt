package fr.mwet.snake.render

import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion
import fr.mwet.snake.assets.TextureHandler
import fr.mwet.snake.game.Segment
import fr.mwet.snake.game.Snake
import fr.mwet.snake.utils.Direction

class SnakeRenderer(textureHandler: TextureHandler, private val snake: Snake) {
    private val snakeTail = textureHandler.snakeTail
    private val snakeBody = textureHandler.snakeBody
    private val animation =
        Animation(1f / 2f, textureHandler.snakeHeadAnimation, Animation.PlayMode.LOOP_PINGPONG)
    private val animationFlipped =
        Animation(1f / 2f, textureHandler.snakeHeadFlippedAnimation, Animation.PlayMode.LOOP_PINGPONG)
    private val snekBodyStraight = textureHandler.snekBodyStraight
    private val snekBodyTurn = textureHandler.snekBodyTurn
    private val snekHead = textureHandler.snekHead
    private val snekHeadAll = textureHandler.snekHeadAll
    private val snekTail = textureHandler.snekTail
    private val snekGooglyAnimation = Animation(0.5f, textureHandler.snekGoogly, Animation.PlayMode.LOOP)

    private var eTime = 0f

    fun render(batch: SpriteBatch, delta: Float) {
        eTime += delta

        var segment: Segment? = snake.head
        while (segment != null) {
            when (segment) {
                snake.tail -> drawSegment(batch, segment, snekTail)
                snake.head -> {
                    if (snake.currentDirection == Direction.LEFT) {
                        drawHead(batch, segment, snekHeadAll)
                    } else {
                        drawHead(batch, segment, snekHeadAll)
                    }
                }

                else -> drawSegment(batch, segment, snekBodyStraight)
            }
            segment = segment.next
        }
    }

//    fun render(batch: SpriteBatch, delta: Float) {
//        eTime += delta
//
//        var segment: Segment? = snake.head
//        while (segment != null) {
//            when (segment) {
//                snake.tail -> drawSegment(batch, segment, snakeTail)
//                snake.head -> {
//                    if (snake.currentDirection == Direction.LEFT) {
//                        drawHead(batch, segment, animation)
//                    } else {
//                        drawHead(batch, segment, animationFlipped)
//                    }
//                }
//
//                else -> drawSegment(batch, segment, snakeBody)
//            }
//            segment = segment.next
//        }
//    }

    fun reset() {
        eTime = 0f
    }

    private fun drawHead(batch: SpriteBatch, segment: Segment, texture: AtlasRegion) {
        batch.draw(
            texture,
            segment.position.x,
            segment.position.y,
            0.5f,
            0.5f,
            1f,
            1f,
            1f,
            1f,
            getAngleFromDirection(snake.currentDirection)
        )
    }

    private fun drawHead(batch: SpriteBatch, segment: Segment, animation: Animation<AtlasRegion>) {
        batch.draw(
            animation.getKeyFrame(eTime),
            segment.position.x,
            segment.position.y,
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
            segment.position.x,
            segment.position.y,
            0.5f,
            0.5f,
            1f,
            1f,
            1f,
            1f,
            segment.getAngleFromPrevious()
        )
    }

    fun Segment.getAngleFromPrevious(): Float {
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
