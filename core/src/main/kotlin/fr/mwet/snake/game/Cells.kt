package fr.mwet.snake.game

import com.badlogic.gdx.math.Vector2
import fr.mwet.snake.DI
import fr.mwet.snake.utils.WORLD_HEIGHT
import fr.mwet.snake.utils.WORLD_WIDTH
import ktx.assets.pool

class Cells {
    private val cellPool = CellPool()
    private val availableCells = mutableSetOf<Cell>()
    private val blockedCells = mutableSetOf<Cell>()

    init {
        (0..<WORLD_WIDTH.toInt()).forEach { width ->
            (0..<WORLD_HEIGHT.toInt()).forEach { height ->
                availableCells.add(cellPool.obtain(DI.vectorPool.obtain(width, height)))
            }
        }
    }

    fun computeAvailableCells(snake: Snake): Set<Cell> {
        freeAllCells()

        blockedCells.add(cellPool.obtain(DI.vectorPool.obtain(snake.head.ox, snake.head.oy)))
        blockedCells.add(cellPool.obtain(DI.vectorPool.obtain(snake.tail.ox, snake.tail.oy)))

        var segment: Segment? = snake.tail.next
        while (segment != null) {
            val next = segment.next
            if (next != null) {
                blockedCells.add(cellPool.obtain(DI.vectorPool.obtain(segment.ox, segment.oy)))
            }
            segment = next
        }

        freeBlockedCells()
        return availableCells
    }

    private fun freeAllCells() {
        availableCells.addAll(blockedCells)
        blockedCells.clear()
    }

    private fun freeBlockedCells() {
        blockedCells.forEach {
            availableCells.remove(it)
            cellPool.free(it)
        }
    }
}

class CellPool {
    private val cellPool = pool { Cell(Vector2.Zero) }

    fun obtain(position: Vector2): Cell = cellPool.obtain().apply { this.position = position }
    fun free(cell: Cell) = cellPool.free(cell)
}

data class Cell(var position: Vector2)
