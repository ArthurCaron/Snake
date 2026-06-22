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

fun Vector2.copyVector() = DI.vectorPool.copy(this)
