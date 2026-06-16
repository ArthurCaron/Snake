package fr.mwet.snake.screens

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.scenes.scene2d.Stage
import fr.mwet.snake.GameViewport
import fr.mwet.snake.StageViewport
import fr.mwet.snake.assets.TextureHandler
import fr.mwet.snake.game.GameWorld
import fr.mwet.snake.utils.WORLD_HEIGHT
import fr.mwet.snake.utils.WORLD_WIDTH
import fr.mwet.snake.utils.resetColor
import ktx.app.KtxScreen
import ktx.assets.DisposableContainer
import ktx.assets.DisposableRegistry
import ktx.graphics.use

@Suppress("DELEGATED_MEMBER_HIDES_SUPERTYPE_OVERRIDE")
class GameScreen(
    private val textureHandler: TextureHandler,
    private val stage: Stage,
    private val batch: SpriteBatch,
    private val gameViewport: GameViewport,
    private val stageViewport: StageViewport,
    private val gameCamera: OrthographicCamera,
    private val gameWorld: GameWorld,
) : KtxScreen, DisposableRegistry by DisposableContainer() {
    override fun show() {
        stage.clear()
        gameWorld.show()
    }

    override fun render(delta: Float) {
        gameViewport.apply(true)
        batch.use(gameCamera) {
            it.draw(textureHandler.background, 0f, 0f, WORLD_WIDTH, WORLD_HEIGHT)
            drawGridMap(it)
            gameWorld.update(delta)
            gameWorld.render(it, delta)
        }

        stageViewport.apply()
        stage.act(delta)
        stage.draw()
    }

    private fun drawGridMap(batch: SpriteBatch) {
        batch.setColor(1f, 1f, 1f, 0.05f)
        (0..<WORLD_WIDTH.toInt()).forEach { x ->
            (0..<WORLD_HEIGHT.toInt()).forEach { y ->
                batch.draw(textureHandler.gridCell, x.toFloat(), y.toFloat(), 1f, 1f)
            }
        }
        batch.resetColor()
    }
}
