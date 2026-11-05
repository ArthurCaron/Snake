package fr.mwet.snake.ui

import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.scenes.scene2d.ui.TextField

// Include the full text, the skin padding, and room for the caret.
fun TextField.widthForCharacters(characters: String, count: Int): Float {
    val layout = GlyphLayout()
    var textWidth = 0f
    for (character in characters) {
        layout.setText(style.font, character.toString().repeat(count))
        textWidth = maxOf(textWidth, layout.width)
    }
    val background = style.background
    val padding = (background?.leftWidth ?: 0f) + (background?.rightWidth ?: 0f)
    return maxOf(background?.minWidth ?: 0f, textWidth + padding + (style.cursor?.minWidth ?: 0f) + UiMetrics.UNIT * 2)
}
