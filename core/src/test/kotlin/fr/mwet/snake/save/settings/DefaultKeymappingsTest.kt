package fr.mwet.snake.save.settings

import com.badlogic.gdx.Input.Keys
import fr.mwet.snake.inputs.GameActionId
import fr.mwet.snake.save.settings.serializable.DefaultKeymappings
import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultKeymappingsTest {
    @Test
    fun `defaults keep physical WASD positions and arrows for desktop and the QWERTY fallback`() {
        val keys = DefaultKeymappings.defaultKeymappings().game.associate { it.action to it.keys.toList() }
        // On desktop GLFW maps these physical positions to the current keyboard's labels.
        // Substituting Z and Q codes here would move the bindings on an AZERTY keyboard.
        assertEquals(listOf(Keys.W, Keys.UP), keys.getValue(GameActionId.GoUp))
        assertEquals(listOf(Keys.A, Keys.LEFT), keys.getValue(GameActionId.GoLeft))
        assertEquals(listOf(Keys.S, Keys.DOWN), keys.getValue(GameActionId.GoDown))
        assertEquals(listOf(Keys.D, Keys.RIGHT), keys.getValue(GameActionId.GoRight))
    }
}
