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
import fr.mwet.snake.generated.assets.TextureRegions
import ktx.assets.DisposableContainer
import ktx.assets.DisposableRegistry
import ktx.assets.getValue
import ktx.assets.loadAsset
import ktx.collections.toGdxArray

class TextureHandler(assetManager: AssetManager) : DisposableRegistry by DisposableContainer() {
    private val textureAtlas by assetManager.loadAsset(
        AssetDescriptor("textures/textures.atlas", TextureAtlas::class.java)
    )

    val gridCell: AtlasRegion by lazy { textureAtlas.findRegion(TextureRegions.Game.GridCell) }

    val strawberry: AtlasRegion by lazy { textureAtlas.findRegion(TextureRegions.Game.Chloe.Fruit.Strawberry) }
    val strawberryAnimation: Array<AtlasRegion> by lazy {
        textureAtlas.findRegions(TextureRegions.Game.Chloe.Fruit.StrawberryAnimation)
    }

    val snakeHead: AtlasRegion by lazy { textureAtlas.findRegion(TextureRegions.Game.Chloe.Snek.SnakeHead) }
    val snakeHeadAnimation: Array<AtlasRegion> by lazy {
        textureAtlas.findRegions(TextureRegions.Game.Chloe.Snek.SnakeHeadAnimation)
    }
    val snakeHeadFlippedAnimation: Array<AtlasRegion> by lazy {
        snakeHeadAnimation.map { AtlasRegion(it).apply { flip(false, true) } }.toGdxArray()
    }
    val snakeBody: AtlasRegion by lazy { textureAtlas.findRegion(TextureRegions.Game.Chloe.Snek.SnakeBody) }
    val snakeTail: AtlasRegion by lazy { textureAtlas.findRegion(TextureRegions.Game.Chloe.Snek.SnakeTail) }

    val snekBodyStraight: AtlasRegion by lazy { textureAtlas.findRegion(TextureRegions.Game.Arthur.Snek.SnekBodyStraight) }
    val snekBodyTurn: AtlasRegion by lazy { textureAtlas.findRegion(TextureRegions.Game.Arthur.Snek.SnekBodyTurn) }
    val snekHead: AtlasRegion by lazy { textureAtlas.findRegion(TextureRegions.Game.Arthur.Snek.SnekHead) }
    val snekHeadAll: AtlasRegion by lazy { textureAtlas.findRegion(TextureRegions.Game.Arthur.Snek.SnekHeadAll) }
    val snekTail: AtlasRegion by lazy { textureAtlas.findRegion(TextureRegions.Game.Arthur.Snek.SnekTail) }
    val snekGoogly: Array<AtlasRegion> by lazy { textureAtlas.findRegions(TextureRegions.Game.Arthur.Snek.SnekGoogly) }

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
