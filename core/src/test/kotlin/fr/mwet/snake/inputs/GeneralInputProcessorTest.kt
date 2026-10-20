package fr.mwet.snake.inputs

import com.badlogic.gdx.Input.Keys
import fr.mwet.snake.save.serialization.JsonStore
import fr.mwet.snake.save.serialization.TextStore
import fr.mwet.snake.save.settings.SettingsRepository
import fr.mwet.snake.save.settings.addGeneralKey
import fr.mwet.snake.save.settings.removeGeneralKey
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GeneralInputProcessorTest {
    private val jsonStore = JsonStore(InMemoryTextStore())

    @Test
    fun `start and resume use the same remappable action without consuming inactive actions`() {
        val settings = SettingsRepository(jsonStore, keysArePhysical = true)
        var starts = 0
        var resumes = 0
        var paused = false
        val mainMenu = GeneralInputProcessor(settings, GeneralCommandContext(onStartGame = { starts++; true }))
        val game = GeneralInputProcessor(settings, GeneralCommandContext(onStartGame = {
            if (paused) {
                resumes++
                paused = false
                true
            } else false
        }))

        assertTrue(mainMenu.keyDown(Keys.ENTER))
        assertFalse(game.keyDown(Keys.ENTER))
        assertFalse(mainMenu.keyDown(Keys.ESCAPE))
        assertEquals(1, starts)
        assertEquals(0, resumes)

        settings.updateKeymappings {
            it.removeGeneralKey(StartGameCommand, Keys.ENTER).addGeneralKey(StartGameCommand, Keys.F5)
        }
        paused = true
        assertFalse(game.keyDown(Keys.ENTER))
        assertTrue(game.keyDown(Keys.F5))
        assertFalse(game.keyDown(Keys.F5))
        assertTrue(mainMenu.keyDown(Keys.F5))
        assertEquals(2, starts)
        assertEquals(1, resumes)
    }
}

private class InMemoryTextStore : TextStore {
    private val files = mutableMapOf<String, String>()
    override fun read(path: String) = files[path]
    override fun write(path: String, text: String) { files[path] = text }
    override fun exists(path: String) = path in files
    override fun delete(path: String) = files.remove(path) != null
    override fun backupCorrupted(path: String) = files.remove(path) != null
}
