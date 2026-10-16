package fr.mwet.snake.transition

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import fr.mwet.snake.render.Palette
import fr.mwet.snake.tuning.TransitionTuning.FADE_IN_SECONDS
import fr.mwet.snake.tuning.TransitionTuning.FADE_OUT_SECONDS

// The old screen fades to a plain colour, and the new screen fades in from it.
class FadeTransition(private val pixel: Texture) : ScreenTransition {
    override val outSeconds = FADE_OUT_SECONDS
    override val inSeconds = FADE_IN_SECONDS

    override fun draw(batch: SpriteBatch, t: Float, coming: Boolean, width: Float, height: Float) {
        val alpha = if (coming) 1f - t else t
        batch.setColor(Palette.transitionFade.r, Palette.transitionFade.g, Palette.transitionFade.b, alpha)
        batch.draw(pixel, 0f, 0f, width, height)
    }
}
