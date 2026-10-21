package fr.mwet.snake.render

import com.badlogic.gdx.graphics.Color

object FruitLook {
    val fruits: Array<FruitStyle> = arrayOf(
        FruitStyle(FruitKind.Apple, Color.valueOf("e0443a")),
        FruitStyle(FruitKind.Strawberry, Color.valueOf("e8344a")),
        FruitStyle(FruitKind.Cherry, Color.valueOf("c0203f")),
        FruitStyle(FruitKind.Banana, Color.valueOf("f2cf3a")),
        FruitStyle(FruitKind.Grapes, Color.valueOf("8a4aa8")),
        FruitStyle(FruitKind.Orange, Color.valueOf("f28a1f")),
        FruitStyle(FruitKind.Pear, Color.valueOf("a8c43a")),
        FruitStyle(FruitKind.Lemon, Color.valueOf("f5e04a")),
        FruitStyle(FruitKind.Watermelon, Color.valueOf("f0485a")),
        FruitStyle(FruitKind.Prickly, Color.valueOf("d8467b")),
    )
    val glow = 0.0f
}
