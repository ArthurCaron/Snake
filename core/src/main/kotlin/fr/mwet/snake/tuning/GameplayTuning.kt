package fr.mwet.snake.tuning

import fr.mwet.snake.utils.Direction

object GameplayTuning {
    const val WORLD_WIDTH = 16f
    const val WORLD_HEIGHT = 20f

    const val START_TICKS_PER_SECOND = 5f
    const val TICKS_PER_SECOND_GAINED_PER_FOOD = 0.15f
    const val MAX_TICKS_PER_SECOND = 12f

    const val SCORE_PER_FOOD = 1
    const val HIGH_SCORES_KEPT = 10

    val START_DIRECTION = Direction.UP
    const val START_TAIL_X = 4f
    const val START_TAIL_Y = 3f
}
