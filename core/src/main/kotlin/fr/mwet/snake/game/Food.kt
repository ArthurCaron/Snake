package fr.mwet.snake.game

import com.badlogic.gdx.math.Vector2
import fr.mwet.snake.DI
import fr.mwet.snake.utils.move

data class Food(val position: Vector2 = DI.vectorPool.obtain(-1, -1)) {
    fun reset(availableCells: Set<Cell>) {
        availableCells.randomOrNull()?.let { position.move(it.position) }
    }
}
