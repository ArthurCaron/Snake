package fr.mwet.snake.ui

import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import ktx.actors.onClick

fun UiStyles.textButton(text: String, onClick: () -> Unit) = button.textButton(text, onClick)

fun TextButton.TextButtonStyle.textButton(text: String, onClick: () -> Unit) = TextButton(text, this).apply {
    pad(8f, 24f, 8f, 24f)
    onClick { onClick() }
}
