package fr.mwet.snake.utils

import com.badlogic.gdx.utils.I18NBundle
import ktx.i18n.BundleLine

enum class I18N : BundleLine {
    play,
    highscore,
    settings,
    settingsSound;

    override val bundle: I18NBundle
        get() = i18nBundle

    companion object {
        lateinit var i18nBundle: I18NBundle
    }
}
