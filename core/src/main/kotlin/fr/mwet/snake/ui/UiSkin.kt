package fr.mwet.snake.ui

import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable
import fr.mwet.snake.assets.FontHandler
import fr.mwet.snake.assets.FontRole
import fr.mwet.snake.assets.TextureHandler
import fr.mwet.snake.assets.toDrawable
import fr.mwet.snake.save.settings.Language
import fr.mwet.snake.tuning.FontTheme
import fr.mwet.snake.tuning.Palette
import fr.mwet.snake.ui.UiMetrics.BUTTON_PRESS_DEPTH
import ktx.style.imageButton
import ktx.style.label
import ktx.style.skin
import ktx.style.textButton

object UiStyle {
    const val TITLE = "title"
    const val LOGO = "logo"
    const val HIGHLIGHT = "highlight"
    const val BIG = "big"
    const val KEY = "key"
    const val PANEL = "panel"
    const val DIM = "dim"
    const val LIGHT_DIM = "lightDim"
    const val PAUSE = "pause"
    const val SOUND_ON = "soundOn"
    const val SOUND_OFF = "soundOff"
    const val MUSIC_ON = "musicOn"
    const val MUSIC_OFF = "musicOff"

    fun flag(language: Language) = "flag${language.name}"
}

fun createUiSkin(fonts: FontHandler, theme: FontTheme, textures: TextureHandler): Skin = skin {
    val smallFont = fonts.font(theme, FontRole.Small)
    val bigFont = fonts.font(theme, FontRole.Big)
    val ui = textures.game.skin
    val button = ui.button.toNinePatch()
    val buttonDown = ui.buttonDown.toNinePatch()
    val buttonOver = ui.buttonOver.toNinePatch()
    val primaryOver = ui.buttonPrimaryOver.toNinePatch()
    label {
        font = smallFont
        fontColor = Palette.label
    }
    label(UiStyle.TITLE) {
        font = bigFont
        fontColor = Palette.title
    }
    label(UiStyle.HIGHLIGHT) {
        font = smallFont
        fontColor = Palette.highlight
    }
    textButton {
        up = button
        down = buttonDown
        over = buttonOver
        pressedOffsetY = -BUTTON_PRESS_DEPTH
        font = smallFont
        fontColor = Palette.buttonLabel
    }
    textButton(UiStyle.BIG) {
        up = ui.buttonPrimary.toNinePatch()
        down = ui.buttonPrimaryDown.toNinePatch()
        over = primaryOver
        pressedOffsetY = -BUTTON_PRESS_DEPTH
        font = bigFont
        fontColor = Palette.bigButtonLabel
    }
    // A key of the controls screen: the shape of the menu buttons in the primary color, pressed while it waits for a key.
    textButton(UiStyle.KEY) {
        up = ui.buttonPrimary.toNinePatch()
        down = ui.buttonPrimaryDown.toNinePatch()
        over = primaryOver
        pressedOffsetY = -BUTTON_PRESS_DEPTH
        checked = ui.buttonPrimaryDown.toNinePatch()
        checkedOffsetY = -BUTTON_PRESS_DEPTH
        font = smallFont
        fontColor = Palette.bigButtonLabel
    }
    imageButton(UiStyle.PAUSE) {
        up = button
        down = buttonDown
        over = buttonOver
        pressedOffsetY = -BUTTON_PRESS_DEPTH
        imageUp = ui.iconPause.toDrawable()
    }
    imageButton(UiStyle.SOUND_ON) {
        up = button
        down = buttonDown
        over = buttonOver
        pressedOffsetY = -BUTTON_PRESS_DEPTH
        imageUp = ui.iconSoundOn.toDrawable()
    }
    imageButton(UiStyle.SOUND_OFF) {
        up = button
        down = buttonDown
        over = buttonOver
        pressedOffsetY = -BUTTON_PRESS_DEPTH
        imageUp = ui.iconSoundOff.toDrawable()
    }
    imageButton(UiStyle.MUSIC_ON) {
        up = button
        down = buttonDown
        over = buttonOver
        pressedOffsetY = -BUTTON_PRESS_DEPTH
        imageUp = ui.iconMusicOn.toDrawable()
    }
    imageButton(UiStyle.MUSIC_OFF) {
        up = button
        down = buttonDown
        over = buttonOver
        pressedOffsetY = -BUTTON_PRESS_DEPTH
        imageUp = ui.iconMusicOff.toDrawable()
    }
    for ((language, flag) in listOf(Language.English to ui.flagEnglish, Language.French to ui.flagFrench)) {
        imageButton(UiStyle.flag(language)) {
            up = button
            down = buttonDown
            over = buttonOver
            pressedOffsetY = -BUTTON_PRESS_DEPTH
            imageUp = flag.toDrawable()
        }
    }
    add(UiStyle.LOGO, ui.logo, TextureRegion::class.java)
    add(UiStyle.PANEL, ui.panel.toNinePatch(), Drawable::class.java)
    add(UiStyle.DIM, textures.rectangleTexture(1, 1, Palette.pauseDim).toDrawable(), Drawable::class.java)
    add(UiStyle.LIGHT_DIM, textures.rectangleTexture(1, 1, Palette.readyDim).toDrawable(), Drawable::class.java)
}

private fun AtlasRegion.toNinePatch(): NinePatchDrawable {
    val split = findValue("split")!!
    return NinePatchDrawable(NinePatch(this, split[0], split[1], split[2], split[3]))
}
