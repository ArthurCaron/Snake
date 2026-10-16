package fr.mwet.snake.tuning

import com.badlogic.gdx.graphics.Color
import fr.mwet.snake.render.MarkShape

object Palette {
    val text: Color = Color.valueOf("fff3da")
    val clear: Color = Color.valueOf("8a4a32")
    val outline: Color = Color.valueOf("3b2220")
    val primary: Color = Color.valueOf("78d0c8")
    val surround: Color = Color.valueOf("b28979")
    val snake: Color = Color.valueOf("3f9b92")
    val marks: Color = Color.valueOf("f5e2b0")
    val markShape = MarkShape.Diamond
    val fruits: Array<Color> = arrayOf(Color.valueOf("d8467b"))
    val snakeTakesFruitColor = false
    val snakeGlow = 0.0f
    val fruitGlow = 0.0f
    val fontFill: Color = text
    val fontOutline: Color = outline
    val fontShadow: Color = outline
    val snakeShadow: Color = Color(outline).apply { a = 0.28f }
    val gridLine: Color = Color(1f, 1f, 1f, 0.1f)
}
