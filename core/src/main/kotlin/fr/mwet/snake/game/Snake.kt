package fr.mwet.snake.game

import com.badlogic.gdx.math.Vector2
import fr.mwet.snake.inputs.TargetActor
import fr.mwet.snake.tuning.GameplayTuning.START_DIRECTION
import fr.mwet.snake.tuning.GameplayTuning.START_TAIL_X
import fr.mwet.snake.tuning.GameplayTuning.START_TAIL_Y
import fr.mwet.snake.tuning.InputTuning.MAX_PENDING_DIRECTIONS
import fr.mwet.snake.utils.Direction
import fr.mwet.snake.utils.collidesWith
import fr.mwet.snake.utils.move
import ktx.assets.pool

private val initialDirection = START_DIRECTION
private val initialTailPosition = Vector2(START_TAIL_X, START_TAIL_Y)
private val initialHeadPosition = Vector2(
    initialTailPosition.x + initialDirection.directionX,
    initialTailPosition.y + initialDirection.directionY
)

class Snake : TargetActor {
    private val segmentPool = SegmentPool()
    val head = segmentPool.obtain(initialHeadPosition)
    val tail = segmentPool.obtain(initialTailPosition).apply { linkPrevious(head) }
    var currentDirection = initialDirection
    private val pendingDirections = ArrayDeque<Direction>(MAX_PENDING_DIRECTIONS)
    val futureDirection: Direction get() = pendingDirections.firstOrNull() ?: currentDirection
    val nextHeadPosition = Vector2(-1f, -1f)

    fun reset() {
        head.position.move(initialHeadPosition)
        tail.position.move(initialTailPosition)
        freeBodyParts()
        tail.linkPrevious(head)
        tail.stayStill = false
        currentDirection = initialDirection
        pendingDirections.clear()
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
            val newBodyPart = segmentPool.obtain(tail.position)
            tail.previous?.let { newBodyPart.linkPrevious(it) }
            newBodyPart.linkNext(tail)
        }

        currentDirection = pendingDirections.removeFirstOrNull() ?: currentDirection
        head.move(currentDirection)
        tail.stayStill = false
        updateNextHeadPosition()
    }

    override fun setDirection(newDirection: Direction) {
        if (pendingDirections.size == MAX_PENDING_DIRECTIONS) return
        val lastDirection = pendingDirections.lastOrNull() ?: currentDirection
        if (newDirection == lastDirection || newDirection.isOpposite(lastDirection)) return
        pendingDirections.addLast(newDirection)
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
    private val segmentPool = pool { Segment(Vector2(-1f, -1f)) }

    fun obtain(position: Vector2): Segment = segmentPool.obtain().apply { move(position) }
    fun free(segment: Segment) {
        segment.position.move(-1, -1)
        segment.next = null
        segment.previous = null
        segment.stayStill = false
        segmentPool.free(segment)
    }
}

class Segment(var position: Vector2) {
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

    fun directionTo(neighbour: Segment?): Direction = when {
        neighbour == null -> Direction.UP
        neighbour.position.x > position.x -> Direction.RIGHT
        neighbour.position.x < position.x -> Direction.LEFT
        neighbour.position.y > position.y -> Direction.UP
        else -> Direction.DOWN
    }
}
