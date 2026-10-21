package fr.mwet.snake.ui

import fr.mwet.snake.ui.UiMetrics.BUTTON_PAD_BOTTOM
import fr.mwet.snake.ui.UiMetrics.BUTTON_PAD_SIDE
import fr.mwet.snake.ui.UiMetrics.BUTTON_PAD_TOP
import fr.mwet.snake.ui.UiMetrics.ICON_BUTTON_PAD_BOTTOM
import fr.mwet.snake.ui.UiMetrics.ICON_BUTTON_PAD_SIDE
import fr.mwet.snake.ui.UiMetrics.ICON_BUTTON_PAD_TOP
import fr.mwet.snake.ui.UiMetrics.ICON_SIZE
import ktx.actors.onClick
import ktx.scene2d.KImageButton
import ktx.scene2d.KTextButton
import ktx.scene2d.KWidget
import ktx.scene2d.defaultStyle
import ktx.scene2d.imageButton
import ktx.scene2d.scene2d
import ktx.scene2d.textButton

class Widgets(private val onButtonActivated: () -> Unit) {
    fun <S> KWidget<S>.menuButton(text: String, onClick: () -> Unit, style: String = defaultStyle, init: KTextButton.(S) -> Unit = {}): KTextButton =
        textButton(text, style) { cell ->
            pad(BUTTON_PAD_TOP, BUTTON_PAD_SIDE, BUTTON_PAD_BOTTOM, BUTTON_PAD_SIDE)
            onClick { if (!isDisabled) activateButtonAction(onClick) }
            init(cell)
        }

    fun menuButton(text: String, onClick: () -> Unit, style: String = defaultStyle, init: KTextButton.() -> Unit = {}): KTextButton =
        scene2d.textButton(text, style) {
            pad(BUTTON_PAD_TOP, BUTTON_PAD_SIDE, BUTTON_PAD_BOTTOM, BUTTON_PAD_SIDE)
            onClick { if (!isDisabled) activateButtonAction(onClick) }
            init()
        }

    fun <S> KWidget<S>.iconButton(style: String, onClick: () -> Unit, init: KImageButton.(S) -> Unit = {}): KImageButton =
        imageButton(style) { cell ->
            pad(ICON_BUTTON_PAD_TOP, ICON_BUTTON_PAD_SIDE, ICON_BUTTON_PAD_BOTTOM, ICON_BUTTON_PAD_SIDE)
            imageCell.size(ICON_SIZE)
            onClick { if (!isDisabled) activateButtonAction(onClick) }
            init(cell)
        }

    fun activateButtonAction(action: () -> Unit) {
        action()
        onButtonActivated()
    }
}
