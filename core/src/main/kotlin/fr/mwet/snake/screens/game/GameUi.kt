package fr.mwet.snake.screens.game

import com.badlogic.gdx.scenes.scene2d.Stage
import fr.mwet.snake.game.GameState
import fr.mwet.snake.screens.game.overlays.EndOverlay
import fr.mwet.snake.screens.game.overlays.GameHud
import fr.mwet.snake.screens.game.overlays.PauseOverlay
import fr.mwet.snake.screens.game.overlays.ReadyPrompt

private const val LOST_OVERLAY_DELAY = 1f

interface GameUiActions {
    fun pause()
    fun resume()
    fun playAgain()
    fun mainMenu()
}

class GameUi(stage: Stage, actions: GameUiActions) {
    private val readyPrompt = ReadyPrompt()
    private val hud = GameHud(onPause = actions::pause)
    private val pauseOverlay = PauseOverlay(onResume = actions::resume, onMainMenu = actions::mainMenu)
    private val endOverlay = EndOverlay(onPlayAgain = actions::playAgain, onMainMenu = actions::mainMenu)

    init {
        stage.addActor(readyPrompt.actor)
        stage.addActor(pauseOverlay.actor)
        stage.addActor(endOverlay.actor)
        stage.addActor(hud.actor)
    }

    fun showState(state: GameState) {
        readyPrompt.actor.isVisible = state == GameState.Ready
        pauseOverlay.actor.isVisible = state == GameState.Paused
        hud.showPauseButton(state == GameState.Ready || state == GameState.Playing)
        when (state) {
            GameState.Lost -> endOverlay.show(won = false, delaySeconds = LOST_OVERLAY_DELAY)
            GameState.Won -> endOverlay.show(won = true, delaySeconds = 0f)
            GameState.Ready, GameState.Playing, GameState.Paused -> endOverlay.hide()
        }
    }

    fun showScore(score: Int, best: Int) {
        hud.show(score, best)
    }

    fun setEndGameResult(score: Int, best: Int, isNewHighScore: Boolean) {
        endOverlay.setEndGameResult(score, best, isNewHighScore)
    }

    fun onHide() {
        endOverlay.hide()
    }
}
