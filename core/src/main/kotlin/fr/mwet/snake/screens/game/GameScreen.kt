package fr.mwet.snake.screens.game

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import fr.mwet.snake.GameViewport
import fr.mwet.snake.Navigator
import fr.mwet.snake.assets.TextureHandler
import fr.mwet.snake.events.GameEvent
import fr.mwet.snake.events.GameEvent.*
import fr.mwet.snake.events.GameEventListener
import fr.mwet.snake.game.GameState
import fr.mwet.snake.game.GameWorld
import fr.mwet.snake.inputs.GameInputProcessor
import fr.mwet.snake.inputs.InputRouter
import fr.mwet.snake.inputs.SwipeInputProcessor
import fr.mwet.snake.render.BoardRenderer
import fr.mwet.snake.render.WorldLayer
import fr.mwet.snake.save.metadata.MetadataRepository
import fr.mwet.snake.ui.GameUiStyles
import fr.mwet.snake.ui.UiLayer
import ktx.app.KtxScreen

class GameScreen(
    textureHandler: TextureHandler,
    batch: SpriteBatch,
    gameViewport: GameViewport,
    private val world: GameWorld,
    styles: GameUiStyles,
    private val metadataRepository: MetadataRepository,
    private val input: InputRouter,
    private val gameInput: GameInputProcessor,
    private val swipeInput: SwipeInputProcessor,
    private val navigator: Navigator,
) : KtxScreen, GameEventListener {
    private val worldLayer = WorldLayer(batch, gameViewport)
    private val uiLayer = UiLayer(batch)
    private val board = BoardRenderer(textureHandler, world)
    private val ui = GameUi(styles, uiLayer.stage, UiActions())
    private var gameState: GameState? = null

    override fun show() {
        input.add(gameInput, swipeInput, uiLayer.stage)
        startNewGame()
    }

    override fun hide() {
        input.remove(gameInput, swipeInput, uiLayer.stage)
        ui.onHide()
    }

    override fun render(delta: Float) {
        world.update(delta)
        if (world.state != gameState) onStateChanged(world.state)
        ui.showScore(world.score, metadataRepository.metadata.highestScore.score)

        worldLayer.render(board, delta)
        uiLayer.render(delta)
    }

    override fun resize(width: Int, height: Int) {
        worldLayer.resize(width, height)
        uiLayer.resize(width, height)
    }

    override fun pause() {
        world.pause()
    }

    override fun dispose() {
        uiLayer.dispose()
    }

    override fun onEvent(event: GameEvent) {
        when (event) {
            GoBackToMainMenu -> back()
            Pause -> togglePause()
            FoodEaten, SnakeMoved, Lost, Won, Paused, Resumed -> {}
        }
    }

    private fun startNewGame() {
        world.newGame()
        board.reset()
        gameState = null
    }

    private fun onStateChanged(state: GameState) {
        gameState = state
        when (state) {
            GameState.Lost -> {
                board.disintegrate()
                setEndGameResult()
            }
            GameState.Won -> setEndGameResult()
            GameState.Ready, GameState.Playing, GameState.Paused -> {}
        }
        ui.showState(state)
    }

    private fun setEndGameResult() {
        val isNewHighScore = metadataRepository.submitHighScore(world.score)
        ui.setEndGameResult(world.score, metadataRepository.metadata.highestScore.score, isNewHighScore)
    }

    private fun back() {
        when (world.state) {
            GameState.Ready, GameState.Playing -> world.pause()
            GameState.Paused, GameState.Lost, GameState.Won -> returnToMainMenu()
        }
    }

    private fun togglePause() {
        if (world.state == GameState.Paused) world.resume() else world.pause()
    }

    private fun returnToMainMenu() {
        if (world.state == GameState.Paused) metadataRepository.submitHighScore(world.score)
        navigator.goToMainMenu()
    }

    private inner class UiActions : GameUiActions {
        override fun start() = world.start()
        override fun pause() = world.pause()
        override fun resume() = world.resume()
        override fun playAgain() = startNewGame()
        override fun mainMenu() = returnToMainMenu()
    }
}
