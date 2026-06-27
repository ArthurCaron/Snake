package fr.mwet.snake.game

import com.badlogic.gdx.math.Vector2
import fr.mwet.snake.DI
import fr.mwet.snake.inputs.TargetActor
import fr.mwet.snake.utils.Direction
import fr.mwet.snake.utils.collidesWith
import fr.mwet.snake.utils.copyVector
import fr.mwet.snake.utils.move
import ktx.assets.pool

private val initialDirection = Direction.UP
private val initialTailPosition = DI.vectorPool.obtain(4f, 3f)
private val initialHeadPosition = DI.vectorPool.obtain(
    initialTailPosition.x + initialDirection.directionX,
    initialTailPosition.y + initialDirection.directionY
)

class Snake : TargetActor {
    private val segmentPool = SegmentPool()
    val head = segmentPool.obtain(initialHeadPosition.copyVector())
    val tail = segmentPool.obtain(initialTailPosition.copyVector()).apply { linkPrevious(head) }
    var currentDirection = initialDirection
    var futureDirection = currentDirection
    var nextHeadPosition = DI.vectorPool.obtain(-1, -1)

    fun reset() {
        head.position.move(initialHeadPosition)
        tail.position.move(initialTailPosition)
        freeBodyParts()
        tail.linkPrevious(head)
        tail.stayStill = false
        currentDirection = initialDirection
        futureDirection = currentDirection
        updateNextHeadPosition()
    }

    private fun freeBodyParts() {
        var bodyPart = head.next
        var next: Segment?
        while (bodyPart != null) {
            next = bodyPart.next
            if (bodyPart != tail) {
                segmentPool.free(bodyPart)
            }
            bodyPart = next
        }
    }

    fun move(eatFood: Boolean) {
        if (eatFood) {
            tail.stayStill = true
            val newBodyPart = segmentPool.obtain(tail.position.copyVector())
            tail.previous?.let { newBodyPart.linkPrevious(it) }
            newBodyPart.linkNext(tail)
        }

        currentDirection = futureDirection
        head.move(currentDirection)
        tail.stayStill = false
        updateNextHeadPosition()
    }

    override fun setDirection(newDirection: Direction) {
        if (newDirection.isOpposite(currentDirection)) return
        futureDirection = newDirection
        updateNextHeadPosition()
    }

    private fun updateNextHeadPosition() {
        nextHeadPosition.move(head.position).move(futureDirection)
    }

    fun wouldCollideWithBodyAfterMoving(willEatFood: Boolean): Boolean {
        var bodyPart: Segment? = head
        while (bodyPart != null) {
            if (bodyPart != tail || willEatFood) {
                if (nextHeadPosition.collidesWith(bodyPart.position)) return true
            }
            bodyPart = bodyPart.next
        }
        return false
    }
}

class SegmentPool {
    private val segmentPool = pool { Segment(DI.vectorPool.obtain(-1, -1)) }

    fun obtain(position: Vector2): Segment = segmentPool.obtain().apply { move(position) }
    fun free(segment: Segment) {
        segment.position.move(-1, -1)
        segment.next = null
        segment.previous = null
        segment.stayStill = false
        segmentPool.free(segment)
    }
}

data class Segment(var position: Vector2) {
    var next: Segment? = null
    var previous: Segment? = null
    var stayStill: Boolean = false

    fun linkNext(segment: Segment) {
        next = segment
        segment.previous = this
    }

    fun linkPrevious(segment: Segment) {
        previous = segment
        segment.next = this
    }

    fun move(direction: Direction) {
        if (stayStill) return
        next?.move(position)
        position.move(direction)
    }

    fun move(newPosition: Vector2) {
        if (stayStill) return
        next?.move(position)
        position.move(newPosition)
    }
}
