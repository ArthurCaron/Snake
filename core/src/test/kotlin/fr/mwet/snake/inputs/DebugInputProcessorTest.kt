package fr.mwet.snake.inputs

import com.badlogic.gdx.Input
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DebugInputProcessorTest {
    @Test
    fun `only the hidden shortcut activates the screen callback`() {
        var activations = 0
        val input = DebugInputProcessor { activations++ }
        assertFalse(input.keyDown(Input.Keys.F9))
        assertFalse(input.keyTyped('2'))
        assertFalse(input.keyTyped('`'))
        assertEquals(0, activations)
        assertTrue(input.keyTyped('²'))
        assertEquals(1, activations)
    }
}
