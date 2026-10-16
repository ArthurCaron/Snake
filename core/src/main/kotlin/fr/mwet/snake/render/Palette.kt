package fr.mwet.snake.render

import com.badlogic.gdx.graphics.Color

object Palette {
    val background: Color = Color.valueOf("1f1f1f")
    val accent: Color = Color.valueOf("25351f")
    val text: Color = Color.WHITE
    val border: Color = Color.WHITE
    val dim: Color = Color(0f, 0f, 0f, 0.6f)
    val lightDim: Color = Color(0f, 0f, 0f, 0.35f)
    val fontFill: Color = text
    val fontOutline: Color = accent
    val gridLine: Color = Color(1f, 1f, 1f, 0.05f)
}
