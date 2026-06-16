package fr.mwet.snake.entities

import fr.mwet.snake.utils.WORLD_HEIGHT
import fr.mwet.snake.utils.WORLD_WIDTH
import kotlin.random.Random

class Food {
    var x = 0
    var y = 0

    fun newFood(snake: Snake) {
        randomizePosition()
        while (snake.collides(x, y)) {
            randomizePosition()
        }
    }

    private fun randomizePosition() {
        x = Random.nextInt(0, WORLD_WIDTH.toInt())
        y = Random.nextInt(0, WORLD_HEIGHT.toInt())
    }
}
