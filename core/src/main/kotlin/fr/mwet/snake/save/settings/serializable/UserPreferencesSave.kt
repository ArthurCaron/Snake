package fr.mwet.snake.save.settings.serializable

import fr.mwet.snake.utils.NO_VERSION
import java.util.*

private const val NO_LOCALE = ""

class UserPreferencesSave() {
    var version: Int = NO_VERSION
    var locale: String = NO_LOCALE

    constructor(version: Int, locale: String) : this() {
        this.version = version
        this.locale = locale
    }

    fun restoreDefaults() {
        version = if (version == NO_VERSION) DefaultUserPreferences.defaultUserPreferences().version else version
        locale = if (locale == NO_LOCALE) DefaultUserPreferences.defaultUserPreferences().locale else locale
    }
}

object DefaultUserPreferences {
    fun defaultUserPreferences() = UserPreferencesSave(
        version = 0,
        locale = Locale.getDefault().toString(),
    )
}
