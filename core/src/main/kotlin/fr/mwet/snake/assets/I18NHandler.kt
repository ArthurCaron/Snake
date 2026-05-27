package fr.mwet.snake.assets

import com.badlogic.gdx.assets.AssetDescriptor
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.assets.loaders.I18NBundleLoader.I18NBundleParameter
import com.badlogic.gdx.utils.I18NBundle
import ktx.assets.DisposableContainer
import ktx.assets.DisposableRegistry
import ktx.assets.getValue
import ktx.assets.loadAsset
import ktx.i18n.BundleLine
import java.util.*

private const val I18N_ROOT = "i18n/translations"

class I18NHandler(assetManager: AssetManager) : DisposableRegistry by DisposableContainer(), AssetsLoadedListener {
    // Should use this one (just stop forcing English) but right now it's in French and my fonts don't handle accents
//    val t: I18NBundle by assetManager.loadAsset(AssetDescriptor(I18N_ROOT, I18NBundle::class.java))
    val t: I18NBundle by assetManager
        .loadAsset(AssetDescriptor(I18N_ROOT, I18NBundle::class.java, I18NBundleParameter(Locale.ENGLISH)))

    override fun onAssetsLoaded() {
        I18N.i18nBundle = t
    }
}

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
