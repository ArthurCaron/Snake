package fr.mwet.snake.screens.game.overlays

import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import fr.mwet.snake.assets.I18N
import fr.mwet.snake.ui.GameUiStyles
import ktx.actors.onClick

class GameHud(styles: GameUiStyles, onPause: () -> Unit) {
    private val scoreLabel = Label("", styles.label).apply { touchable = Touchable.disabled }
    private val bestLabel = Label("", styles.label).apply { touchable = Touchable.disabled }
    private val pauseButton = TextButton("II", styles.button).apply {
        pad(4f, 16f, 4f, 16f)
        onClick { onPause() }
    }
    private var shownScore = -1
    private var shownBest = -1

    val actor = Table().apply {
        setFillParent(true)
        top().pad(16f)
        add(scoreLabel).expandX().left()
        add(bestLabel).expandX()
        add(pauseButton).expandX().right()
    }

    fun showPauseButton(visible: Boolean) {
        pauseButton.isVisible = visible
    }

    fun show(score: Int, best: Int) {
        if (score != shownScore) {
            shownScore = score
            scoreLabel.setText(I18N.score(score))
        }
        if (best != shownBest) {
            shownBest = best
            bestLabel.setText(I18N.best(best))
        }
    }
}
