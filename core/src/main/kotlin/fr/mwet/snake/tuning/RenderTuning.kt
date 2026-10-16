package fr.mwet.snake.tuning

object RenderTuning {
    const val PIXELS_PER_CELL = 64f

    object Background {
        const val TILE_CELLS = 8f
    }

    object Snake {
        const val SHADOW_X = 0.07f
        const val SHADOW_Y = -0.09f
        const val EYE_FORWARD = -10f / PIXELS_PER_CELL
        const val EYE_SIDE = 14.5f / PIXELS_PER_CELL
        const val PUPIL_SIZE = 16f / PIXELS_PER_CELL
        const val PUPIL_TRAVEL = 3.6f / PIXELS_PER_CELL
        const val GLOW_SIZE = 3.2f
        const val MARK_SIZE = 40f / PIXELS_PER_CELL
        const val HEAD_MARK_OFFSET = 7.5f / PIXELS_PER_CELL
        const val TURN_MARK_OFFSET = 9.37f / PIXELS_PER_CELL
        const val TAIL_MARK_OFFSET = 18f / PIXELS_PER_CELL
        const val TAIL_MARK_SCALE = 0.85f
    }

    object Food {
        const val GLOW_SIZE = 4.5f
    }
}
