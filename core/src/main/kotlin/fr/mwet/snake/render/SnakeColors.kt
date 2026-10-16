package fr.mwet.snake.render

import com.badlogic.gdx.graphics.Color
import fr.mwet.snake.tuning.AnimationTuning.SNAKE_COLOR_FADE_SECONDS
import fr.mwet.snake.tuning.AnimationTuning.SNAKE_COLOR_SEGMENT_DELAY
import fr.mwet.snake.tuning.Palette

class SnakeColors {
    private val from = Color(Palette.snake)
    private val to = Color(Palette.snake)
    private val scratch = Color()
    private var elapsed = Float.MAX_VALUE

    fun reset() {
        from.set(Palette.snake)
        to.set(Palette.snake)
        elapsed = Float.MAX_VALUE
    }

    fun change(color: Color) {
        from.set(to)
        to.set(color)
        elapsed = 0f
    }

    fun update(delta: Float) {
        if (elapsed < Float.MAX_VALUE) elapsed += delta
    }

    fun at(index: Int): Color {
        val progress = ((elapsed - index * SNAKE_COLOR_SEGMENT_DELAY) / SNAKE_COLOR_FADE_SECONDS).coerceIn(0f, 1f)
        return scratch.set(from).lerp(to, progress)
    }
}
