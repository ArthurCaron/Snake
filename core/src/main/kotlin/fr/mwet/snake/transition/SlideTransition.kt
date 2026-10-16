package fr.mwet.snake.transition

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Interpolation
import fr.mwet.snake.tuning.TransitionTuning.SLIDE_SECONDS
import fr.mwet.snake.utils.captureScreen

// The screens switch at once, and a capture of the old screen slides out to the left over the new one.
class SlideTransition : ScreenTransition {
    private var capture: TextureRegion? = null

    override val outSeconds = 0f
    override val inSeconds = SLIDE_SECONDS

    override fun begin() {
        capture = captureScreen()
    }

    override fun draw(batch: SpriteBatch, t: Float, coming: Boolean, width: Float, height: Float) {
        val region = capture ?: return
        batch.draw(region, -Interpolation.smooth.apply(t) * width, 0f, width, height)
    }

    override fun end() {
        capture?.texture?.dispose()
        capture = null
    }
}
