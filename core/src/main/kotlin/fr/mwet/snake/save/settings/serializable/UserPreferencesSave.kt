package fr.mwet.snake.save.settings.serializable

import fr.mwet.snake.utils.NO_VERSION
import fr.mwet.snake.save.settings.Language
import fr.mwet.snake.tuning.FontTheme

private const val NO_LOCALE = ""

class UserPreferencesSave() {
    var version: Int = NO_VERSION
    var locale: String = NO_LOCALE
    var soundOn: Boolean = true
    var musicOn: Boolean = true
    var fontTheme: String = DefaultUserPreferences.fontTheme.tag

    constructor(version: Int, locale: String, soundOn: Boolean, musicOn: Boolean, fontTheme: String) : this() {
        this.version = version
        this.locale = locale
        this.soundOn = soundOn
        this.musicOn = musicOn
        this.fontTheme = fontTheme
    }

    fun restoreDefaults() {
        version = if (version == NO_VERSION) DefaultUserPreferences.defaultUserPreferences().version else version
        locale = if (locale == NO_LOCALE) DefaultUserPreferences.defaultUserPreferences().locale else locale
    }
}

object DefaultUserPreferences {
    val fontTheme: FontTheme = FontTheme.Classic

    fun defaultUserPreferences() = UserPreferencesSave(
        version = 0,
        locale = Language.System.tag,
        soundOn = true,
        musicOn = true,
        fontTheme = fontTheme.tag,
    )
}
