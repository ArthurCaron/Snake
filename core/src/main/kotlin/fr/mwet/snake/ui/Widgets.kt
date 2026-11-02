package fr.mwet.snake.ui

import ktx.actors.onClick
import ktx.scene2d.KImageButton
import ktx.scene2d.KTextButton
import ktx.scene2d.KWidget
import ktx.scene2d.defaultStyle
import ktx.scene2d.imageButton
import ktx.scene2d.scene2d
import ktx.scene2d.textButton

private const val ICON_SIZE = 44f

class Widgets(private val onButtonActivated: () -> Unit) {
    fun <S> KWidget<S>.menuButton(text: String, onClick: () -> Unit, style: String = defaultStyle, init: KTextButton.(S) -> Unit = {}): KTextButton =
        textButton(text, style) { cell ->
            pad(8f, 24f, 14f, 24f)
            onClick { if (!isDisabled) activateButtonAction(onClick) }
            init(cell)
        }

    fun menuButton(text: String, onClick: () -> Unit, style: String = defaultStyle, init: KTextButton.() -> Unit = {}): KTextButton =
        scene2d.textButton(text, style) {
            pad(8f, 24f, 14f, 24f)
            onClick { if (!isDisabled) activateButtonAction(onClick) }
            init()
        }

    fun <S> KWidget<S>.iconButton(style: String, onClick: () -> Unit, init: KImageButton.(S) -> Unit = {}): KImageButton =
        imageButton(style) { cell ->
            pad(12f, 14f, 18f, 14f)
            imageCell.size(ICON_SIZE)
            onClick { if (!isDisabled) activateButtonAction(onClick) }
            init(cell)
        }

    fun activateButtonAction(action: () -> Unit) {
        action()
        onButtonActivated()
    }
}
