package fr.mwet.snake.game

import com.badlogic.gdx.math.Vector2
import fr.mwet.snake.DI
import fr.mwet.snake.utils.copyVector

data class Food(var position: Vector2 = DI.vectorPool.obtain(0, 0)) {
    fun reset(availableCells: Set<Cell>) {
        val randomCell = availableCells.randomOrNull()
        position = randomCell?.position?.copyVector() ?: Vector2.Zero
    }
}
