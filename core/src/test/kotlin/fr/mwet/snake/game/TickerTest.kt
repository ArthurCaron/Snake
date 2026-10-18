package fr.mwet.snake.game

import kotlin.test.Test
import kotlin.test.assertEquals

class TickerTest {
    private val sut = Ticker(5f)

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

    @Test
    fun `resetting the timing preserves speed while a new game restores initial speed`() {
        sut.speedUp(by = 5f, upTo = 12f)
        sut.update(0.05f)

        sut.resetTiming()

        assertEquals(UpdateResult.KeepWaiting, sut.update(0.06f))
        assertEquals(UpdateResult.DoUpdate, sut.update(0.05f))

        sut.reset()

        assertEquals(UpdateResult.KeepWaiting, sut.update(0.11f))
        assertEquals(UpdateResult.DoUpdate, sut.update(0.1f))
    }
}
