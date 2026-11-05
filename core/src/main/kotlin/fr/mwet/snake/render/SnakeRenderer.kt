package fr.mwet.snake.render

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion
import com.badlogic.gdx.math.MathUtils
import fr.mwet.snake.save.settings.SettingsRepository
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
import fr.mwet.snake.tuning.RenderTuning.Snake.GLOW_SIZE
import fr.mwet.snake.tuning.RenderTuning.Snake.HEAD_MARK_OFFSET
import fr.mwet.snake.tuning.RenderTuning.Snake.MARK_SIZE
import fr.mwet.snake.tuning.RenderTuning.Snake.PUPIL_SIZE
import fr.mwet.snake.tuning.RenderTuning.Snake.PUPIL_TRAVEL
import fr.mwet.snake.tuning.RenderTuning.Snake.SHADOW_X
import fr.mwet.snake.tuning.RenderTuning.Snake.SHADOW_Y
import fr.mwet.snake.tuning.RenderTuning.Snake.TAIL_MARK_OFFSET
import fr.mwet.snake.tuning.RenderTuning.Snake.TAIL_MARK_SCALE
import fr.mwet.snake.tuning.RenderTuning.Snake.TURN_MARK_OFFSET

class SnakeSprites(val head: AtlasRegion, val straight: AtlasRegion, val turn: AtlasRegion, val tail: AtlasRegion)

class SnakeRenderer(textureHandler: TextureHandler, private val snake: Snake, private val food: Food, private val settings: SettingsRepository) {
    private val skin = textureHandler.skin
    private val base = SnakeSprites(skin.snakeHead, skin.snakeBodyStraight, skin.snakeBodyTurn, skin.snakeTail)
    private val detail = SnakeSprites(skin.snakeHeadDetail, skin.snakeBodyStraightDetail, skin.snakeBodyTurnDetail, skin.snakeTailDetail)
    private val pupil = skin.snakePupil
    private val glow = skin.glow

    val colors = SnakeColors(settings)

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
        colors.update(delta)

        if (settings.appearance.snakeGlow) drawGlow(batch)
        batch.color = Palette.snakeShadow
        drawLayer(batch, base, SHADOW_X, SHADOW_Y, tinted = false)
        drawLayer(batch, base, 0f, 0f, tinted = true)
        drawMarks(batch)
        batch.resetColor()
        drawLayer(batch, detail, 0f, 0f, tinted = false)
        drawPupils(batch, snake.head, getAngleFromDirection(snake.currentDirection))
    }

    private fun drawGlow(batch: SpriteBatch) {
        batch.additive {
            var index = 0
            var segment: Segment? = snake.head
            while (segment != null) {
                val color = colors.at(index)
                batch.setColor(color.r, color.g, color.b, settings.appearance.theme.snakeGlowAlpha)
                batch.draw(glow, segment.position.x + (1f - GLOW_SIZE) / 2, segment.position.y + (1f - GLOW_SIZE) / 2, GLOW_SIZE, GLOW_SIZE)
                segment = segment.next
                index++
            }
        }
        batch.resetColor()
    }

    private fun drawLayer(batch: SpriteBatch, sprites: SnakeSprites, x: Float, y: Float, tinted: Boolean) {
        offsetX = x
        offsetY = y
        var index = 0
        var segment: Segment? = snake.head
        while (segment != null) {
            if (tinted) batch.color = colors.at(index)
            when (segment) {
                snake.tail -> drawSegment(batch, segment, sprites.tail)
                snake.head -> drawHead(batch, segment, sprites.head)
                else -> drawBody(batch, segment, sprites)
            }
            segment = segment.next
            index++
        }
    }

    private fun drawMarks(batch: SpriteBatch) {
        val mark = settings.appearance.markShape.region(skin) ?: return
        batch.color = settings.appearance.marks
        val head = snake.head
        val facing = snake.currentDirection
        drawMark(
            batch,
            mark,
            head.position.x + 0.5f - facing.directionX * HEAD_MARK_OFFSET,
            head.position.y + 0.5f - facing.directionY * HEAD_MARK_OFFSET,
            angleOf(facing.directionX.toFloat(), facing.directionY.toFloat()),
            1f,
        )
        var segment: Segment? = head.next
        while (segment != null) {
            val towardHead = segment.directionTo(segment.previous)
            val x = segment.position.x + 0.5f
            val y = segment.position.y + 0.5f
            if (segment == snake.tail) {
                drawMark(
                    batch,
                    mark,
                    x + towardHead.directionX * TAIL_MARK_OFFSET,
                    y + towardHead.directionY * TAIL_MARK_OFFSET,
                    angleOf(towardHead.directionX.toFloat(), towardHead.directionY.toFloat()),
                    TAIL_MARK_SCALE,
                )
            } else {
                val towardTail = segment.directionTo(segment.next)
                val turnX = if (towardHead.isOpposite(towardTail)) 0f else (towardHead.directionX + towardTail.directionX) * TURN_MARK_OFFSET
                val turnY = if (towardHead.isOpposite(towardTail)) 0f else (towardHead.directionY + towardTail.directionY) * TURN_MARK_OFFSET
                val angle = angleOf(
                    (towardHead.directionX - towardTail.directionX).toFloat(),
                    (towardHead.directionY - towardTail.directionY).toFloat(),
                )
                drawMark(batch, mark, x + turnX, y + turnY, angle, 1f)
            }
            segment = segment.next
        }
    }

    private fun drawMark(batch: SpriteBatch, region: AtlasRegion, x: Float, y: Float, angle: Float, scale: Float) {
        val half = MARK_SIZE / 2
        batch.draw(region, x - half, y - half, half, half, MARK_SIZE, MARK_SIZE, scale, scale, angle)
    }

    private fun angleOf(x: Float, y: Float): Float = MathUtils.atan2Deg360(y, x)

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

    private fun drawBody(batch: SpriteBatch, segment: Segment, sprites: SnakeSprites) {
        val frontSegmentDirection = segment.directionTo(segment.previous)
        val backSegmentDirection = segment.directionTo(segment.next)
        if (frontSegmentDirection.isOpposite(backSegmentDirection)) {
            drawSegment(batch, segment, sprites.straight)
        } else {
            drawRotated(batch, segment, sprites.turn, turnAngle(frontSegmentDirection, backSegmentDirection))
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
        colors.reset()
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
