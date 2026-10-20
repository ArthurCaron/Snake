package fr.mwet.snake.screens.game.overlays

import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import fr.mwet.snake.generated.assets.I18N
import fr.mwet.snake.ui.UiStyles
import ktx.actors.onClick

class EndOverlay(styles: UiStyles, onPlayAgain: () -> Unit, onMainMenu: () -> Unit) {
    private val title = Label("", styles.title)
    private val score = Label("", styles.label)
    private val best = Label("", styles.label)
    private val newHighScore = Label(I18N.newHighScore(), styles.label)
    private val playAgain = TextButton(I18N.playAgain(), styles.button).apply {
        pad(8f, 24f, 8f, 24f)
        onClick { onPlayAgain() }
    }
    private val mainMenu = TextButton(I18N.mainMenu(), styles.button).apply {
        pad(8f, 24f, 8f, 24f)
        onClick { onMainMenu() }
    }

    private val panel = Table().apply {
        background = styles.panel
        pad(32f)
        defaults().space(16f)
        add(title).row()
        add(score).row()
        add(best).row()
        add(newHighScore).row()
        add(playAgain).fillX().row()
        add(mainMenu).fillX()
    }

    val actor = Table().apply {
        setFillParent(true)
        touchable = Touchable.enabled
        background = styles.dim
        add(panel)
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
                Actions.fadeIn(0.3f)
            )
        )
    }

    fun hide() {
        actor.clearActions()
        actor.isVisible = false
    }
}
