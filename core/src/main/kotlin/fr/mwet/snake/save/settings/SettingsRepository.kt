package fr.mwet.snake.save.settings

import fr.mwet.snake.inputs.GameActionId
import fr.mwet.snake.inputs.GeneralActionId
import fr.mwet.snake.save.serialization.JsonStore
import fr.mwet.snake.save.settings.serializable.*

private const val ROOT_FOLDER = "settings"
private const val KEYMAPPINGS_FILE = "${ROOT_FOLDER}/keymappings.json"
private const val USER_PREFERENCES_FILE = "${ROOT_FOLDER}/user_preferences.json"

class SettingsRepository(private val jsonStore: JsonStore) {
    fun load(): Settings {
        val keymappingsSave = jsonStore.readOrCreate(KEYMAPPINGS_FILE) { DefaultKeymappings.defaultKeymappings() }
        keymappingsSave.restoreDefaults()

        val userPreferencesSave =
            jsonStore.readOrCreate(USER_PREFERENCES_FILE) { DefaultUserPreferences.defaultUserPreferences() }
        userPreferencesSave.restoreDefaults()

        val settings = Settings(
            userPreferences = UserPreferences(
                version = userPreferencesSave.version,
                locale = userPreferencesSave.locale,
            ),
            keymappings = Keymappings(
                game = keymappingsSave.game.map { it.toKeymapping() },
                general = keymappingsSave.general.map { it.toKeymapping() },
            ),
        )
        save(settings)

        return settings
    }

    fun save(settings: Settings) {
        jsonStore.write(KEYMAPPINGS_FILE, settings.keymappings.toSerializable())
        jsonStore.write(USER_PREFERENCES_FILE, settings.userPreferences.toSerializable())
    }

    private fun GameKeymappingSave.toKeymapping() = GameKeymapping(action.gameCommand, keys)
    private fun GeneralKeymappingSave.toKeymapping() = GeneralKeymapping(action.generalCommand, keys)

    private fun Keymappings.toSerializable() = KeymappingsSave(
        version = DefaultKeymappings.defaultKeymappings().version,
        game = game.map { it.toSerializable() },
        general = general.map { it.toSerializable() },
    )

    private fun GameKeymapping.toSerializable() = GameKeymappingSave(action = GameActionId.from(action), keys = keys)
    private fun GeneralKeymapping.toSerializable() =
        GeneralKeymappingSave(action = GeneralActionId.from(action), keys = keys)

    private fun UserPreferences.toSerializable() = UserPreferencesSave(version = version, locale = locale)
}
