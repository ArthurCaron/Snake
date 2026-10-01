package fr.mwet.snake.render

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion
import fr.mwet.snake.assets.TextureHandler
import fr.mwet.snake.game.Segment
import fr.mwet.snake.game.Snake
import fr.mwet.snake.utils.Direction
import fr.mwet.snake.utils.Direction.*

class SnakeRenderer(textureHandler: TextureHandler, private val snake: Snake) {
    private val snekBodyStraight = textureHandler.snekBodyStraight
    private val snekBodyTurn = textureHandler.snekBodyTurn
    private val snekHeadAll = textureHandler.snekHeadAll
    private val snekTail = textureHandler.snekTail

    private class DirectionPair(val first: Direction, val second: Direction) {
        fun matches(first: Direction, second: Direction): Boolean =
            (this.first == first && this.second == second) ||
                (this.first == second && this.second == first)
    }

    private val rightDown = DirectionPair(RIGHT, DOWN)
    private val upRight = DirectionPair(UP, RIGHT)
    private val leftUp = DirectionPair(LEFT, UP)

    private var eTime = 0f

    fun render(batch: SpriteBatch, delta: Float) {
        eTime += delta

        var segment: Segment? = snake.head
        while (segment != null) {
            when (segment) {
                snake.tail -> drawSegment(batch, segment, snekTail)
                snake.head -> drawHead(batch, segment, snekHeadAll)
                else -> drawBody(batch, segment)
            }
            segment = segment.next
        }
    }

    private fun drawBody(batch: SpriteBatch, segment: Segment) {
        val frontSegmentDirection = segment.directionTo(segment.previous)
        val backSegmentDirection = segment.directionTo(segment.next)
        if (frontSegmentDirection.isOpposite(backSegmentDirection)) {
            drawSegment(batch, segment, snekBodyStraight)
        } else {
            drawRotated(batch, segment, snekBodyTurn, turnAngle(frontSegmentDirection, backSegmentDirection))
        }
    }

    private fun turnAngle(direction: Direction, otherDirection: Direction): Float = when {
        rightDown.matches(direction, otherDirection) -> 0f
        upRight.matches(direction, otherDirection) -> 90f
        leftUp.matches(direction, otherDirection) -> 180f
        else -> 270f
    }

    private fun drawRotated(batch: SpriteBatch, segment: Segment, texture: AtlasRegion, angle: Float) {
        batch.draw(texture, segment.position.x, segment.position.y, 0.5f, 0.5f, 1f, 1f, 1f, 1f, angle)
    }

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
        val direction = if (previous == null) UP
        else if (position.x < previous!!.position.x) RIGHT
        else if (position.x > previous!!.position.x) LEFT
        else if (position.y > previous!!.position.y) DOWN
        else if (position.y < previous!!.position.y) UP
        else /* WTF */ UP

        return getAngleFromDirection(direction)
    }

    fun getAngleFromDirection(direction: Direction): Float {
        return when (direction) {
            UP -> 270f
            RIGHT -> 180f
            DOWN -> 90f
            LEFT -> 0f
        }
    }
}
