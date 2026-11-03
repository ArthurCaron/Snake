package fr.mwet.tools.translationpreview

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import fr.mwet.snake.game.GameState
import fr.mwet.snake.screens.game.GameUi
import fr.mwet.snake.screens.game.GameUiActions
import fr.mwet.snake.ui.UiLayer
import fr.mwet.snake.ui.Widgets

private const val SCORE = 42
private const val LOW_SCORE = 7

class Scenario(val name: String, val layer: UiLayer)

fun gameScenarios(batch: SpriteBatch, width: Int, height: Int): List<Scenario> = listOf(
    gameScenario("Game, paused", batch, width, height) {
        showState(GameState.Paused)
    },
    gameScenario("Game, won with a new high score", batch, width, height) {
        setEndGameResult(SCORE, SCORE, isNewHighScore = true)
        showState(GameState.Won)
    },
    gameScenario("Game, lost", batch, width, height) {
        setEndGameResult(LOW_SCORE, SCORE, isNewHighScore = false)
        showState(GameState.Lost)
    },
)

private fun gameScenario(name: String, batch: SpriteBatch, width: Int, height: Int, apply: GameUi.() -> Unit): Scenario {
    val layer = UiLayer(batch).apply { resize(width, height) }
    // No game viewport here: the board covers the whole stage.
    layer.board.setBounds(0f, 0f, layer.width, layer.height)
    GameUi(layer.stage, layer.board, noOp<GameUiActions>(), Widgets {}).apply {
        layout()
        showScore(SCORE, SCORE)
        apply()
    }
    return Scenario(name, layer)
}
