package fr.mwet.snake.game

import com.badlogic.gdx.math.Vector2
import fr.mwet.snake.DI
import fr.mwet.snake.inputs.TargetActor
import fr.mwet.snake.utils.*
import ktx.assets.pool

private const val TICKS_PER_SECOND = 3f
private const val SECONDS_PER_TICK = 1f / TICKS_PER_SECOND

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
    private var secondsSinceLastTick: Float = 0f
    private var isEating = false

    fun reset() {
        head.position.move(initialHeadPosition)
        tail.position.move(initialTailPosition)
        freeBodyParts()
        tail.linkPrevious(head)
        currentDirection = initialDirection
        futureDirection = currentDirection
        secondsSinceLastTick = 0f
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

    fun update(delta: Float): Boolean {
        secondsSinceLastTick += delta
        if (secondsSinceLastTick < SECONDS_PER_TICK) return false

        secondsSinceLastTick %= SECONDS_PER_TICK
        move()
        return true
    }

    fun move() {
        currentDirection = futureDirection
        head.move(currentDirection)
        isEating = false
        tail.stayStill = false
    }

    override fun setDirection(newDirection: Direction) {
        if (newDirection.isOpposite(currentDirection)) return
        futureDirection = newDirection
    }

    fun eatFood() {
        if (isEating) return
        isEating = true
        tail.stayStill = true
        val newBodyPart = segmentPool.obtain(tail.position.copyVector())
        tail.previous?.let { newBodyPart.linkPrevious(it) }
        newBodyPart.linkNext(tail)
    }

    fun collidesWithBoundaries() = head.collidesWithBoundaries()
    fun collidesWithOwnBody() = head.collidesWithOwnBody()
    fun collidesWithFood(food: Food) = head.collidesWith(food.position)
    fun willCollideWithFood(food: Food): Boolean {
        val vector = head.position.copyVector().move(futureDirection)
        val doesCollide = vector.collidesWith(food.position)
        vector.free()
        return doesCollide
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

    fun collidesWith(otherPosition: Vector2): Boolean =
        position.x == otherPosition.x && position.y == otherPosition.y

    fun collidesWithBoundaries(): Boolean {
        if (position.x < 0) return true
        if (position.y < 0) return true
        if (position.x >= WORLD_WIDTH) return true
        if (position.y >= WORLD_HEIGHT) return true
        return false
    }

    fun collidesWithOwnBody(): Boolean {
        var bodyPart = previous
        while (bodyPart != null) {
            if (collidesWith(bodyPart.position)) return true
            bodyPart = bodyPart.previous
        }
        bodyPart = next
        while (bodyPart != null) {
            if (collidesWith(bodyPart.position)) return true
            bodyPart = bodyPart.next
        }
        return false
    }
}
