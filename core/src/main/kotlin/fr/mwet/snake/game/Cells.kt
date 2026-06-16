package fr.mwet.snake.game

import fr.mwet.snake.utils.WORLD_HEIGHT
import fr.mwet.snake.utils.WORLD_WIDTH

class Cells {
    private val availableCells = mutableSetOf<Cell>()
    private val blockedCells = mutableSetOf<Cell>()

    init {
        (0..WORLD_WIDTH.toInt()).forEach { width ->
            (0..WORLD_HEIGHT.toInt()).forEach { height ->
                availableCells.add(Cell(width, height))
            }
        }
    }

    fun computeAvailableCells(snake: Snake): Set<Cell> {
        freeAllCells()

        blockedCells.add(Cell(snake.head.x.toInt(), snake.head.y.toInt()))
        blockedCells.add(Cell(snake.tail.x.toInt(), snake.tail.y.toInt()))

        var segment: Segment? = snake.tail.next
        while (segment != null) {
            val next = segment.next
            if (next != null) {
                blockedCells.add(Cell(segment.x.toInt(), segment.y.toInt()))
            }
            segment = next
        }

        availableCells.removeAll(blockedCells)
        return availableCells
    }

    private fun freeAllCells() {
        availableCells.addAll(blockedCells)
        blockedCells.clear()
    }
}

data class Cell(val x: Int, val y: Int)
