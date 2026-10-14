package fr.mwet.snake

import com.badlogic.gdx.assets.AssetManager
import fr.mwet.snake.assets.FontHandler
import fr.mwet.snake.assets.I18N
import fr.mwet.snake.assets.I18NHandler
import fr.mwet.snake.assets.MusicHandler
import fr.mwet.snake.assets.SoundHandler
import fr.mwet.snake.assets.TextureHandler
import fr.mwet.snake.assets.registerFontLoaders
import ktx.assets.DisposableContainer
import ktx.assets.DisposableRegistry

private const val LOADING_MILLIS_PER_FRAME = 16

class AssetLoading : DisposableRegistry by DisposableContainer() {
    private val assetManager = AssetManager().apply { registerFontLoaders() }.alsoRegister()
    private val textures = TextureHandler(assetManager).alsoRegister()
    private val fonts = FontHandler(assetManager)
    private val sounds = SoundHandler(assetManager)
    private val music = MusicHandler(assetManager)
    private val i18n = I18NHandler(assetManager)

    val progress: Progress get() = Progress(assetManager.progress)

    fun update(): LoadedAssets? {
        if (!assetManager.update(LOADING_MILLIS_PER_FRAME)) return null
        I18N.i18nBundle = i18n.t
        return LoadedAssets(textures, fonts, sounds, music)
    }
}

class LoadedAssets(
    val textures: TextureHandler,
    val fonts: FontHandler,
    val sounds: SoundHandler,
    val music: MusicHandler,
)

@JvmInline
value class Progress(val value: Float)
