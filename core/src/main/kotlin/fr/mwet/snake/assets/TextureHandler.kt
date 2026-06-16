package fr.mwet.snake.assets

import com.badlogic.gdx.assets.AssetDescriptor
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Pixmap.Format
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion
import com.badlogic.gdx.utils.Array
import ktx.assets.DisposableContainer
import ktx.assets.DisposableRegistry
import ktx.assets.getValue
import ktx.assets.loadAsset
import ktx.collections.toGdxArray

private const val TEXTURES_ATLAS_ROOT = "textures/textures.atlas"
private val BACKGROUND_COLOR = Color.valueOf("#1f1f1f")

class TextureHandler(assetManager: AssetManager) : DisposableRegistry by DisposableContainer() {
    // Texture Atlas
    private val textureAtlas by assetManager.loadAsset(
        AssetDescriptor(TEXTURES_ATLAS_ROOT, TextureAtlas::class.java)
    )

    // Cursor
    val cursor: AtlasRegion by lazy { textureAtlas.findRegion("cursor") }

    // Menus
    val gameTitle: AtlasRegion by lazy { textureAtlas.findRegion("GameTitle") }
    val playBtn: AtlasRegion by lazy { textureAtlas.findRegion("PlayBtn") }
    val playBtnDown: AtlasRegion by lazy { textureAtlas.findRegion("PlayBtnDown") }

    // Gameplay
    val gridCell: AtlasRegion by lazy { textureAtlas.findRegion("GridCell") }
    val foodAnimation: Array<AtlasRegion> by lazy { textureAtlas.findRegions("Food") }
    val snakeSegmentAnimation: Array<AtlasRegion> by lazy { textureAtlas.findRegions("SnakeSegment") }

    val strawberry: AtlasRegion by lazy { textureAtlas.findRegion("Strawberry") }
    val strawberryAnimation: Array<AtlasRegion> by lazy { textureAtlas.findRegions("StrawberryAnimation") }
    val snakeHead: AtlasRegion by lazy { textureAtlas.findRegion("SnakeHead") }
    val snakeHeadAnimation: Array<AtlasRegion> by lazy { textureAtlas.findRegions("SnakeHeadAnimation") }
    val snakeHeadFlippedAnimation: Array<AtlasRegion> by lazy {
        snakeHeadAnimation.map { AtlasRegion(it).apply { flip(false, true) } }.toGdxArray()
    }
    val snakeBody: AtlasRegion by lazy { textureAtlas.findRegion("SnakeBody") }
    val snakeTail: AtlasRegion by lazy { textureAtlas.findRegion("SnakeTail") }

    val background: Texture by lazy {
        val pm = Pixmap(1, 1, Format.RGBA8888)
        pm.setColor(BACKGROUND_COLOR)
        pm.fill()
        val backgroundTexture = Texture(pm)
        pm.dispose()
        backgroundTexture
    }
}
