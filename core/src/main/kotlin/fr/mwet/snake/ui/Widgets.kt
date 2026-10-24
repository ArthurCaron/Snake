package fr.mwet.snake.ui

import ktx.actors.onClick
import ktx.scene2d.KTextButton
import ktx.scene2d.KWidget
import ktx.scene2d.defaultStyle
import ktx.scene2d.scene2d
import ktx.scene2d.textButton

class Widgets(private val onButtonActivated: () -> Unit) {
    fun <S> KWidget<S>.menuButton(text: String, onClick: () -> Unit, style: String = defaultStyle, init: KTextButton.(S) -> Unit = {}): KTextButton =
        textButton(text, style) { cell ->
            pad(8f, 24f, 8f, 24f)
            onClick { if (!isDisabled) activateButtonAction(onClick) }
            init(cell)
        }

    fun menuButton(text: String, onClick: () -> Unit, style: String = defaultStyle, init: KTextButton.() -> Unit = {}): KTextButton =
        scene2d.textButton(text, style) {
            pad(8f, 24f, 8f, 24f)
            onClick { if (!isDisabled) activateButtonAction(onClick) }
            init()
        }

    fun activateButtonAction(action: () -> Unit) {
        action()
        onButtonActivated()
    }
}
