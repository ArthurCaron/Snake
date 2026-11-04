package fr.mwet.snake.save.settings

import fr.mwet.snake.backdrop.BackdropStyle
import fr.mwet.snake.inputs.GameActionId
import fr.mwet.snake.inputs.GeneralActionId
import fr.mwet.snake.render.GroundStyle
import fr.mwet.snake.save.serialization.JsonStore
import fr.mwet.snake.save.settings.serializable.DefaultKeymappings
import fr.mwet.snake.save.settings.serializable.DefaultUserPreferences
import fr.mwet.snake.save.settings.serializable.GameKeymappingSave
import fr.mwet.snake.save.settings.serializable.GeneralKeymappingSave
import fr.mwet.snake.save.settings.serializable.KeymappingsSave
import fr.mwet.snake.save.settings.serializable.UserPreferencesSave
import fr.mwet.snake.transition.TransitionStyle
import fr.mwet.snake.tuning.FontTheme

private const val ROOT_FOLDER = "settings"
private const val KEYMAPPINGS_FILE = "${ROOT_FOLDER}/keymappings.json"
private const val USER_PREFERENCES_FILE = "${ROOT_FOLDER}/user_preferences.json"

class SettingsRepository(private val jsonStore: JsonStore) {
    private val defaultKeymappingsSave = DefaultKeymappings.defaultKeymappings()
    var version = 0
        private set
    var settings = load()
        private set
    val soundOn: Boolean get() = settings.userPreferences.soundOn
    val musicOn: Boolean get() = settings.userPreferences.musicOn
    val language: Language get() = Language.fromTag(settings.userPreferences.locale)
    val fontTheme: FontTheme
        get() = FontTheme.fromTag(settings.userPreferences.fontTheme) ?: DefaultUserPreferences.fontTheme
    val transition: TransitionStyle
        get() = TransitionStyle.fromTag(settings.userPreferences.transition)
    val backdrop: BackdropStyle get() = BackdropStyle.fromTag(settings.userPreferences.backdrop)

    fun toggleSound() {
        save(settings.copy(userPreferences = settings.userPreferences.copy(soundOn = !soundOn)))
    }

    fun toggleMusic() {
        save(settings.copy(userPreferences = settings.userPreferences.copy(musicOn = !musicOn)))
    }

    fun setLanguage(language: Language) {
        save(settings.copy(userPreferences = settings.userPreferences.copy(locale = language.tag)))
    }

    fun setFontTheme(theme: FontTheme) {
        save(settings.copy(userPreferences = settings.userPreferences.copy(fontTheme = theme.tag)))
    }

    fun setTransition(style: TransitionStyle) {
        save(settings.copy(userPreferences = settings.userPreferences.copy(transition = style.tag)))
    }

    fun setBackdrop(style: BackdropStyle) {
        save(settings.copy(userPreferences = settings.userPreferences.copy(backdrop = style.tag)))
    }

    val ground: GroundStyle get() = GroundStyle.fromTag(settings.userPreferences.ground)

    fun setGround(style: GroundStyle) {
        save(settings.copy(userPreferences = settings.userPreferences.copy(ground = style.tag)))
    }

    fun updateKeymappings(transform: (Keymappings) -> Keymappings) {
        save(settings.copy(keymappings = transform(settings.keymappings)))
    }

    fun resetKeymappings() {
        save(settings.copy(keymappings = defaultKeymappings()))
    }

    private fun load(): Settings {
        val keymappingsSave = jsonStore.readOrCreate(KEYMAPPINGS_FILE) { DefaultKeymappings.defaultKeymappings() }
        keymappingsSave.restoreDefaults(defaultKeymappingsSave)

        val userPreferencesSave =
            jsonStore.readOrCreate(USER_PREFERENCES_FILE) { DefaultUserPreferences.defaultUserPreferences() }
        userPreferencesSave.restoreDefaults()

        val settings = Settings(
            userPreferences = UserPreferences(
                version = userPreferencesSave.version,
                locale = userPreferencesSave.locale,
                soundOn = userPreferencesSave.soundOn,
                musicOn = userPreferencesSave.musicOn,
                fontTheme = userPreferencesSave.fontTheme,
                transition = userPreferencesSave.transition,
                backdrop = userPreferencesSave.backdrop,
                ground = userPreferencesSave.ground,
            ),
            keymappings = Keymappings(
                game = keymappingsSave.game.map { it.toKeymapping() },
                general = keymappingsSave.general.map { it.toKeymapping() },
            ),
        )
        save(settings)

        return settings
    }

    fun defaultKeymappings(): Keymappings = defaultKeymappingsSave.let { defaults ->
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

    // An older save can hold more keys than the controls screen shows: only the first slots stay.
    private fun GameKeymappingSave.toKeymapping() = GameKeymapping(action.gameCommand, keys.take(KEYS_PER_ACTION))
    private fun GeneralKeymappingSave.toKeymapping() = GeneralKeymapping(action.generalCommand, keys.take(KEYS_PER_ACTION))

    private fun Keymappings.toSerializable() = KeymappingsSave(
        version = defaultKeymappingsSave.version,
        game = game.map { it.toSerializable() },
        general = general.map { it.toSerializable() },
    )

    private fun GameKeymapping.toSerializable() = GameKeymappingSave(action = GameActionId.from(action), keys = keys)
    private fun GeneralKeymapping.toSerializable() =
        GeneralKeymappingSave(action = GeneralActionId.from(action), keys = keys)

    private fun UserPreferences.toSerializable() = UserPreferencesSave(
        version = version,
        locale = locale,
        soundOn = soundOn,
        musicOn = musicOn,
        fontTheme = fontTheme,
        transition = transition,
        backdrop = backdrop,
        ground = ground,
    )
}
