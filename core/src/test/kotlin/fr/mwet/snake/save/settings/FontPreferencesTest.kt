package fr.mwet.snake.save.settings

import fr.mwet.snake.save.serialization.JsonStore
import fr.mwet.snake.save.serialization.InMemoryTextStore
import fr.mwet.snake.save.settings.serializable.DefaultUserPreferences
import fr.mwet.snake.tuning.FontTheme
import kotlin.test.Test
import kotlin.test.assertEquals

class FontPreferencesTest {
    @Test
    fun `new missing empty and unknown font preferences use the declared default`() {
        val inputs = listOf(null, "{}", "{\"fontTheme\":\"\"}", "{\"fontTheme\":\"unknown-font\"}")
        for (input in inputs) {
            val store = InMemoryTextStore()
            if (input != null) store.write("settings/user_preferences.json", input)
            val settings = SettingsRepository(JsonStore(store), keysArePhysical = true)
            assertEquals(DefaultUserPreferences.fontTheme, settings.fontTheme)
            if (input == null || input == "{}") {
                assertEquals(DefaultUserPreferences.fontTheme.tag, settings.settings.userPreferences.fontTheme)
            }
        }
    }

    @Test
    fun `a saved font choice survives a settings reload`() {
        val store = InMemoryTextStore()
        SettingsRepository(JsonStore(store), keysArePhysical = true).setFontTheme(FontTheme.Silkscreen)
        val restored = SettingsRepository(JsonStore(store), keysArePhysical = true)
        assertEquals(FontTheme.Silkscreen, restored.fontTheme)
        assertEquals(FontTheme.Silkscreen.tag, restored.settings.userPreferences.fontTheme)
    }
}
