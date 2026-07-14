package fr.mwet.snake.assets

import com.badlogic.gdx.assets.AssetDescriptor
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Pixmap.Format
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Array
import ktx.assets.DisposableContainer
import ktx.assets.DisposableRegistry
import ktx.assets.getValue
import ktx.assets.loadAsset
import ktx.collections.toGdxArray

private const val TEXTURES_ATLAS_ROOT = "textures/textures.atlas"

class TextureHandler(assetManager: AssetManager) : DisposableRegistry by DisposableContainer() {
    // Texture Atlas
    private val textureAtlas by assetManager.loadAsset(
        AssetDescriptor(TEXTURES_ATLAS_ROOT, TextureAtlas::class.java)
    )

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

    fun rectangleTexture(width: Float, height: Float, fill: Color = Color.BLACK) =
        rectangleTexture(width.toInt(), height.toInt(), fill)

    fun rectangleTexture(width: Int, height: Int, fill: Color = Color.BLACK) =
        rectangleWithBorderTexture(width = width, height = height, fill = fill)

    fun rectangleWithBorderTexture(
        width: Float,
        height: Float,
        border: Int = 0,
        fill: Color = Color.BLACK,
        borderColor: Color = Color.BLACK
    ) = rectangleWithBorderTexture(width.toInt(), height.toInt(), border, fill, borderColor)

    fun rectangleWithBorderTexture(
        width: Int,
        height: Int,
        border: Int = 0,
        fill: Color = Color.BLACK,
        borderColor: Color = Color.BLACK,
    ): Texture {
        val pixmap = Pixmap(width, height, Format.RGBA8888)

        pixmap.setColor(borderColor)
        pixmap.fillRectangle(0, 0, width, height)

        pixmap.setColor(fill)
        pixmap.fillRectangle(
            border,
            border,
            width - border * 2,
            height - border * 2,
        )

        val texture = Texture(pixmap).alsoRegister()
        pixmap.dispose()

        return texture
    }
}

fun Texture.toDrawable() = TextureRegionDrawable(TextureRegion(this))

fun TextureRegion.toDrawable() = TextureRegionDrawable(this)
