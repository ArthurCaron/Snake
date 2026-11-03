package fr.mwet.snake.screens.game.overlays

import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton
import com.badlogic.gdx.scenes.scene2d.ui.Label
import fr.mwet.snake.generated.assets.I18N
import fr.mwet.snake.ui.UiMetrics.HUD_BUTTON_PAD_BOTTOM
import fr.mwet.snake.ui.UiMetrics.HUD_BUTTON_PAD_SIDE
import fr.mwet.snake.ui.UiMetrics.HUD_BUTTON_PAD_TOP
import fr.mwet.snake.ui.UiMetrics.HUD_ICON_SIZE
import fr.mwet.snake.ui.UiMetrics.HUD_PAD
import fr.mwet.snake.ui.UiStyle
import fr.mwet.snake.ui.Widgets
import ktx.scene2d.label
import ktx.scene2d.scene2d
import ktx.scene2d.table

class GameHud(widgets: Widgets, onPause: () -> Unit) {
    private var scoreLabel: Label
    private var bestLabel: Label
    private var pauseButton: ImageButton
    private var shownScore = -1
    private var shownBest = -1

    val actor = with(widgets) {
        scene2d.table {
            pad(HUD_PAD)
            // The three cells share one width, so the best score stays in the middle when the score gets longer.
            scoreLabel = label("") {
                touchable = Touchable.disabled
                it.expandX().uniformX().left()
            }
            bestLabel = label("") {
                touchable = Touchable.disabled
                it.expandX().uniformX()
            }
            pauseButton = iconButton(UiStyle.PAUSE, onClick = onPause) {
                pad(HUD_BUTTON_PAD_TOP, HUD_BUTTON_PAD_SIDE, HUD_BUTTON_PAD_BOTTOM, HUD_BUTTON_PAD_SIDE)
                imageCell.size(HUD_ICON_SIZE)
                it.expandX().uniformX().right()
            }
        }
    }

    // The height of the row, pads included, in stage units.
    val height: Float get() = actor.prefHeight

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
