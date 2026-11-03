package fr.mwet.snake.ui

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.utils.Disposable
import fr.mwet.snake.GameViewport
import fr.mwet.snake.StageViewport

class UiLayer(batch: SpriteBatch) : Disposable {
    private val viewport = StageViewport()
    val stage = Stage(viewport, batch)

    // Covers the board on screen. The screens put their widgets in it, so that the bands around the board stay empty.
    val board = Group().also {
        it.touchable = Touchable.childrenOnly
        stage.addActor(it)
    }

    val width: Float get() = viewport.worldWidth
    val height: Float get() = viewport.worldHeight
    val unitsPerPixel: Float get() = viewport.unitsPerPixel

    fun resize(width: Int, height: Int) {
        viewport.update(width, height, true)
    }

    // Call after the world layer has resized the game viewport.
    fun fitBoard(gameViewport: GameViewport) {
        board.setBounds(
            gameViewport.screenX * unitsPerPixel,
            gameViewport.screenY * unitsPerPixel,
            gameViewport.screenWidth * unitsPerPixel,
            gameViewport.screenHeight * unitsPerPixel,
        )
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
