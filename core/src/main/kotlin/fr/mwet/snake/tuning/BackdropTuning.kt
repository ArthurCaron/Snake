package fr.mwet.snake.tuning

import com.badlogic.gdx.graphics.Color

object BackdropTuning {
    object DemoSnake {
        val DIM_COLOR: Color = Color(Palette.background).apply { a = 0.45f }
        const val RESTART_DELAY = 1.5f
    }

    object DiagonalStripes {
        val COLOR: Color = Color(Color.GREEN).apply { a = 0.06f }
        const val SPACING = 3f
        const val BAND = 1f
        const val CELLS_PER_SECOND = 0.3f
    }

    object DriftingGrid {
        val COLOR: Color = Color.GREEN
        const val CELLS_PER_SECOND = 0.4f
        const val MIN_ALPHA = 0.04f
        const val MAX_ALPHA = 0.12f
        const val PULSE_SECONDS = 4f
    }

    object Fireflies {
        val COLOR: Color = Color.GOLD
        const val MIN_BLINK_SPEED = 0.4f
        const val MAX_BLINK_SPEED = 1.1f
        const val COUNT = 18
        const val SIZE = 0.18f
        const val MAX_ALPHA = 0.45f
        const val MAX_DRIFT = 0.35f
    }

    object FloatingFruits {
        val COLOR: Color = Color(1f, 1f, 1f, 0.35f)
        const val ROTATION_DEGREES = 20f
        const val ROTATION_SPEED = 0.7f
        const val MIN_SIZE = 0.6f
        const val MAX_SIZE = 1.4f
        const val FRUIT_COUNT = 14
        const val MIN_SPEED = 0.5f
        const val MAX_SPEED = 1.2f
        const val SWAY = 0.4f
    }

    object GameOfLife {
        val COLOR: Color = Color.GREEN
        const val MIN_BRIGHTNESS = 0.02f
        const val STEP_SECONDS = 0.35f
        const val SEED_DENSITY = 0.3f
        const val MAX_GENERATIONS = 80
        const val MIN_POPULATION = 10
        const val MAX_ALPHA = 0.2f
        const val FADE_PER_SECOND = 4f
        const val INSET = 0.08f
    }

    object Ripples {
        val COLOR: Color = Color.GREEN
        const val MIN_VISIBLE_ALPHA = 0.005f
        const val MAX_ALPHA = 0.22f
        const val RING_FREQUENCY = 1.1f
        const val RING_SPEED = 2.2f
        const val SOURCE_SPEED = 0.13f
        const val INSET = 0.08f
    }

    object WanderingSnakes {
        val COLOR: Color = Color.GREEN
        const val MIN_STEP_SECONDS = 0.15f
        const val MAX_STEP_SECONDS = 0.3f
        const val MIN_LENGTH = 5
        const val SNAKE_COUNT = 5
        const val MAX_LENGTH = 10
        const val TURN_CHANCE = 0.25f
        const val HEAD_ALPHA = 0.3f
        const val INSET = 0.12f
    }

    object Waves {
        val COLOR: Color = Color.GREEN
        const val MAX_ALPHA = 0.08f
        const val SPEED = 1.2f
        const val ROWS_PER_WAVE = 7f
    }
}
