package fr.mwet.snake.tuning

import com.badlogic.gdx.graphics.Color

object Palette {
    val text: Color = Color.valueOf("fff3da")
    val clear: Color = Color.valueOf("8a4a32")
    val outline: Color = Color.valueOf("3b2220")
    val primary: Color = Color.valueOf("78d0c8")
    val surround: Color = Color.valueOf("b28979")
    val fontFill: Color = text
    val fontOutline: Color = outline
    val fontShadow: Color = outline
    val snakeShadow: Color = Color(outline).apply { a = 0.28f }
    val gridLine: Color = Color(1f, 1f, 1f, 0.1f)
}
