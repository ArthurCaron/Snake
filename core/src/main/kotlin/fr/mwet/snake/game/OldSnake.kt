package fr.mwet.snake.game

import com.badlogic.gdx.utils.Pool
import fr.mwet.snake.events.GameEvent.SnakeMoved
import fr.mwet.snake.events.GameEventBus
import fr.mwet.snake.inputs.game.TargetActor
import fr.mwet.snake.utils.Direction
import fr.mwet.snake.utils.WORLD_HEIGHT
import fr.mwet.snake.utils.WORLD_WIDTH
import ktx.assets.pool
import kotlin.math.abs

private const val SNAKE_DEFAULT_SPEED = 6f

class OldSnake(val gameEventBus: GameEventBus) : TargetActor {
    private val segmentPool = pool { Segment() }

    lateinit var head: Segment
    lateinit var tail: Segment

    var currentDirection: Direction = Direction.UP
    private var nextX: Int = 0
    private var nextY: Int = 0
    private var currentSpeed = SNAKE_DEFAULT_SPEED

    fun reset() {
        currentDirection = Direction.UP
        currentSpeed = SNAKE_DEFAULT_SPEED

        if (this::head.isInitialized) {
            var segment: Segment? = tail
            while (segment != null) {
                val next = segment.next
                segmentPool.free(segment)
                segment = next
            }
        }

        head = segmentPool.obtain().apply { updatePosition(3, 3) }
        tail = segmentPool.obtain().apply { updatePosition(3, 2) }
        tail.next = head

        updateNext()
    }

    fun update(delta: Float) {
        var segment: Segment? = tail
        while (segment != null) {
            val next = segment.next
            if (next != null) {
                segment.x += (next.ox - segment.ox) * delta * currentSpeed
                segment.y += (next.oy - segment.oy) * delta * currentSpeed
            }
            segment = next
        }

        head.x += (nextX - head.ox) * delta * currentSpeed
        head.y += (nextY - head.oy) * delta * currentSpeed

        if (abs(nextX - head.x) < 0.15f && abs(nextY - head.y) < 0.15f) shiftSegments()
    }

    private fun shiftSegments() {
        gameEventBus.emit(SnakeMoved)

        var segment = tail
        while (segment != head) {
            val next = segment.next!!
            segment.x = next.ox.toFloat()
            segment.ox = next.ox
            segment.y = next.oy.toFloat()
            segment.oy = next.oy
            segment = next
        }
        head.x = nextX.toFloat()
        head.ox = nextX
        head.y = nextY.toFloat()
        head.oy = nextY
        updateNext()
    }

    private fun updateNext() {
        nextX = head.ox + currentDirection.directionX
        nextY = head.oy + currentDirection.directionY
    }

    override fun setDirection(newDirection: Direction) {
        if (newDirection.isOpposite(currentDirection)) return
        currentDirection = newDirection
    }

    fun setSpeed(speed: Float) {
        currentSpeed = speed
    }

    fun hasEatenFood(food: Food): Boolean {
        return abs(head.x - food.position.x) < 0.1f && abs(head.y - food.position.y) < 0.1f
    }

    fun ateFood() {
        updateNext()
        addSegment(nextX, nextY)
    }

    fun hitBoundariesTest(): Boolean {
        return head.x < (-1 + 0.15f) || head.x >= WORLD_WIDTH || head.y < (-1 + 0.15f) || head.y >= WORLD_HEIGHT
    }

    fun hitBodyTest(): Boolean {
        var segment = tail
        var index = 0
        while (segment != head) {
            if (abs(segment.x - head.x) < 0.15f && abs(segment.y - head.y) < 0.15f) {
                return true
            }
            segment = segment.next!!
            index++
        }
        return false
    }

    private fun addSegment(ox: Int, oy: Int) {
        val segment = segmentPool.obtain().apply { updatePosition(ox, oy) }
        head.next = segment
        head = segment
    }
}

data class Segment(
    var ox: Int = 0,
    var oy: Int = 0,
) : Pool.Poolable {
    var x: Float = ox.toFloat()
    var y: Float = oy.toFloat()
    var next: Segment? = null

    fun updatePosition(ox: Int, oy: Int) {
        this.ox = ox
        this.oy = oy
        x = ox.toFloat()
        y = oy.toFloat()
    }

    override fun reset() {
        ox = 0
        oy = 0
        x = 0f
        y = 0f
        next = null
    }
}
