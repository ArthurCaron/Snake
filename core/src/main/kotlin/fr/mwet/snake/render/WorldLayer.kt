package fr.mwet.snake.render

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import fr.mwet.snake.GameViewport
import ktx.graphics.use

interface WorldDrawable {
    fun draw(batch: SpriteBatch, delta: Float)
}

class WorldLayer(private val batch: SpriteBatch, private val viewport: GameViewport) {
    fun resize(width: Int, height: Int) {
        viewport.update(width, height, true)
    }

    fun render(drawable: WorldDrawable, delta: Float) {
        viewport.apply()
        batch.use(viewport.camera) { drawable.draw(it, delta) }
    }
}
