package fr.mwet.snake.ui

import ktx.actors.onClick
import ktx.scene2d.KTextButton
import ktx.scene2d.KWidget
import ktx.scene2d.scene2d
import ktx.scene2d.textButton

fun <S> KWidget<S>.menuButton(text: String, onClick: () -> Unit, init: KTextButton.(S) -> Unit = {}): KTextButton =
    textButton(text) { cell ->
        pad(8f, 24f, 8f, 24f)
        onClick { onClick() }
        init(cell)
    }

fun menuButton(text: String, onClick: () -> Unit): KTextButton =
    scene2d.textButton(text) {
        pad(8f, 24f, 8f, 24f)
        onClick { onClick() }
    }
