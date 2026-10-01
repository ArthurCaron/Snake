package fr.mwet.snake.game

import fr.mwet.snake.utils.Direction
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SnakeTest {
    private val sut = Snake().apply { reset() }

    @Test
    fun `keeps a second turn given before the next tick`() {
        sut.setDirection(Direction.LEFT)
        sut.setDirection(Direction.DOWN)

        sut.move(eatFood = false)
        assertEquals(Direction.LEFT, sut.currentDirection)
        sut.move(eatFood = false)
        assertEquals(Direction.DOWN, sut.currentDirection)
    }

    @Test
    fun `can move into the cell the tail is leaving, but not when the snake grows`() {
        moveHeadNextToTheTail()

        assertEquals(sut.tail.position, sut.nextHeadPosition)
        assertFalse(sut.wouldCollideWithBodyAfterMoving(willEatFood = false))
        assertTrue(sut.wouldCollideWithBodyAfterMoving(willEatFood = true))
    }

    private fun moveHeadNextToTheTail() {
        sut.move(eatFood = true)
        sut.move(eatFood = true)
        sut.setDirection(Direction.RIGHT)
        sut.move(eatFood = false)
        sut.setDirection(Direction.DOWN)
        sut.move(eatFood = false)
        sut.setDirection(Direction.LEFT)
    }
}
