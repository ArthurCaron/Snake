package fr.mwet.snake.screens

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
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
import fr.mwet.snake.save.metadata.MetadataRepository
import fr.mwet.snake.ui.GameUiStyles
import fr.mwet.snake.utils.WORLD_HEIGHT
import fr.mwet.snake.utils.WORLD_WIDTH
import fr.mwet.snake.utils.resetColor
import ktx.actors.stage
import ktx.app.KtxScreen
import ktx.assets.DisposableContainer
import ktx.assets.DisposableRegistry
import ktx.graphics.use

private const val LOST_OVERLAY_DELAY = 1f

@Suppress("DELEGATED_MEMBER_HIDES_SUPERTYPE_OVERRIDE")
class GameScreen(
    textureHandler: TextureHandler,
    private val batch: SpriteBatch,
    private val gameViewport: GameViewport,
    private val gameCamera: OrthographicCamera,
    private val gameWorld: GameWorld,
    styles: GameUiStyles,
    private val metadataRepository: MetadataRepository,
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
        onMainMenu = {
            metadataRepository.submitHighScore(gameWorld.score)
            Game.setScreen<MainMenuScreen>()
        },
    )
    private val endOverlay = EndOverlay(
        styles,
        onPlayAgain = { startNewGame() },
        onMainMenu = { Game.setScreen<MainMenuScreen>() },
    )

    private val hud = GameHud(styles, onPause = { gameWorld.pause() })
    private var gameState: GameState? = null

    init {
        stage.addActor(readyPrompt.actor)
        stage.addActor(pauseOverlay.actor)
        stage.addActor(endOverlay.actor)
        stage.addActor(hud.actor)
    }

    override fun show() {
        DI.registerGameInputProcessor()
        DI.registerInputProcessor(stage)
        startNewGame()
    }

    override fun hide() {
        DI.unRegisterGameInputProcessor()
        DI.unRegisterInputProcessor(stage)
        endOverlay.hide()
    }

    private fun startNewGame() {
        gameWorld.newGame()
        gameState = null
        foodRenderer.reset()
        snakeRenderer.reset()
        disintegratingSnakeRenderer.reset()
    }

    override fun render(delta: Float) {
        gameWorld.update(delta)
        if (gameWorld.state != gameState) onStateChanged(gameWorld.state)
        hud.show(gameWorld.score, metadataRepository.metadata.highScore.score)

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
        when (state) {
            GameState.Lost -> {
                disintegratingSnakeRenderer.disintegrate()
                showEndOverlay(won = false, delay = LOST_OVERLAY_DELAY)
            }

            GameState.Won -> showEndOverlay(won = true, delay = 0f)
            GameState.Ready, GameState.Playing, GameState.Paused -> endOverlay.hide()
        }
    }

    private fun showEndOverlay(won: Boolean, delay: Float) {
        val isNewHighScore = metadataRepository.submitHighScore(gameWorld.score)
        endOverlay.show(won, gameWorld.score, metadataRepository.metadata.highScore.score, isNewHighScore, delay)
    }

    override fun pause() {
        gameWorld.pause()
    }

    fun render(batch: SpriteBatch, delta: Float) {
        when (gameWorld.state) {
            GameState.Lost -> disintegratingSnakeRenderer.render(batch, delta)
            GameState.Won -> snakeRenderer.render(batch, delta)
            GameState.Ready, GameState.Playing, GameState.Paused -> {
                foodRenderer.render(batch, delta)
                snakeRenderer.render(batch, delta)
            }
        }
    }

    override fun onEvent(event: GameEvent) {
        when (event) {
            GoBackToMainMenu -> back()
            Pause -> togglePause()
            FoodEaten, SnakeMoved, Lost, Won, Paused, Resumed -> {}
        }
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
