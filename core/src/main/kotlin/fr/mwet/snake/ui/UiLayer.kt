package fr.mwet.snake.ui

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.Disposable
import fr.mwet.snake.StageViewport

class UiLayer(batch: SpriteBatch) : Disposable {
    private val viewport = StageViewport()
    val stage = Stage(viewport, batch)

    val width: Float get() = viewport.worldWidth
    val height: Float get() = viewport.worldHeight
    val unitsPerPixel: Float get() = viewport.unitsPerPixel

    fun resize(width: Int, height: Int) {
        viewport.update(width, height, true)
    }

    fun render(delta: Float) {
        viewport.apply()
        stage.act(delta)
        stage.draw()
    }

    override fun dispose() {
        stage.dispose()
    }
}
