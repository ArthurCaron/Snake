package fr.mwet.snake.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable
import fr.mwet.snake.assets.FontHandler
import fr.mwet.snake.assets.TextureHandler
import fr.mwet.snake.assets.toDrawable
import fr.mwet.snake.render.Palette

class UiStyles(fontHandler: FontHandler, textureHandler: TextureHandler) {
    val mainTitle = Label.LabelStyle(fontHandler.snekTitleFont, Palette.text)
    val title = Label.LabelStyle(fontHandler.bigFont, Palette.text)
    val label = Label.LabelStyle(fontHandler.smallFont, Palette.text)

    val panel = textureHandler.ninePatch(fill = Palette.background)
    val dim = textureHandler.rectangleTexture(1, 1, Palette.dim).toDrawable()
    val lightDim = textureHandler.rectangleTexture(1, 1, Palette.lightDim).toDrawable()

    val playButton = TextButton.TextButtonStyle().apply {
        up = textureHandler.rectangleWithBorderTexture(
            width = 256,
            height = 96,
            border = 4,
            fill = Palette.background,
            borderColor = Palette.border,
        ).toDrawable()

        down = textureHandler.rectangleWithBorderTexture(
            width = 256,
            height = 96,
            border = 4,
            fill = Palette.accent,
            borderColor = Palette.border,
        ).toDrawable()

        font = fontHandler.bigFont
        fontColor = Palette.text
        downFontColor = Palette.text
    }

    val button = TextButton.TextButtonStyle().apply {
        up = textureHandler.ninePatch(fill = Palette.background)
        down = textureHandler.ninePatch(fill = Palette.accent)
        font = fontHandler.smallFont
        fontColor = Palette.text
    }
}

private fun TextureHandler.ninePatch(fill: Color): NinePatchDrawable {
    val texture =
        rectangleWithBorderTexture(width = 24, height = 24, border = 4, fill = fill, borderColor = Palette.border)
    return NinePatchDrawable(NinePatch(texture, 8, 8, 8, 8))
}
