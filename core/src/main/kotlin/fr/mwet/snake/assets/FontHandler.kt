package fr.mwet.snake.assets

import com.badlogic.gdx.assets.AssetDescriptor
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver
import com.badlogic.gdx.graphics.Texture.TextureFilter
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGeneratorLoader
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader
import fr.mwet.snake.tuning.FontTheme
import fr.mwet.snake.tuning.FontTuning.MIN_OUTLINE
import fr.mwet.snake.tuning.FontTuning.MIN_SHADOW
import fr.mwet.snake.tuning.FontTuning.OUTLINE_RATIO
import fr.mwet.snake.tuning.FontTuning.SHADOW_RATIO
import fr.mwet.snake.tuning.FontTuning.TEXT_CHARS
import fr.mwet.snake.tuning.FontTuning.TITLE_BORDER
import fr.mwet.snake.tuning.FontTuning.TITLE_CHARS
import fr.mwet.snake.tuning.FontTuning.TITLE_SIZE
import fr.mwet.snake.render.VisualTheme

class FontHandler(private val assetManager: AssetManager) {
    private val descriptors = VisualTheme.entries.associateWith { visual ->
        FontTheme.entries.associateWith { theme ->
            FontRole.entries.associateWith { role -> descriptor(theme, role, visual) }
        }
    }

    init {
        descriptors.values.forEach { themes -> themes.values.forEach { roles -> roles.values.forEach(assetManager::load) } }
    }

    fun font(theme: FontTheme, role: FontRole, visual: VisualTheme): BitmapFont =
        assetManager.get(descriptors.getValue(visual).getValue(theme).getValue(role))
}

private fun descriptor(theme: FontTheme, role: FontRole, visual: VisualTheme) = AssetDescriptor(
    "generated-fonts/${visual.tag}-${theme.tag}-${role.name.lowercase()}.ttf",
    BitmapFont::class.java,
    FreetypeFontLoader.FreeTypeFontLoaderParameter().apply {
        fontFileName = if (role == FontRole.SnekTitle) theme.titleFile else theme.textFile
        fontParameters.size = when (role) {
            FontRole.SnekTitle -> TITLE_SIZE
            FontRole.Big -> theme.bigSize
            FontRole.Small -> theme.smallSize
        }
        fontParameters.characters = if (role == FontRole.SnekTitle) TITLE_CHARS else TEXT_CHARS
        fontParameters.color = visual.text
        val outline = if (role == FontRole.SnekTitle) TITLE_BORDER.toInt() else maxOf(MIN_OUTLINE, fontParameters.size / OUTLINE_RATIO)
        fontParameters.borderWidth = outline.toFloat()
        fontParameters.borderColor = visual.outline
        // Thinner than the outline: a thick shadow weighs the text down on its button.
        val shadow = if (role == FontRole.SnekTitle) outline else maxOf(MIN_SHADOW, fontParameters.size / SHADOW_RATIO)
        fontParameters.shadowOffsetY = shadow
        fontParameters.shadowOffsetX = shadow
        fontParameters.shadowColor = visual.outline
        fontParameters.mono = theme.pixel
        val filter = if (theme.pixel) TextureFilter.Nearest else TextureFilter.Linear
        fontParameters.minFilter = filter
        fontParameters.magFilter = filter
    },
)

fun AssetManager.registerFontLoaders() {
    val resolver = InternalFileHandleResolver()

    setLoader(
        FreeTypeFontGenerator::class.java,
        FreeTypeFontGeneratorLoader(resolver)
    )

    setLoader(
        BitmapFont::class.java,
        ".ttf",
        FreetypeFontLoader(resolver)
    )
}
