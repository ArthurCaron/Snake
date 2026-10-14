package fr.mwet.snake.assets

import com.badlogic.gdx.assets.AssetDescriptor
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.utils.I18NBundle
import ktx.assets.getValue
import ktx.assets.loadAsset
import ktx.i18n.BundleLine

private const val I18N_ROOT = "i18n/translations"

class I18NHandler(assetManager: AssetManager) {
    val t: I18NBundle by assetManager.loadAsset(AssetDescriptor(I18N_ROOT, I18NBundle::class.java))
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
    playAgain;

    override val bundle: I18NBundle
        get() = i18nBundle

    companion object {
        lateinit var i18nBundle: I18NBundle
    }
}
