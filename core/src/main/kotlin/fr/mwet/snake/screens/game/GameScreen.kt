package fr.mwet.snake.screens.game

import fr.mwet.snake.Navigator
import fr.mwet.snake.game.GameState
import fr.mwet.snake.game.GameWorld
import fr.mwet.snake.game.SnakeStart
import fr.mwet.snake.inputs.DebugInputProcessor
import fr.mwet.snake.inputs.GameInputProcessor
import fr.mwet.snake.inputs.GeneralCommandContext
import fr.mwet.snake.inputs.GeneralInputProcessor
import fr.mwet.snake.inputs.TouchInputProcessor
import fr.mwet.snake.render.BoardRenderer
import fr.mwet.snake.render.WorldLayer
import fr.mwet.snake.save.metadata.MetadataRepository
import fr.mwet.snake.tuning.DebugTuning
import fr.mwet.snake.ui.UiKit
import fr.mwet.snake.ui.UiLayer
import ktx.app.KtxScreen

class GameScreen(
    private val kit: UiKit,
    private val world: GameWorld,
    private val metadataRepository: MetadataRepository,
    private val gameInput: GameInputProcessor,
    private val touchInput: TouchInputProcessor,
    private val navigator: Navigator,
) : KtxScreen {
    private val worldLayer = WorldLayer(kit.batch, kit.gameViewport)
    private val uiLayer = UiLayer(kit.batch)
    private val board = BoardRenderer(kit.textures, world)
    private val ui = GameUi(uiLayer.stage, UiActions(), kit.widgets)
    private val menuInput = GeneralInputProcessor(
        kit.settingsRepository, GeneralCommandContext(
            onStartGame = {
                if (world.state.isStopped()) {
                    kit.widgets.activateButtonAction(::resumeGame)
                    true
                } else false
            },
            onBack = {
                kit.widgets.activateButtonAction(::back)
                true
            },
            onPause = {
                if (world.isOver) false else {
                    kit.widgets.activateButtonAction(::togglePause)
                    true
                }
            },
        )
    )
    private val debugInput = DebugInputProcessor(onStartAlmostWonGame = { startNewGame(SnakeStart.almostWon()) })
    private var gameState: GameState? = null

    override fun show() {
        kit.input.add(menuInput, gameInput, uiLayer.stage, touchInput)
        if (DebugTuning.ENABLED) kit.input.addFirst(debugInput)
        startNewGame()
    }

    override fun hide() {
        kit.input.remove(debugInput, menuInput, gameInput, uiLayer.stage, touchInput)
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

    private fun startNewGame(start: SnakeStart = SnakeStart.random()) {
        world.newGame(start)
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

    private fun resumeGame() {
        if (world.state == GameState.Paused) world.resume()
        if (world.state == GameState.Lost || world.state == GameState.Won) startNewGame()
    }

    private fun back() {
        if (world.state.isPlaying()) world.pause()
        if (world.state.isStopped()) returnToMainMenu()
    }

    private fun togglePause() {
        if (world.state == GameState.Paused) world.resume() else world.pause()
    }

    private fun returnToMainMenu() {
        if (world.state.isStopped()) metadataRepository.submitHighScore(world.score)
        navigator.goToMainMenu()
    }

    private inner class UiActions : GameUiActions {
        override fun pause() = world.pause()
        override fun resume() = world.resume()
        override fun playAgain() = startNewGame()
        override fun mainMenu() = returnToMainMenu()
    }
}
