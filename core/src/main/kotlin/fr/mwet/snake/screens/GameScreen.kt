package fr.mwet.snake.screens

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.Timer
import fr.mwet.snake.DI
import fr.mwet.snake.Game
import fr.mwet.snake.GameViewport
import fr.mwet.snake.StageViewport
import fr.mwet.snake.assets.TextureHandler
import fr.mwet.snake.events.GameEvent
import fr.mwet.snake.events.GameEventListener
import fr.mwet.snake.game.GameWorld
import fr.mwet.snake.render.DisintegratingSnakeRenderer
import fr.mwet.snake.render.FoodRenderer
import fr.mwet.snake.render.SnakeRenderer
import fr.mwet.snake.utils.WORLD_HEIGHT
import fr.mwet.snake.utils.WORLD_WIDTH
import fr.mwet.snake.utils.resetColor
import ktx.actors.stage
import ktx.app.KtxScreen
import ktx.assets.DisposableContainer
import ktx.assets.DisposableRegistry
import ktx.graphics.use

@Suppress("DELEGATED_MEMBER_HIDES_SUPERTYPE_OVERRIDE")
class GameScreen(
    textureHandler: TextureHandler,
    private val batch: SpriteBatch,
    private val gameViewport: GameViewport,
    private val gameCamera: OrthographicCamera,
    private val gameWorld: GameWorld,
) : GameEventListener, KtxScreen, DisposableRegistry by DisposableContainer() {
    private val stageViewport = StageViewport().also {
        Game.addViewport(it)
    }
    private val stage = stage(batch, stageViewport)
    private val backgroundTexture = textureHandler.background
    private val gridCellTexture = textureHandler.gridCell
    val foodRenderer = FoodRenderer(gameWorld.food)
    val snakeRenderer = SnakeRenderer(gameWorld.snake)
    val disintegratingSnakeRenderer = DisintegratingSnakeRenderer(gameWorld.snake)

    override fun show() {
        DI.registerGameInputProcessor()
        DI.registerInputProcessor(stage)
        stage.clear()
        gameWorld.newGame()
        foodRenderer.reset()
        snakeRenderer.reset()
        disintegratingSnakeRenderer.reset()
    }

    override fun hide() {
        DI.unRegisterGameInputProcessor()
        DI.unRegisterInputProcessor(stage)
    }

    override fun render(delta: Float) {
        gameWorld.update(delta)

        gameViewport.apply(true)
        batch.use(gameCamera) {
            it.draw(backgroundTexture, 0f, 0f, WORLD_WIDTH, WORLD_HEIGHT)
            drawGridMap(it)
            render(it, delta)
        }

        stageViewport.apply()
        stage.act(delta)
        stage.draw()
    }

    fun render(batch: SpriteBatch, delta: Float) {
        if (gameWorld.gameOver) {
            disintegratingSnakeRenderer.render(batch, delta)
            return
        }
        foodRenderer.render(batch, delta)
        snakeRenderer.render(batch, delta)
    }

    override fun onEvent(event: GameEvent) {
        when (event) {
            GameEvent.GameOver -> gameOver()
            GameEvent.FoodEaten -> {}
            GameEvent.SnakeMoved -> {}
        }
    }

    private fun gameOver() {
        disintegratingSnakeRenderer.disintegrate(gameWorld.snake)
        Timer.schedule(object : Timer.Task() {
            override fun run() {
                Game.setScreen<MainMenuScreen>()
            }
        }, 1.5f)
    }

    private fun drawGridMap(batch: SpriteBatch) {
        batch.setColor(1f, 1f, 1f, 0.05f)
        (0..<WORLD_WIDTH.toInt()).forEach { x ->
            (0..<WORLD_HEIGHT.toInt()).forEach { y ->
                batch.draw(gridCellTexture, x.toFloat(), y.toFloat(), 1f, 1f)
            }
        }
        batch.resetColor()
    }
}
