package fr.mwet.snake

import com.badlogic.gdx.assets.AssetManager
import fr.mwet.snake.assets.FontHandler
import fr.mwet.snake.assets.MusicHandler
import fr.mwet.snake.assets.SoundHandler
import fr.mwet.snake.assets.TextureHandler
import fr.mwet.snake.assets.registerFontLoaders
import ktx.assets.DisposableContainer
import ktx.assets.DisposableRegistry
import fr.mwet.snake.tuning.AssetTuning.LOADING_MILLIS_PER_FRAME

class AssetLoading : DisposableRegistry by DisposableContainer() {
    private val assetManager = AssetManager().apply { registerFontLoaders() }.alsoRegister()
    private val textures = TextureHandler(assetManager).alsoRegister()
    private val fonts = FontHandler(assetManager)
    private val sounds = SoundHandler(assetManager)
    private val music = MusicHandler(assetManager)

    val progress: Progress get() = Progress(assetManager.progress)

    fun update(): LoadedAssets? {
        if (!assetManager.update(LOADING_MILLIS_PER_FRAME)) return null
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
