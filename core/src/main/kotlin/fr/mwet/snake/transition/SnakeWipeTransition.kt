package fr.mwet.snake.transition

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import fr.mwet.snake.tuning.Palette
import fr.mwet.snake.tuning.TransitionTuning.WIPE_CELLS_ON_SHORT_SIDE
import fr.mwet.snake.tuning.TransitionTuning.WIPE_SECONDS
import kotlin.math.ceil

// A snake of cells covers the screen row by row, turning at each edge, then uncovers the new screen in the same order.
class SnakeWipeTransition(private val pixel: Texture) : ScreenTransition {
    override val outSeconds = WIPE_SECONDS
    override val inSeconds = WIPE_SECONDS

    override fun draw(batch: SpriteBatch, t: Float, coming: Boolean, width: Float, height: Float) {
        val cell = minOf(width, height) / WIPE_CELLS_ON_SHORT_SIDE
        val columns = ceil(width / cell).toInt()
        val rows = ceil(height / cell).toInt()
        val total = columns * rows
        val reached = (t * total).toInt()
        val first = if (coming) reached else 0
        val last = if (coming) total else reached
        batch.color = Palette.accent
        for (index in first until last) {
            val row = index / columns
            val inRow = index % columns
            val column = if (row % 2 == 0) inRow else columns - 1 - inRow
            batch.draw(pixel, column * cell, height - (row + 1) * cell, cell, cell)
        }
    }
}
