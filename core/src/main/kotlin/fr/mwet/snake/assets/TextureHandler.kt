package fr.mwet.snake.assets

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Pixmap.Format
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import fr.mwet.snake.generated.assets.TextureAssets
import ktx.assets.DisposableContainer
import ktx.assets.DisposableRegistry

class TextureHandler(assetManager: AssetManager) :
    TextureAssets(assetManager),
    DisposableRegistry by DisposableContainer() {
    private val rectangles = HashMap<RectangleKey, Texture>()

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
        val key = RectangleKey(width, height, border, Color(fill), Color(borderColor))
        return rectangles.getOrPut(key) {
            createRectangle(key)
        }
    }

    private fun createRectangle(key: RectangleKey): Texture {
        val pixmap = Pixmap(key.width, key.height, Format.RGBA8888)

        pixmap.setColor(key.borderColor)
        pixmap.fillRectangle(0, 0, key.width, key.height)

        pixmap.setColor(key.fill)
        pixmap.fillRectangle(
            key.border,
            key.border,
            key.width - key.border * 2,
            key.height - key.border * 2,
        )

        val texture = Texture(pixmap).alsoRegister()
        pixmap.dispose()

        return texture
    }
}

private data class RectangleKey(
    val width: Int,
    val height: Int,
    val border: Int,
    val fill: Color,
    val borderColor: Color
)

fun Texture.toDrawable() = TextureRegionDrawable(TextureRegion(this))

fun TextureRegion.toDrawable() = TextureRegionDrawable(this)
