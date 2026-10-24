package fr.mwet.snake.screens.game

import com.badlogic.gdx.scenes.scene2d.Stage
import fr.mwet.snake.game.GameState
import fr.mwet.snake.screens.game.overlays.EndOverlay
import fr.mwet.snake.screens.game.overlays.GameHud
import fr.mwet.snake.screens.game.overlays.PauseOverlay
import fr.mwet.snake.screens.game.overlays.ReadyPrompt
import fr.mwet.snake.tuning.AnimationTuning.LOST_OVERLAY_DELAY
import fr.mwet.snake.ui.Widgets

interface GameUiActions {
    fun pause()
    fun resume()
    fun playAgain()
    fun mainMenu()
}

class GameUi(stage: Stage, actions: GameUiActions, widgets: Widgets) {
    private val readyPrompt = ReadyPrompt()
    private val hud = GameHud(widgets, onPause = actions::pause)
    private val pauseOverlay = PauseOverlay(widgets, onResume = actions::resume, onMainMenu = actions::mainMenu)
    private val endOverlay = EndOverlay(widgets, onPlayAgain = actions::playAgain, onMainMenu = actions::mainMenu)

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
