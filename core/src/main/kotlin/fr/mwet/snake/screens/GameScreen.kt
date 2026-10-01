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
import fr.mwet.snake.events.GameEvent.*
import fr.mwet.snake.events.GameEventListener
import fr.mwet.snake.game.GameState
import fr.mwet.snake.game.GameWorld
import fr.mwet.snake.render.DisintegratingSnakeRenderer
import fr.mwet.snake.render.FoodRenderer
import fr.mwet.snake.render.Palette
import fr.mwet.snake.render.SnakeRenderer
import fr.mwet.snake.save.metadata.BestScore
import fr.mwet.snake.ui.GameUiStyles
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
    styles: GameUiStyles,
    private val bestScore: BestScore,
) : GameEventListener, KtxScreen, DisposableRegistry by DisposableContainer() {
    private val stageViewport = StageViewport().also {
        Game.addViewport(it)
    }
    private val stage = stage(batch, stageViewport)
    private val backgroundTexture =
        textureHandler.rectangleWithBorderTexture(width = WORLD_WIDTH, height = WORLD_HEIGHT, fill = Palette.background)
    private val gridCellTexture = textureHandler.gridCell
    val foodRenderer = FoodRenderer(textureHandler, gameWorld.food)
    val snakeRenderer = SnakeRenderer(textureHandler, gameWorld.snake)
    val disintegratingSnakeRenderer = DisintegratingSnakeRenderer(textureHandler, gameWorld.snake)

    private val readyPrompt = ReadyPrompt(styles, onStart = { gameWorld.start() })
    private val pauseOverlay = PauseOverlay(
        styles,
        onResume = { gameWorld.resume() },
        onMainMenu = { Game.setScreen<MainMenuScreen>() },
    )

    private val hud = GameHud(styles, onPause = { gameWorld.pause() })
    private var gameState: GameState? = null

    init {
        stage.addActor(readyPrompt.actor)
        stage.addActor(pauseOverlay.actor)
        stage.addActor(hud.actor)
    }

    val lostTask = object : Timer.Task() {
        override fun run() {
            Game.setScreen<MainMenuScreen>()
        }
    }

    override fun show() {
        DI.registerGameInputProcessor()
        DI.registerInputProcessor(stage)
        gameWorld.newGame()
        gameState = null
        foodRenderer.reset()
        snakeRenderer.reset()
        disintegratingSnakeRenderer.reset()
    }

    override fun hide() {
        DI.unRegisterGameInputProcessor()
        DI.unRegisterInputProcessor(stage)
        lostTask.cancel()
    }

    override fun render(delta: Float) {
        gameWorld.update(delta)
        if (gameWorld.state != gameState) onStateChanged(gameWorld.state)
        hud.show(gameWorld.score, bestScore.value)

        gameViewport.apply(true)
        batch.use(gameCamera) {
            it.draw(backgroundTexture, 0f, 0f)
            drawGridMap(it)
            render(it, delta)
        }

        stageViewport.apply()
        stage.act(delta)
        stage.draw()
    }

    private fun onStateChanged(state: GameState) {
        gameState = state
        readyPrompt.actor.isVisible = state == GameState.Ready
        pauseOverlay.actor.isVisible = state == GameState.Paused
        hud.showPauseButton(state == GameState.Ready || state == GameState.Playing)
        if (gameWorld.isOver) bestScore.submit(gameWorld.score)
    }

    override fun pause() {
        gameWorld.pause()
    }

    fun render(batch: SpriteBatch, delta: Float) {
        if (gameWorld.state == GameState.Lost) {
            disintegratingSnakeRenderer.render(batch, delta)
            return
        }
        foodRenderer.render(batch, delta)
        snakeRenderer.render(batch, delta)
    }

    override fun onEvent(event: GameEvent) {
        when (event) {
            Lost -> lost()
            Won -> won()
            GoBackToMainMenu -> back()
            Pause -> togglePause()
            FoodEaten, SnakeMoved -> {}
        }
    }

    private fun lost() {
        disintegratingSnakeRenderer.disintegrate()
        lostTask.cancel()
        Timer.schedule(lostTask, 1.5f)
    }

    private fun won() {
        // Do something
    }

    private fun back() {
        when (gameWorld.state) {
            GameState.Ready, GameState.Playing -> gameWorld.pause()
            GameState.Paused, GameState.Lost, GameState.Won -> Game.setScreen<MainMenuScreen>()
        }
    }

    private fun togglePause() {
        if (gameWorld.state == GameState.Paused) gameWorld.resume() else gameWorld.pause()
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
