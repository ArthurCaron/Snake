package fr.mwet.snake.render

import com.badlogic.gdx.graphics.Color

enum class VisualTheme(
    val tag: String,
    val assetFolder: String,
    clear: String, outline: String, text: String, primary: String, surround: String,
    val snakeGlowAlpha: Float,
    val fruitGlowAlpha: Float,
    val beveledButtons: Boolean,
) {
    Desert("desert", "skin", "8a4a32", "3b2220", "fff3da", "78d0c8", "b28979", 0.35f, 0.4f, true),
    Laser("laser", "laser", "05030b", "05030b", "ffffff", "ffa6e6", "5c5b60", 0.44f, 0.8f, false);

    val clear = Color.valueOf(clear)
    val outline = Color.valueOf(outline)
    val text = Color.valueOf(text)
    val primary = Color.valueOf(primary)
    val surround = Color.valueOf(surround)

    val fruitColors: List<Color> = when (tag) {
        "laser" -> listOf("ff3f9a", "22f2ff", "b6ff3e", "ffd23f", "a46bff", "ff7a1f", "ff3f9a", "22f2ff", "b6ff3e", "ffd23f").map(Color::valueOf)
        else -> FruitLook.fruits.map { it.color }
    }

    fun defaultAppearance(): Appearance = when (this) {
        Desert -> Appearance()
        Laser -> Appearance(bodyHex = "22f2ff", marksHex = "ffffff", takesFruitColor = true,
            snakeGlow = true, fruitGlow = true, menuGlow = true, theme = Laser)
    }

    companion object {
        fun fromTag(tag: String): VisualTheme = entries.firstOrNull { it.tag == tag } ?: Desert
    }
}
