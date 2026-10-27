package fr.mwet.snake.game

import com.badlogic.gdx.math.Vector2
import fr.mwet.snake.tuning.GameplayTuning.START_LENGTH
import fr.mwet.snake.tuning.GameplayTuning.WORLD_HEIGHT
import fr.mwet.snake.tuning.GameplayTuning.WORLD_WIDTH
import fr.mwet.snake.utils.Direction

class SnakeStart(val segments: List<Vector2>, val direction: Direction) {
    val foodsAlreadyEaten get() = segments.size - START_LENGTH

    companion object {
        fun random(): SnakeStart {
            val margin = START_LENGTH - 1
            val head = Vector2(
                (margin..WORLD_WIDTH.toInt() - 1 - margin).random().toFloat(),
                (margin..WORLD_HEIGHT.toInt() - 1 - margin).random().toFloat(),
            )
            val direction = Direction.entries.maxBy { distanceToWall(head, it) }
            val segments = List(START_LENGTH) { index ->
                Vector2(head.x - direction.directionX * index, head.y - direction.directionY * index)
            }
            return SnakeStart(segments, direction)
        }

        fun almostWon(): SnakeStart {
            val zigzag = buildList {
                (0..<WORLD_HEIGHT.toInt()).forEach { y ->
                    val xs = if (y % 2 == 0) 0..<WORLD_WIDTH.toInt() else (WORLD_WIDTH.toInt() - 1) downTo 0
                    xs.forEach { x -> add(Vector2(x.toFloat(), y.toFloat())) }
                }
            }
            val freeCell = zigzag.last()
            val segments = zigzag.dropLast(1).asReversed()
            val head = segments.first()
            val direction = Direction.entries.first {
                head.x + it.directionX == freeCell.x && head.y + it.directionY == freeCell.y
            }
            return SnakeStart(segments, direction)
        }

        private fun distanceToWall(position: Vector2, direction: Direction): Float = when (direction) {
            Direction.UP -> WORLD_HEIGHT - 1 - position.y
            Direction.RIGHT -> WORLD_WIDTH - 1 - position.x
            Direction.DOWN -> position.y
            Direction.LEFT -> position.x
        }
    }
}
