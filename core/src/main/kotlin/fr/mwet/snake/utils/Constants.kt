package fr.mwet.snake.utils

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector2

const val WORLD_WIDTH = 16f
const val WORLD_HEIGHT = 20f

fun SpriteBatch.resetColor() = setColor(1f, 1f, 1f, 1f)

fun SpriteBatch.resetColor(alpha: Float) = setColor(1f, 1f, 1f, alpha)

fun SpriteBatch.draw(texture: Texture, position: Vector2, size: Vector2) =
    draw(texture, position.x, position.y, size.x, size.y)
