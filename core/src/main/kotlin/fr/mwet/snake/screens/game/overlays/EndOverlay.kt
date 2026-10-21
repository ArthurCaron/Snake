package fr.mwet.snake.screens.game.overlays

import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Label
import fr.mwet.snake.generated.assets.I18N
import fr.mwet.snake.tuning.AnimationTuning.END_OVERLAY_FADE_IN_DURATION
import fr.mwet.snake.ui.UiStyle
import fr.mwet.snake.ui.menuButton
import ktx.scene2d.label
import ktx.scene2d.scene2d
import ktx.scene2d.table

class EndOverlay(onPlayAgain: () -> Unit, onMainMenu: () -> Unit) {
    private var title: Label
    private var score: Label
    private var best: Label
    private var newHighScore: Label

    val actor = scene2d.table {
        setFillParent(true)
        touchable = Touchable.enabled
        background(UiStyle.DIM)
        table {
            background(UiStyle.PANEL)
            pad(32f)
            defaults().space(16f)
            title = label("", UiStyle.TITLE) { it.row() }
            score = label("") { it.row() }
            best = label("") { it.row() }
            newHighScore = label(I18N.newHighScore()) { it.row() }
            menuButton(I18N.playAgain(), onClick = onPlayAgain) { it.fillX().row() }
            menuButton(I18N.mainMenu(), onClick = onMainMenu) { it.fillX() }
        }
    }

    fun setEndGameResult(score: Int, best: Int, isNewHighScore: Boolean) {
        this.score.setText(I18N.score(score))
        this.best.setText(I18N.best(best))
        newHighScore.isVisible = isNewHighScore
    }

    fun show(won: Boolean, delaySeconds: Float) {
        title.setText(if (won) I18N.victory() else I18N.gameOver())
        actor.clearActions()
        actor.isVisible = false
        actor.color.a = 0f
        actor.addAction(
            Actions.sequence(
                Actions.delay(delaySeconds),
                Actions.show(),
                Actions.fadeIn(END_OVERLAY_FADE_IN_DURATION)
            )
        )
    }

    fun hide() {
        actor.clearActions()
        actor.isVisible = false
    }
}
