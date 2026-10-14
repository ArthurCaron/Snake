package fr.mwet.snake.screens.game

import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.utils.Align
import fr.mwet.snake.assets.I18N
import fr.mwet.snake.ui.GameUiStyles
import ktx.actors.onClick

class ReadyPrompt(styles: GameUiStyles, onStart: () -> Unit) {
    private val tapToStart = Label(I18N.tapToStart(), styles.label).apply {
        touchable = Touchable.disabled
        wrap = true
        setAlignment(Align.center)
    }

    val actor = Table().apply {
        setFillParent(true)
        touchable = Touchable.enabled
        bottom().padBottom(96f)
        add(tapToStart).growX().padLeft(32f).padRight(32f)
        onClick { onStart() }
    }
}

class PauseOverlay(styles: GameUiStyles, onResume: () -> Unit, onMainMenu: () -> Unit) {
    private val resume = TextButton(I18N.resume(), styles.button).apply {
        pad(8f, 24f, 8f, 24f)
        onClick { onResume() }
    }
    private val mainMenu = TextButton(I18N.mainMenu(), styles.button).apply {
        pad(8f, 24f, 8f, 24f)
        onClick { onMainMenu() }
    }

    private val panel = Table().apply {
        background = styles.panel
        pad(32f)
        defaults().space(16f).fillX()
        add(Label(I18N.paused(), styles.title)).row()
        add(resume).row()
        add(mainMenu)
    }

    val actor = Table().apply {
        setFillParent(true)
        touchable = Touchable.enabled
        background = styles.dim
        add(panel)
    }
}

class EndOverlay(styles: GameUiStyles, onPlayAgain: () -> Unit, onMainMenu: () -> Unit) {
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

    fun show(won: Boolean, score: Int, best: Int, isNewHighScore: Boolean, delaySeconds: Float) {
        title.setText(if (won) I18N.victory() else I18N.gameOver())
        this.score.setText(I18N.score(score))
        this.best.setText(I18N.best(best))
        newHighScore.isVisible = isNewHighScore
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
