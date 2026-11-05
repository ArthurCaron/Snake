package fr.mwet.snake.save.settings

import fr.mwet.snake.render.Appearance
import fr.mwet.snake.render.MarkShape
import fr.mwet.snake.save.serialization.InMemoryTextStore
import fr.mwet.snake.save.serialization.JsonStore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AppearancePreferencesTest {
    @Test
    fun `all appearance choices survive reload and reset without changing other preferences`() {
        val store = InMemoryTextStore()
        val repository = SettingsRepository(JsonStore(store))
        repository.toggleMusic()
        val look = Appearance("1257ab", "fedcba", MarkShape.Heart, true, true, true, true)
        repository.setAppearance(look)

        val restored = SettingsRepository(JsonStore(store))
        assertEquals(look, restored.appearance)
        assertEquals(look.body, restored.appearance.body)
        assertEquals(look.marks, restored.appearance.marks)
        restored.setAppearance(Appearance())
        val reset = SettingsRepository(JsonStore(store))
        assertEquals(Appearance(), reset.appearance)
        assertEquals(false, reset.musicOn)
    }

    @Test
    fun `invalid colors and symbol use the default appearance`() {
        val store = InMemoryTextStore()
        store.write("settings/user_preferences.json", """{"snakeBody":"invalid","snakeMarks":"123","snakeSymbol":"unknown"}""")
        assertEquals(Appearance(), SettingsRepository(JsonStore(store)).appearance)
        assertEquals("12abef", Appearance.colorHex(" #12ABef "))
        assertNull(Appearance.colorHex("12345"))
        assertNull(Appearance.colorHex("12345678"))
        assertNull(Appearance.colorHex("gggggg"))
    }
}
