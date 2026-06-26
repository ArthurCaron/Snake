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
    private val miscFolder = "misc"
    val cursor: AtlasRegion by lazy { textureAtlas.findRegion("$miscFolder/cursor") }

    // Menus
    private val mainMenuFolder = "mainMenu"
    val gameTitle: AtlasRegion by lazy { textureAtlas.findRegion("$mainMenuFolder/gameTitle") }
    val playBtn: AtlasRegion by lazy { textureAtlas.findRegion("$mainMenuFolder/playBtn") }
    val playBtnDown: AtlasRegion by lazy { textureAtlas.findRegion("$mainMenuFolder/playBtnDown") }

    // Game
    private val gameFolder = "game"
    val gridCell: AtlasRegion by lazy { textureAtlas.findRegion("$gameFolder/gridCell") }

    // Chloe
    private val chloeFruitFolder = "$gameFolder/chloe/fruit"
    private val chloeSnekFolder = "$gameFolder/chloe/snek"
    val strawberry: AtlasRegion by lazy { textureAtlas.findRegion("$chloeFruitFolder/strawberry") }
    val strawberryAnimation: Array<AtlasRegion> by lazy { textureAtlas.findRegions("$chloeFruitFolder/strawberryAnimation") }
    val snakeHead: AtlasRegion by lazy { textureAtlas.findRegion("$chloeSnekFolder/snakeHead") }
    val snakeHeadAnimation: Array<AtlasRegion> by lazy { textureAtlas.findRegions("$chloeSnekFolder/snakeHeadAnimation") }
    val snakeHeadFlippedAnimation: Array<AtlasRegion> by lazy {
        snakeHeadAnimation.map { AtlasRegion(it).apply { flip(false, true) } }.toGdxArray()
    }
    val snakeBody: AtlasRegion by lazy { textureAtlas.findRegion("$chloeSnekFolder/snakeBody") }
    val snakeTail: AtlasRegion by lazy { textureAtlas.findRegion("$chloeSnekFolder/snakeTail") }

    // Arthur
    private val arthurFruitFolder = "$gameFolder/arthur/fruit"
    private val arthurSnekFolder = "$gameFolder/arthur/snek"
    val snekBodyStraight: AtlasRegion by lazy { textureAtlas.findRegion("$arthurSnekFolder/snekBodyStraight") }
    val snekBodyTurn: AtlasRegion by lazy { textureAtlas.findRegion("$arthurSnekFolder/snekBodyTurn") }
    val snekHead: AtlasRegion by lazy { textureAtlas.findRegion("$arthurSnekFolder/snekHead") }
    val snekHeadAll: AtlasRegion by lazy { textureAtlas.findRegion("$arthurSnekFolder/snekHeadAll") }
    val snekTail: AtlasRegion by lazy { textureAtlas.findRegion("$arthurSnekFolder/snekTail") }
    val snekGoogly: Array<AtlasRegion> by lazy { textureAtlas.findRegions("$chloeFruitFolder/snekGoogly") }


    val background: Texture by lazy {
        val pm = Pixmap(1, 1, Format.RGBA8888)
        pm.setColor(BACKGROUND_COLOR)
        pm.fill()
        val backgroundTexture = Texture(pm).alsoRegister()
        pm.dispose()
        backgroundTexture
    }
}
