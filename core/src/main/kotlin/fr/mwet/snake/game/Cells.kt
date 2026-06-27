package fr.mwet.snake.game

import com.badlogic.gdx.math.Vector2
import fr.mwet.snake.DI
import fr.mwet.snake.utils.WORLD_HEIGHT
import fr.mwet.snake.utils.WORLD_WIDTH
import fr.mwet.snake.utils.doesNotContain

class Cells {
    private val allPositions: List<Cell> = buildList {
        (0..<WORLD_WIDTH.toInt()).forEach { width ->
            (0..<WORLD_HEIGHT.toInt()).forEach { height ->
                add(Cell(DI.vectorPool.obtain(width, height)))
            }
        }
    }
    private val availableCells = mutableSetOf<Cell>()
    private val forbiddenPositions = mutableSetOf<Vector2>()

    fun randomAvailableCell(snake: Snake): Cell? {
        var segment: Segment? = snake.head
        while (segment != null) {
            forbiddenPositions.add(segment.position)
            segment = segment.next
        }

        availableCells.clear()
        allPositions.forEach {
            if (forbiddenPositions.doesNotContain(it.position)) {
                availableCells.add(it)
            }
        }
        forbiddenPositions.clear()

        return availableCells.randomOrNull()
    }
}

data class Cell(val position: Vector2)
