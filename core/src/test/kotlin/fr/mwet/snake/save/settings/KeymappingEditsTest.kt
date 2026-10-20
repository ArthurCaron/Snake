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
        val pauseOnW = keymappings.addGeneralKey(PauseCommand, Keys.W)
        assertEquals(listOf(Keys.UP), pauseOnW.game.single().keys)
        assertEquals(listOf(Keys.SPACE, Keys.W), pauseOnW.general.single().keys)

        val upOnSpace = pauseOnW.addGameKey(GoUpCommand, Keys.SPACE)
        assertEquals(listOf(Keys.UP, Keys.SPACE), upOnSpace.game.single().keys)
        assertEquals(listOf(Keys.W), upOnSpace.general.single().keys)
    }

    @Test
    fun `adding a key keeps the keys of the action and takes the key away from the other actions`() {
        val keymappings = Keymappings(
            game = listOf(
                GameKeymapping(GoUpCommand, listOf(Keys.W, Keys.UP)),
                GameKeymapping(GoLeftCommand, listOf(Keys.A)),
            ),
        )

        val edited = keymappings.addGameKey(GoLeftCommand, Keys.W)

        assertEquals(listOf(Keys.UP), edited.game.single { it.action == GoUpCommand }.keys)
        assertEquals(listOf(Keys.A, Keys.W), edited.game.single { it.action == GoLeftCommand }.keys)
    }
}
