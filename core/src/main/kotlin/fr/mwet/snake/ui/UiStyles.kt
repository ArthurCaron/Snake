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
    val label = Label.LabelStyle(fontHandler.uiFont, Palette.text)
    val title = Label.LabelStyle(fontHandler.buttonFont, Palette.text)

    val panel = textureHandler.ninePatch(fill = Palette.background)
    val dim = textureHandler.rectangleTexture(1, 1, Palette.dim).toDrawable()

    val button = TextButton.TextButtonStyle().apply {
        up = textureHandler.ninePatch(fill = Palette.background)
        down = textureHandler.ninePatch(fill = Palette.accent)
        font = fontHandler.uiFont
        fontColor = Palette.text
    }
}

private fun TextureHandler.ninePatch(fill: Color): NinePatchDrawable {
    val texture =
        rectangleWithBorderTexture(width = 24, height = 24, border = 4, fill = fill, borderColor = Palette.border)
    return NinePatchDrawable(NinePatch(texture, 8, 8, 8, 8))
}
