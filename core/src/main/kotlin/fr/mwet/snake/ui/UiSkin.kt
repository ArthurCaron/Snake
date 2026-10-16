package fr.mwet.snake.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable
import fr.mwet.snake.assets.FontHandler
import fr.mwet.snake.assets.FontRole
import fr.mwet.snake.tuning.FontTheme
import fr.mwet.snake.assets.TextureHandler
import fr.mwet.snake.assets.toDrawable
import fr.mwet.snake.render.Palette
import ktx.style.label
import ktx.style.skin
import ktx.style.textButton

object UiStyle {
    const val TITLE = "title"
    const val LOGO = "logo"
    const val BIG = "big"
    const val PANEL = "panel"
    const val DIM = "dim"
    const val LIGHT_DIM = "lightDim"
}

fun createUiSkin(fonts: FontHandler, theme: FontTheme, textures: TextureHandler): Skin = skin {
    val smallFont = fonts.font(theme, FontRole.Small)
    val bigFont = fonts.font(theme, FontRole.Big)
    val snekTitleFont = fonts.font(theme, FontRole.SnekTitle)
    label {
        font = smallFont
        fontColor = Palette.text
    }
    label(UiStyle.TITLE) {
        font = bigFont
        fontColor = Palette.text
    }
    label(UiStyle.LOGO) {
        font = snekTitleFont
        fontColor = Palette.text
    }
    textButton {
        up = textures.ninePatch(Palette.background)
        down = textures.ninePatch(Palette.accent)
        font = smallFont
        fontColor = Palette.text
    }
    textButton(UiStyle.BIG) {
        up = textures.rectangleWithBorderTexture(256, 96, border = 4, fill = Palette.background, borderColor = Palette.border).toDrawable()
        down = textures.rectangleWithBorderTexture(256, 96, border = 4, fill = Palette.accent, borderColor = Palette.border).toDrawable()
        font = bigFont
        fontColor = Palette.text
    }
    add(UiStyle.PANEL, textures.ninePatch(Palette.background), Drawable::class.java)
    add(UiStyle.DIM, textures.rectangleTexture(1, 1, Palette.dim).toDrawable(), Drawable::class.java)
    add(UiStyle.LIGHT_DIM, textures.rectangleTexture(1, 1, Palette.lightDim).toDrawable(), Drawable::class.java)
}

private fun TextureHandler.ninePatch(fill: Color): NinePatchDrawable {
    val texture = rectangleWithBorderTexture(width = 24, height = 24, border = 4, fill = fill, borderColor = Palette.border)
    return NinePatchDrawable(NinePatch(texture, 8, 8, 8, 8))
}
