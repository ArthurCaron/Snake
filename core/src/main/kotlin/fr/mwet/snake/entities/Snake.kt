package fr.mwet.snake.entities

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.Pool
import com.badlogic.gdx.utils.Timer
import fr.mwet.snake.DI
import fr.mwet.snake.assets.AssetHandler
import fr.mwet.snake.utils.SoundHelper
import fr.mwet.snake.utils.WORLD_HEIGHT
import fr.mwet.snake.utils.WORLD_WIDTH
import ktx.assets.invoke
import ktx.assets.pool
import kotlin.math.abs
import kotlin.random.Random

const val SNAKE_DEFAULT_SPEED = 6f

class Snake(val gameWorld: GameWorld) {
    //    private val gameWorld = DI.inject<GameWorld>()
    private val assetHandler = DI.inject<AssetHandler>()
    private val soundHelper = DI.inject<SoundHelper>()
    private val segmentPool = pool { Segment() }

    private var eTime = 0f
    private val originalAnimation =
        Animation(1f / 8f, assetHandler.snakeSegmentAnimation, Animation.PlayMode.LOOP_PINGPONG)
    private val animation =
        Animation(1f / 2f, assetHandler.snakeHeadAnimation, Animation.PlayMode.LOOP_PINGPONG)
    private val animationFlipped =
        Animation(1f / 2f, assetHandler.snakeHeadFlippedAnimation, Animation.PlayMode.LOOP_PINGPONG)
    private var color = Color.WHITE
    private lateinit var head: Segment
    private lateinit var tail: Segment

    private var currentDirection: Direction = Direction.UP
    private var nextX: Int = 0
    private var nextY: Int = 0
    private var currentSpeed = SNAKE_DEFAULT_SPEED
    private var isDisintegrating = false

    init {
        reset()
    }

    fun reset() {
        isDisintegrating = false
        eTime = 0f
        currentDirection = Direction.UP
        color = Color.WHITE
        currentSpeed = SNAKE_DEFAULT_SPEED

        if (this::head.isInitialized) {
            var segment: Segment? = tail
            while (segment != null) {
                val next = segment.next
                segmentPool(segment)
                segment = next
            }
        }

        head = segmentPool().apply { updatePosition(3, 3) }
        tail = segmentPool().apply { updatePosition(4, 3) }
        tail.next = head

        updateNext()
    }

    fun render(batch: SpriteBatch, delta: Float) {
        eTime += delta
//        batch.setColor(color)

        if (isDisintegrating) {
            var segment: Segment? = tail
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
        } else {
            var segment: Segment? = tail
            while (segment != null) {
                val next = segment.next
                if (next != null) {
                    segment.x += (next.ox - segment.ox) * delta * currentSpeed
                    segment.y += (next.oy - segment.oy) * delta * currentSpeed
                }
                if (segment == tail) {
//                    batch.draw(assetHandler.snakeTail, segment.x, segment.y, 1f, 1f)
                    batch.draw(
                        assetHandler.snakeTail,
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
                } else if (segment == head) {
                } else {
//                    batch.draw(animation.getKeyFrame(eTime), segment.x, segment.y, 1f, 1f)
                    batch.draw(
                        assetHandler.snakeBody,
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
                segment = next
            }

            head.x += (nextX - head.ox) * delta * currentSpeed
            head.y += (nextY - head.oy) * delta * currentSpeed

            if (hitBoundariesTest() || hitBodyTest()) disintegrate()

//            batch.draw(animation.getKeyFrame(eTime), head.x, head.y, 1f, 1f)
            if (currentDirection == Direction.RIGHT) {
                batch.draw(
                    animationFlipped.getKeyFrame(eTime),
                    head.x,
                    head.y,
                    0.5f,
                    0.5f,
                    1f,
                    1f,
                    1f,
                    1f,
                    head.getAngleFromDirection(currentDirection)
                )
            } else {
                batch.draw(
                    animation.getKeyFrame(eTime),
                    head.x,
                    head.y,
                    0.5f,
                    0.5f,
                    1f,
                    1f,
                    1f,
                    1f,
                    head.getAngleFromDirection(currentDirection)
                )
            }
            if (abs(nextX - head.x) < 0.15f && abs(nextY - head.y) < 0.15f) shiftSegments()
        }

//        batch.resetColor()
    }

    private fun shiftSegments() {
        soundHelper.playMove()

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

    fun setDirection(newDirection: Direction) {
        if (newDirection.isOpposite(currentDirection)) return
        currentDirection = newDirection
    }

    fun setSpeed(speed: Float) {
        currentSpeed = speed
    }

    fun hitFoodTest(food: Food): Boolean {
        if (abs(head.x - food.x) < 0.1f && abs(head.y - food.y) < 0.1f) {
            updateNext()
            addSegment(nextX, nextY)
            color = food.color
            soundHelper.playEat()
            return true
        }
        return false
    }

    private fun hitBoundariesTest(): Boolean {
        return head.x < (-1 + 0.15f) || head.x >= WORLD_WIDTH || head.y < (-1 + 0.15f) || head.y >= WORLD_HEIGHT
    }

    private fun hitBodyTest(): Boolean {
        var segment = tail
        var index = 0
        while (segment != head) {
            if (abs(segment.x - head.x) < 0.15f && abs(segment.y - head.y) < 0.15f) {
                println("Hit body: $index")
                return true
            }
            segment = segment.next!!
            index++
        }
        return false
    }

    fun collides(x: Int, y: Int): Boolean {
        var segment = tail
        while (segment.next != null) {
            if (abs(segment.x - x) < 0.15f && abs(segment.y - y) < 0.15f) {
                println("Collides: $x, $y")
                return true
            }
            segment = segment.next!!
        }
        return false
    }

    private fun disintegrate() {
        isDisintegrating = true
        Timer.schedule(object : Timer.Task() {
            override fun run() {
                gameWorld.lost()
            }
        }, 1.5f)
    }

    private fun addSegment(ox: Int, oy: Int) {
        val segment = segmentPool().apply { updatePosition(ox, oy) }
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

    val tx: Float = Random.nextInt(0, WORLD_WIDTH.toInt() - 1).toFloat()
    val ty: Float = Random.nextInt(0, WORLD_WIDTH.toInt() - 1).toFloat()
    val angle: Float = Random.nextInt(0, 360).toFloat()

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

    fun getAngleFromDirection(direction: Direction): Float {
        return when (direction) {
            Direction.UP -> 270f
            Direction.RIGHT -> 180f
            Direction.DOWN -> 90f
            Direction.LEFT -> 0f
        }
    }

    fun getAngleFromNext(): Float {
        val direction = if (next == null) Direction.UP
        else if (ox < next!!.ox) Direction.RIGHT
        else if (ox > next!!.ox) Direction.LEFT
        else if (oy > next!!.oy) Direction.DOWN
        else if (oy < next!!.oy) Direction.UP
        else /* WTF */ Direction.UP

        return getAngleFromDirection(direction)
    }
}

enum class Direction(val directionX: Int, val directionY: Int) {
    UP(0, 1),
    RIGHT(1, 0),
    DOWN(0, -1),
    LEFT(-1, 0);

    fun isOpposite(direction: Direction): Boolean {
        if (this == DOWN && direction == UP) return true
        if (this == UP && direction == DOWN) return true
        if (this == RIGHT && direction == LEFT) return true
        if (this == LEFT && direction == RIGHT) return true
        return false
    }
}
