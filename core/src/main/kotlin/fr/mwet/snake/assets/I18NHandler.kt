package fr.mwet.snake.assets

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.utils.I18NBundle
import ktx.i18n.BundleLine
import java.util.Locale

private const val I18N_ROOT = "i18n/translations"

fun loadTranslations(locale: Locale) {
    I18N.i18nBundle = I18NBundle.createBundle(Gdx.files.internal(I18N_ROOT), locale)
}

enum class I18N : BundleLine {
    play,
    highscore,
    settings,
    settingsSound,
    soundOn,
    soundOff,
    musicOn,
    musicOff,
    score,
    best,
    tapToStart,
    paused,
    resume,
    mainMenu,
    gameOver,
    victory,
    newHighScore,
    playAgain,
    back,
    noScores,
    controls,
    change,
    resetControls,
    pressAKey,
    actionGoUp,
    actionGoRight,
    actionGoDown,
    actionGoLeft,
    actionPause,
    actionBack,
    actionStartGame,
    language,
    languageSystem;

    override val bundle: I18NBundle
        get() = i18nBundle

    companion object {
        lateinit var i18nBundle: I18NBundle
    }
}
