package fr.mwet.snake.screens.game.overlays

import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import fr.mwet.snake.generated.assets.I18N
import fr.mwet.snake.ui.Widgets
import ktx.scene2d.label
import ktx.scene2d.scene2d
import ktx.scene2d.table

class GameHud(widgets: Widgets, onPause: () -> Unit) {
    private var scoreLabel: Label
    private var bestLabel: Label
    private var pauseButton: TextButton
    private var shownScore = -1
    private var shownBest = -1

    val actor = with(widgets) {
        scene2d.table {
            setFillParent(true)
            top().pad(16f)
            scoreLabel = label("") {
                touchable = Touchable.disabled
                it.expandX().left()
            }
            bestLabel = label("") {
                touchable = Touchable.disabled
                it.expandX()
            }
            pauseButton = menuButton("II", onClick = onPause) { it.expandX().right() }
        }
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
