package fr.mwet.snake.render

import com.badlogic.gdx.graphics.Color

// Resolve colors when preferences change, never during a draw.
data class Appearance(
    val bodyHex: String = DEFAULT_BODY,
    val marksHex: String = DEFAULT_MARKS,
    val markShape: MarkShape = MarkShape.Diamond,
    val takesFruitColor: Boolean = false,
    val snakeGlow: Boolean = false,
    val fruitGlow: Boolean = false,
    val menuGlow: Boolean = false,
    val theme: VisualTheme = VisualTheme.Normal,
) {
    val body: Color = Color.valueOf(bodyHex)
    val marks: Color = Color.valueOf(marksHex)

    fun withTheme(theme: VisualTheme): Appearance {
        val defaults = theme.defaultAppearance()
        return copy(theme = theme, marksHex = defaults.marksHex, snakeGlow = defaults.snakeGlow,
            fruitGlow = defaults.fruitGlow, menuGlow = defaults.menuGlow)
    }

    companion object {
        const val DEFAULT_BODY = "3f9b92"
        const val DEFAULT_MARKS = "f5e2b0"

        fun colorHex(text: String): String? {
            val hex = text.trim().removePrefix("#")
            return hex.takeIf { it.length == 6 && it.all { c -> c in '0'..'9' || c.lowercaseChar() in 'a'..'f' } }?.lowercase()
        }
    }
}
