package fr.mwet.snake.render

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion
import com.badlogic.gdx.math.MathUtils
import fr.mwet.snake.assets.TextureHandler
import fr.mwet.snake.game.Food
import fr.mwet.snake.game.Segment
import fr.mwet.snake.game.Snake
import fr.mwet.snake.tuning.Palette
import fr.mwet.snake.utils.Direction
import fr.mwet.snake.utils.Direction.*
import fr.mwet.snake.utils.hypotenuse
import fr.mwet.snake.utils.resetColor
import fr.mwet.snake.utils.rotatedX
import fr.mwet.snake.utils.rotatedY
import fr.mwet.snake.tuning.RenderTuning.Snake.EYE_FORWARD
import fr.mwet.snake.tuning.RenderTuning.Snake.EYE_SIDE
import fr.mwet.snake.tuning.RenderTuning.Snake.PUPIL_SIZE
import fr.mwet.snake.tuning.RenderTuning.Snake.PUPIL_TRAVEL
import fr.mwet.snake.tuning.RenderTuning.Snake.SHADOW_X
import fr.mwet.snake.tuning.RenderTuning.Snake.SHADOW_Y

class SnakeRenderer(textureHandler: TextureHandler, private val snake: Snake, private val food: Food) {
    private val snekBodyStraight = textureHandler.game.skin.snakeBodyStraight
    private val snekBodyTurn = textureHandler.game.skin.snakeBodyTurn
    private val snekHeadAll = textureHandler.game.skin.snakeHead
    private val snekTail = textureHandler.game.skin.snakeTail

    private val pupil = textureHandler.game.skin.snakePupil

    private class DirectionPair(val first: Direction, val second: Direction) {
        fun matches(first: Direction, second: Direction): Boolean =
            (this.first == first && this.second == second) ||
                (this.first == second && this.second == first)
    }

    private val rightDown = DirectionPair(RIGHT, DOWN)
    private val upRight = DirectionPair(UP, RIGHT)
    private val leftUp = DirectionPair(LEFT, UP)

    private var eTime = 0f
    private var offsetX = 0f
    private var offsetY = 0f

    fun render(batch: SpriteBatch, delta: Float) {
        eTime += delta

        batch.color = Palette.snakeShadow
        drawSnake(batch, SHADOW_X, SHADOW_Y)
        batch.resetColor()
        drawSnake(batch, 0f, 0f)
        drawPupils(batch, snake.head, getAngleFromDirection(snake.currentDirection))
    }

    private fun drawPupils(batch: SpriteBatch, head: Segment, angle: Float) {
        val cos = MathUtils.cosDeg(angle)
        val sin = MathUtils.sinDeg(angle)
        val centerX = head.position.x + 0.5f
        val centerY = head.position.y + 0.5f
        drawPupil(batch, centerX + rotatedX(EYE_FORWARD, EYE_SIDE, cos, sin), centerY + rotatedY(EYE_FORWARD, EYE_SIDE, cos, sin))
        drawPupil(batch, centerX + rotatedX(EYE_FORWARD, -EYE_SIDE, cos, sin), centerY + rotatedY(EYE_FORWARD, -EYE_SIDE, cos, sin))
    }

    private fun drawPupil(batch: SpriteBatch, eyeX: Float, eyeY: Float) {
        val dx = food.position.x + 0.5f - eyeX
        val dy = food.position.y + 0.5f - eyeY
        val distance = hypotenuse(dx, dy)
        val directionX = dx / distance
        val directionY = dy / distance
        batch.draw(pupil, eyeX + directionX * PUPIL_TRAVEL - PUPIL_SIZE / 2, eyeY + directionY * PUPIL_TRAVEL - PUPIL_SIZE / 2, PUPIL_SIZE, PUPIL_SIZE)
    }

    private fun drawSnake(batch: SpriteBatch, x: Float, y: Float) {
        offsetX = x
        offsetY = y
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
        batch.draw(texture, segment.position.x + offsetX, segment.position.y + offsetY, 0.5f, 0.5f, 1f, 1f, 1f, 1f, angle)
    }

    fun reset() {
        eTime = 0f
    }

    private fun drawHead(batch: SpriteBatch, segment: Segment, texture: AtlasRegion) {
        batch.draw(
            texture,
            segment.position.x + offsetX,
            segment.position.y + offsetY,
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
            segment.position.x + offsetX,
            segment.position.y + offsetY,
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
