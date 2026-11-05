package fr.mwet.snake.save.settings.serializable

import fr.mwet.snake.render.Appearance
import fr.mwet.snake.save.settings.Language
import fr.mwet.snake.tuning.FontTheme
import fr.mwet.snake.utils.NO_VERSION

private const val NO_LOCALE = ""

class UserPreferencesSave() {
    var version: Int = NO_VERSION
    var locale: String = NO_LOCALE
    var soundOn: Boolean = true
    var musicOn: Boolean = true
    var fontTheme: String = DefaultUserPreferences.fontTheme.tag
    var transition: String = ""
    var backdrop: String = ""
    var ground: String = ""
    var snakeBody: String = Appearance.DEFAULT_BODY
    var snakeMarks: String = Appearance.DEFAULT_MARKS
    var snakeSymbol: String = "diamond"
    var takesFruitColor: Boolean = false
    var snakeGlow: Boolean = false
    var fruitGlow: Boolean = false
    var menuGlow: Boolean = false

    constructor(
        version: Int,
        locale: String,
        soundOn: Boolean, musicOn: Boolean,
        fontTheme: String,
        transition: String,
        backdrop: String,
        ground: String,
        snakeBody: String = Appearance.DEFAULT_BODY,
        snakeMarks: String = Appearance.DEFAULT_MARKS,
        snakeSymbol: String = "diamond",
        takesFruitColor: Boolean = false,
        snakeGlow: Boolean = false,
        fruitGlow: Boolean = false,
        menuGlow: Boolean = false,
    ) : this() {
        this.version = version
        this.locale = locale
        this.soundOn = soundOn
        this.musicOn = musicOn
        this.fontTheme = fontTheme
        this.transition = transition
        this.backdrop = backdrop
        this.ground = ground
        this.snakeBody = snakeBody
        this.snakeMarks = snakeMarks
        this.snakeSymbol = snakeSymbol
        this.takesFruitColor = takesFruitColor
        this.snakeGlow = snakeGlow
        this.fruitGlow = fruitGlow
        this.menuGlow = menuGlow
    }

    fun restoreDefaults() {
        version = if (version == NO_VERSION) DefaultUserPreferences.defaultUserPreferences().version else version
        locale = if (locale == NO_LOCALE) DefaultUserPreferences.defaultUserPreferences().locale else locale
    }
}

object DefaultUserPreferences {
    val fontTheme: FontTheme = FontTheme.Lilita

    fun defaultUserPreferences() = UserPreferencesSave(
        version = 0,
        locale = Language.System.tag,
        soundOn = true,
        musicOn = true,
        fontTheme = fontTheme.tag,
        transition = "",
        backdrop = "",
        ground = "",
    )
}
