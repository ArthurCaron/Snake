package fr.mwet.snake.assets

import com.badlogic.gdx.assets.AssetDescriptor
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.assets.loaders.I18NBundleLoader.I18NBundleParameter
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.I18NBundle
import fr.mwet.snake.utils.I18N
import ktx.assets.DisposableContainer
import ktx.assets.DisposableRegistry
import ktx.assets.getValue
import ktx.assets.loadAsset
import java.util.*

val BACKGROUND_COLOR: Color = Color.valueOf("#1f1f1f")

class AssetHandler(private val assetManager: AssetManager) : DisposableRegistry by DisposableContainer() {

    fun loadAssets(): Progress {
        if (assetManager.update(16)) {
            println("Done loading")
            I18N.i18nBundle = t
            return Progress(1f)
        }

        return Progress(assetManager.progress)
    }

    private val assetsRoot = "snake/assets"
    private val soundsRoot = "sounds"

    // Sounds
    val switchSound: Sound by assetManager.loadAsset(AssetDescriptor("$soundsRoot/switch.mp3", Sound::class.java))
    val moveSound: Sound by assetManager.loadAsset(AssetDescriptor("$soundsRoot/snake_move.mp3", Sound::class.java))
    val eatSound: Sound by assetManager.loadAsset(AssetDescriptor("$soundsRoot/snake_eat.mp3", Sound::class.java))

    // Music
//    val music by assetManager.loadAsset(AssetDescriptor("$musicRoot/music.mp3", Music::class.java))

    // I18N
    // Should use this one but right now it's in french and my fonts don't handle accents
//    val t: I18NBundle by assetManager.loadAsset(AssetDescriptor(i18nBundle, I18NBundle::class.java))
    val t: I18NBundle by assetManager
        .loadAsset(AssetDescriptor("i18n/translations", I18NBundle::class.java, I18NBundleParameter(Locale.ENGLISH)))

    // Texture Atlas
    private val textureAtlas by assetManager.loadAsset(
        AssetDescriptor("game.atlas", TextureAtlas::class.java)
    )

    // Cursor
    val cursor: AtlasRegion by lazy { textureAtlas.findRegion("$assetsRoot/cursor") }

    // Menus
    val gameTitle: AtlasRegion by lazy { textureAtlas.findRegion("$assetsRoot/GameTitle") }
    val playBtn: AtlasRegion by lazy { textureAtlas.findRegion("$assetsRoot/PlayBtn") }
    val playBtnDown: AtlasRegion by lazy { textureAtlas.findRegion("$assetsRoot/PlayBtnDown") }

    // Gameplay
    val gridCell: AtlasRegion by lazy { textureAtlas.findRegion("$assetsRoot/GridCell") }
    val food: Array<AtlasRegion> by lazy { textureAtlas.findRegions("$assetsRoot/Food") }
    val snakeSegment: Array<AtlasRegion> by lazy { textureAtlas.findRegions("$assetsRoot/SnakeSegment") }

    val background: Texture by lazy { initBackground() }

    private fun initBackground(): Texture {
        val pm = Pixmap(1, 1, Pixmap.Format.RGBA8888)
        pm.setColor(BACKGROUND_COLOR)
        pm.fill()
        val backgroundTexture = Texture(pm)
        pm.dispose()
        return backgroundTexture
    }
}

@JvmInline
value class Progress(val value: Float)
