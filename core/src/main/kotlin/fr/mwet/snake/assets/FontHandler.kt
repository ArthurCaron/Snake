package fr.mwet.snake.assets

import com.badlogic.gdx.assets.AssetDescriptor
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver
import com.badlogic.gdx.graphics.Texture.TextureFilter
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGeneratorLoader
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader
import fr.mwet.snake.render.Palette
import fr.mwet.snake.tuning.FontTheme
import fr.mwet.snake.tuning.FontTuning.TEXT_CHARS
import fr.mwet.snake.tuning.FontTuning.TITLE_BORDER
import fr.mwet.snake.tuning.FontTuning.TITLE_CHARS
import fr.mwet.snake.tuning.FontTuning.TITLE_SIZE

class FontHandler(private val assetManager: AssetManager) {
    private val descriptors = FontTheme.entries.associateWith { theme ->
        FontRole.entries.associateWith { role -> descriptor(theme, role) }
    }

    init {
        descriptors.values.forEach { roles -> roles.values.forEach(assetManager::load) }
    }

    fun font(theme: FontTheme, role: FontRole): BitmapFont = assetManager.get(descriptors.getValue(theme).getValue(role))
}

private fun descriptor(theme: FontTheme, role: FontRole) = AssetDescriptor(
    "generated-fonts/${theme.tag}-${role.name.lowercase()}.ttf",
    BitmapFont::class.java,
    FreetypeFontLoader.FreeTypeFontLoaderParameter().apply {
        fontFileName = if (role == FontRole.SnekTitle) theme.titleFile else theme.textFile
        fontParameters.size = when (role) {
            FontRole.SnekTitle -> TITLE_SIZE
            FontRole.Big -> theme.bigSize
            FontRole.Small -> theme.smallSize
        }
        fontParameters.characters = if (role == FontRole.SnekTitle) TITLE_CHARS else TEXT_CHARS
        fontParameters.color = Palette.fontFill
        if (role == FontRole.SnekTitle) {
            fontParameters.borderWidth = TITLE_BORDER
            fontParameters.borderColor = Palette.fontOutline
        }
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
