package fr.mwet.snake.utils

import com.badlogic.gdx.math.Vector2
import fr.mwet.snake.DI
import ktx.assets.pool

class VectorPool {
    private val vectorPool = pool { Vector2(Vector2.Zero) }

    fun obtain(vector: Vector2): Vector2 = obtain(vector.x, vector.y)

    fun obtain(x: Int, y: Int): Vector2 = obtain(x.toFloat(), y.toFloat())

    fun obtain(x: Float, y: Float): Vector2 = vectorPool.obtain().apply {
        this.x = x
        this.y = y
    }

    fun free(vector: Vector2) = vectorPool.free(vector)

    fun copy(vector: Vector2) = obtain(vector)
}

fun Vector2.free() = DI.vectorPool.free(this)

fun Vector2.obtain(otherVector: Vector2) = obtain(otherVector.x, otherVector.y)

fun Vector2.obtain(x: Float, y: Float): Vector2 {
    val newVector2 = DI.vectorPool.obtain(x, y)
    free()
    return newVector2
}

fun Vector2.copyVector() = DI.vectorPool.copy(this)

fun Vector2.collidesWith(otherPosition: Vector2): Boolean =
    x == otherPosition.x && y == otherPosition.y
