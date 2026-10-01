package fr.mwet.snake.game

import kotlin.test.Test
import kotlin.test.assertEquals

class TickerTest {
    private val sut = Ticker()

    @Test
    fun `ticks once the tick duration has elapsed`() {
        assertEquals(UpdateResult.KeepWaiting, sut.update(0.15f))
        assertEquals(UpdateResult.DoUpdate, sut.update(0.15f))
    }

    @Test
    fun `drops the backlog after a long frame instead of ticking on every following frame`() {
        assertEquals(UpdateResult.DoUpdate, sut.update(10f))
        assertEquals(UpdateResult.KeepWaiting, sut.update(1f / 60f))
    }
}
