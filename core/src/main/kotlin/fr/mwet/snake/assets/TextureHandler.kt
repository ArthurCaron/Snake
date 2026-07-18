package fr.mwet.snake.assets

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Pixmap.Format
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Array
import fr.mwet.snake.generated.assets.TextureAssets
import ktx.assets.DisposableContainer
import ktx.assets.DisposableRegistry
import ktx.collections.toGdxArray

class TextureHandler(assetManager: AssetManager) :
    TextureAssets(assetManager),
    DisposableRegistry by DisposableContainer() {
    val snakeHeadFlippedAnimation: Array<AtlasRegion> by lazy {
        snakeHeadAnimation.map { AtlasRegion(it).apply { flip(false, true) } }.toGdxArray()
    }

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
