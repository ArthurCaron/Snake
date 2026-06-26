package fr.mwet.snake.utils

import com.badlogic.gdx.math.Vector2

enum class Direction(val directionX: Int, val directionY: Int) {
    UP(0, 1),
    RIGHT(1, 0),
    DOWN(0, -1),
    LEFT(-1, 0);

    fun isOpposite(direction: Direction): Boolean {
        if (this == DOWN && direction == UP) return true
        if (this == UP && direction == DOWN) return true
        if (this == RIGHT && direction == LEFT) return true
        if (this == LEFT && direction == RIGHT) return true
        return false
    }
}

fun Vector2.move(direction: Direction) = move(x + direction.directionX, y + direction.directionY)
fun Vector2.move(position: Vector2) = move(position.x, position.y)
fun Vector2.move(x: Int, y: Int) = move(x.toFloat(), y.toFloat())
fun Vector2.move(x: Float, y: Float): Vector2 {
    this.x = x
    this.y = y
    return this
}
