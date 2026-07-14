package fr.mwet.snake.assets

import com.badlogic.gdx.assets.AssetDescriptor
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGeneratorLoader
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader
import fr.mwet.snake.generated.assets.FontAssets
import ktx.assets.DisposableContainer
import ktx.assets.DisposableRegistry
import ktx.assets.getValue
import ktx.assets.loadAsset

private const val CHEWY_FILE = FontAssets.Chewy.ChewyRegular
private const val FREDOKA_FILE = FontAssets.Fredoka.FredokaRegular

private const val TITLE_FONT_ASSET = "generated-fonts/title-chewy-160.ttf"
private const val UI_FONT_ASSET = "generated-fonts/ui-fredoka-36.ttf"
private const val BUTTON_FONT_ASSET = "generated-fonts/button-fredoka-60.ttf"

private const val FRENCH_CHARS = "ÀÂÄÇÉÈÊËÎÏÔÖÙÛÜŸàâäçéèêëîïôöùûüÿœŒæÆ"

class FontHandler(assetManager: AssetManager) : DisposableRegistry by DisposableContainer() {
    private val characters = FreeTypeFontGenerator.DEFAULT_CHARS + FRENCH_CHARS

    val titleFont: BitmapFont by assetManager.loadAsset(
        AssetDescriptor(
            TITLE_FONT_ASSET,
            BitmapFont::class.java,
            fontParameters(
                fontFile = CHEWY_FILE,
                size = 160,
                characters = "Snek",
                borderWidth = 5f,
            )
        )
    )

    val uiFont: BitmapFont by assetManager.loadAsset(
        AssetDescriptor(
            UI_FONT_ASSET,
            BitmapFont::class.java,
            fontParameters(
                fontFile = FREDOKA_FILE,
                size = 36,
                characters = characters,
            )
        )
    )

    val buttonFont: BitmapFont by assetManager.loadAsset(
        AssetDescriptor(
            BUTTON_FONT_ASSET,
            BitmapFont::class.java,
            fontParameters(
                fontFile = FREDOKA_FILE,
                size = 60,
                characters = characters,
            )
        )
    )
}

private fun fontParameters(
    fontFile: String,
    size: Int,
    characters: String,
    borderWidth: Float = 0f,
) = FreetypeFontLoader.FreeTypeFontLoaderParameter().apply {
    fontFileName = fontFile
    fontParameters.size = size
    fontParameters.characters = characters
    fontParameters.color = Color.WHITE
    fontParameters.borderWidth = borderWidth
    fontParameters.borderColor = Color.valueOf("25351f")
    fontParameters.minFilter = Texture.TextureFilter.Linear
    fontParameters.magFilter = Texture.TextureFilter.Linear
}

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
