package fr.mwet.snake.save.settings

import fr.mwet.snake.inputs.GameActionId
import fr.mwet.snake.inputs.GeneralActionId
import fr.mwet.snake.save.serialization.JsonStore
import fr.mwet.snake.save.settings.serializable.*

private const val ROOT_FOLDER = "settings"
private const val KEYMAPPINGS_FILE = "${ROOT_FOLDER}/keymappings.json"
private const val USER_PREFERENCES_FILE = "${ROOT_FOLDER}/user_preferences.json"

class SettingsRepository(private val jsonStore: JsonStore) {
    var version = 0
        private set
    var settings = load()
        private set
    val soundOn: Boolean get() = settings.userPreferences.soundOn
    val musicOn: Boolean get() = settings.userPreferences.musicOn
    val language: Language get() = Language.fromTag(settings.userPreferences.locale)

    fun toggleSound() {
        save(settings.copy(userPreferences = settings.userPreferences.copy(soundOn = !soundOn)))
    }

    fun toggleMusic() {
        save(settings.copy(userPreferences = settings.userPreferences.copy(musicOn = !musicOn)))
    }

    fun setLanguage(language: Language) {
        save(settings.copy(userPreferences = settings.userPreferences.copy(locale = language.tag)))
    }

    fun updateKeymappings(transform: (Keymappings) -> Keymappings) {
        save(settings.copy(keymappings = transform(settings.keymappings)))
    }

    fun resetKeymappings() {
        save(settings.copy(keymappings = defaultKeymappings()))
    }

    private fun load(): Settings {
        val keymappingsSave = jsonStore.readOrCreate(KEYMAPPINGS_FILE) { DefaultKeymappings.defaultKeymappings() }
        keymappingsSave.restoreDefaults()

        val userPreferencesSave =
            jsonStore.readOrCreate(USER_PREFERENCES_FILE) { DefaultUserPreferences.defaultUserPreferences() }
        userPreferencesSave.restoreDefaults()

        val settings = Settings(
            userPreferences = UserPreferences(
                version = userPreferencesSave.version,
                locale = userPreferencesSave.locale,
                soundOn = userPreferencesSave.soundOn,
                musicOn = userPreferencesSave.musicOn,
            ),
            keymappings = Keymappings(
                game = keymappingsSave.game.map { it.toKeymapping() },
                general = keymappingsSave.general.map { it.toKeymapping() },
            ),
        )
        save(settings)

        return settings
    }

    private fun defaultKeymappings(): Keymappings = DefaultKeymappings.defaultKeymappings().let { defaults ->
        Keymappings(
            game = defaults.game.map { it.toKeymapping() },
            general = defaults.general.map { it.toKeymapping() },
        )
    }

    private fun save(settings: Settings) {
        this.settings = settings
        jsonStore.write(KEYMAPPINGS_FILE, settings.keymappings.toSerializable())
        jsonStore.write(USER_PREFERENCES_FILE, settings.userPreferences.toSerializable())
        version++
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

    private fun UserPreferences.toSerializable() =
        UserPreferencesSave(version = version, locale = locale, soundOn = soundOn, musicOn = musicOn)
}
