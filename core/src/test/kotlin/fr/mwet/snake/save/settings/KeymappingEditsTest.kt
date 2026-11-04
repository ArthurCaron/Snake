package fr.mwet.snake.save.settings

import com.badlogic.gdx.Input.Keys
import fr.mwet.snake.inputs.GoLeftCommand
import fr.mwet.snake.inputs.GoUpCommand
import fr.mwet.snake.inputs.PauseCommand
import kotlin.test.Test
import kotlin.test.assertEquals

class KeymappingEditsTest {
    @Test
    fun `a key is reassigned between gameplay and general actions`() {
        val keymappings = Keymappings(
            game = listOf(GameKeymapping(GoUpCommand, listOf(Keys.W, Keys.UP))),
            general = listOf(GeneralKeymapping(PauseCommand, listOf(Keys.SPACE))),
        )
        val pauseOnW = keymappings.setGeneralKey(PauseCommand, slot = 1, Keys.W)
        assertEquals(listOf(Keys.UP), pauseOnW.game.single().keys)
        assertEquals(listOf(Keys.SPACE, Keys.W), pauseOnW.general.single().keys)

        val upOnSpace = pauseOnW.setGameKey(GoUpCommand, slot = 1, Keys.SPACE)
        assertEquals(listOf(Keys.UP, Keys.SPACE), upOnSpace.game.single().keys)
        assertEquals(listOf(Keys.W), upOnSpace.general.single().keys)
    }

    @Test
    fun `a key replaces the key of its slot and is taken away from the other actions`() {
        val keymappings = Keymappings(
            game = listOf(
                GameKeymapping(GoUpCommand, listOf(Keys.W, Keys.UP)),
                GameKeymapping(GoLeftCommand, listOf(Keys.A, Keys.LEFT)),
            ),
        )

        val edited = keymappings.setGameKey(GoLeftCommand, slot = 0, Keys.W)

        assertEquals(listOf(Keys.UP), edited.game.single { it.action == GoUpCommand }.keys)
        assertEquals(listOf(Keys.W, Keys.LEFT), edited.game.single { it.action == GoLeftCommand }.keys)
    }

    @Test
    fun `removing a slot keeps the other key`() {
        val keymappings = Keymappings(game = listOf(GameKeymapping(GoUpCommand, listOf(Keys.W, Keys.UP))))

        assertEquals(listOf(Keys.W), keymappings.removeGameKey(GoUpCommand, slot = 1).game.single().keys)
    }
}
