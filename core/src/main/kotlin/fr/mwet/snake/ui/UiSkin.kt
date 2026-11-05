package fr.mwet.snake.ui

import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion
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
import ktx.style.textField
import fr.mwet.snake.save.settings.SettingsRepository

object UiStyle {
    const val TITLE = "title"
    const val GAME_TITLE = "gameTitle"
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

fun createUiSkin(fonts: FontHandler, theme: FontTheme, textures: TextureHandler, settings: SettingsRepository): Skin = skin {
    val smallFont = fonts.font(theme, FontRole.Small, settings.appearance.theme)
    val bigFont = fonts.font(theme, FontRole.Big, settings.appearance.theme)
    val titleFont = fonts.font(theme, FontRole.SnekTitle, settings.appearance.theme)
    val ui = textures.skin
    val pressDepth = if (settings.appearance.theme.beveledButtons) BUTTON_PRESS_DEPTH else 0f
    val button = ui.button.toNinePatch()
    val buttonDown = GlowDrawable(ui.buttonDown.toNinePatch(), ui.glow, settings)
    val buttonOver = GlowDrawable(ui.buttonOver.toNinePatch(), ui.glow, settings)
    val primaryOver = GlowDrawable(ui.buttonPrimaryOver.toNinePatch(), ui.glow, settings)
    label {
        font = smallFont
        fontColor = Palette.label
    }
    label(UiStyle.TITLE) {
        font = bigFont
        fontColor = Palette.title
    }
    label(UiStyle.GAME_TITLE) {
        font = titleFont
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
        pressedOffsetY = -pressDepth
        font = smallFont
        fontColor = Palette.buttonLabel
    }
    textButton(UiStyle.BIG) {
        up = ui.buttonPrimary.toNinePatch()
        down = GlowDrawable(ui.buttonPrimaryDown.toNinePatch(), ui.glow, settings)
        over = primaryOver
        pressedOffsetY = -pressDepth
        font = bigFont
        fontColor = Palette.bigButtonLabel
    }
    textField {
        font = smallFont
        fontColor = Palette.label
        background = button
        cursor = textures.rectangleTexture(2, 32, Palette.label).toDrawable()
        selection = textures.rectangleTexture(1, 1, Palette.highlight).toDrawable()
    }
    // A key of the controls screen: the shape of the menu buttons in the primary color, pressed while it waits for a key.
    textButton(UiStyle.KEY) {
        up = ui.buttonPrimary.toNinePatch()
        down = GlowDrawable(ui.buttonPrimaryDown.toNinePatch(), ui.glow, settings)
        over = primaryOver
        pressedOffsetY = -pressDepth
        checked = GlowDrawable(ui.buttonPrimaryDown.toNinePatch(), ui.glow, settings)
        checkedOffsetY = -pressDepth
        font = smallFont
        fontColor = Palette.bigButtonLabel
    }
    imageButton(UiStyle.PAUSE) {
        up = button
        down = buttonDown
        over = buttonOver
        pressedOffsetY = -pressDepth
        imageUp = ui.iconPause.toDrawable()
    }
    imageButton(UiStyle.SOUND_ON) {
        up = button
        down = buttonDown
        over = buttonOver
        pressedOffsetY = -pressDepth
        imageUp = ui.iconSoundOn.toDrawable()
    }
    imageButton(UiStyle.SOUND_OFF) {
        up = button
        down = buttonDown
        over = buttonOver
        pressedOffsetY = -pressDepth
        imageUp = ui.iconSoundOff.toDrawable()
    }
    imageButton(UiStyle.MUSIC_ON) {
        up = button
        down = buttonDown
        over = buttonOver
        pressedOffsetY = -pressDepth
        imageUp = ui.iconMusicOn.toDrawable()
    }
    imageButton(UiStyle.MUSIC_OFF) {
        up = button
        down = buttonDown
        over = buttonOver
        pressedOffsetY = -pressDepth
        imageUp = ui.iconMusicOff.toDrawable()
    }
    for ((language, flag) in listOf(Language.English to ui.flagEnglish, Language.French to ui.flagFrench)) {
        imageButton(UiStyle.flag(language)) {
            up = button
            down = buttonDown
            over = buttonOver
            pressedOffsetY = -pressDepth
            imageUp = flag.toDrawable()
        }
    }
    add(UiStyle.PANEL, ui.panel.toNinePatch(), Drawable::class.java)
    add(UiStyle.DIM, textures.rectangleTexture(1, 1, Palette.pauseDim).toDrawable(), Drawable::class.java)
    add(UiStyle.LIGHT_DIM, textures.rectangleTexture(1, 1, Palette.readyDim).toDrawable(), Drawable::class.java)
}

private fun AtlasRegion.toNinePatch(): NinePatchDrawable {
    val split = findValue("split")!!
    return NinePatchDrawable(NinePatch(this, split[0], split[1], split[2], split[3]))
}
