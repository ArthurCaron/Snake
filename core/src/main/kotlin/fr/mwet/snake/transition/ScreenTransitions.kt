package fr.mwet.snake.transition

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Matrix4
import fr.mwet.snake.save.settings.SettingsRepository

class ScreenTransitions(
    private val batch: SpriteBatch,
    pixel: Texture,
    private val settingsRepository: SettingsRepository,
) {
    private val projection = Matrix4()
    private val fade = FadeTransition(pixel)
    private val slide = SlideTransition()
    private val snakeWipe = SnakeWipeTransition(pixel)
    private var transition: ScreenTransition? = null
    private var phase = Phase.Begin
    private var elapsed = 0f
    private var pendingSwitch: (() -> Unit)? = null

    fun start(switch: () -> Unit) {
        if (transition != null) return
        val chosen = transitionFor(settingsRepository.transition)
        if (chosen == null) {
            switch()
            return
        }
        transition = chosen
        pendingSwitch = switch
        elapsed = 0f
        phase = Phase.Begin
    }

    fun render(delta: Float) {
        val current = transition ?: return
        when (phase) {
            Phase.Begin -> {
                current.begin()
                if (current.outSeconds > 0f) phase = Phase.Out else switchScreen()
            }
            Phase.Out -> {
                elapsed += delta
                draw(current, progress = elapsed / current.outSeconds, coming = false)
                if (elapsed >= current.outSeconds) switchScreen()
            }
            Phase.In -> {
                elapsed += delta
                draw(current, progress = elapsed / current.inSeconds, coming = true)
                if (elapsed >= current.inSeconds) finish(current)
            }
        }
    }

    private fun transitionFor(style: TransitionStyle): ScreenTransition? = when (style) {
        TransitionStyle.None -> null
        TransitionStyle.Fade -> fade
        TransitionStyle.Slide -> slide
        TransitionStyle.SnakeWipe -> snakeWipe
    }

    private fun switchScreen() {
        pendingSwitch?.invoke()
        pendingSwitch = null
        elapsed = 0f
        phase = Phase.In
    }

    private fun finish(current: ScreenTransition) {
        current.end()
        transition = null
    }

    private fun draw(current: ScreenTransition, progress: Float, coming: Boolean) {
        val width = Gdx.graphics.width.toFloat()
        val height = Gdx.graphics.height.toFloat()
        Gdx.gl.glViewport(0, 0, Gdx.graphics.backBufferWidth, Gdx.graphics.backBufferHeight)
        batch.projectionMatrix = projection.setToOrtho2D(0f, 0f, width, height)
        batch.begin()
        current.draw(batch, progress.coerceIn(0f, 1f), coming, width, height)
        batch.color = Color.WHITE
        batch.end()
    }

    private enum class Phase { Begin, Out, In }
}
