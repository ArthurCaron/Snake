package fr.mwet.snake.screens.game.overlays

import com.badlogic.gdx.scenes.scene2d.Touchable
import fr.mwet.snake.generated.assets.I18N
import fr.mwet.snake.ui.UiMetrics.OVERLAY_SPACE
import fr.mwet.snake.ui.UiMetrics.PANEL_PAD
import fr.mwet.snake.ui.UiStyle
import fr.mwet.snake.ui.Widgets
import ktx.scene2d.label
import ktx.scene2d.scene2d
import ktx.scene2d.table

class PauseOverlay(widgets: Widgets, onResume: () -> Unit, onMainMenu: () -> Unit) {
    val actor = with(widgets) {
        scene2d.table {
            setFillParent(true)
            touchable = Touchable.enabled
            background(UiStyle.DIM)
            table {
                background(UiStyle.PANEL)
                pad(PANEL_PAD)
                defaults().space(OVERLAY_SPACE).fillX()
                label(I18N.paused(), UiStyle.TITLE) { it.row() }
                menuButton(I18N.resume(), onClick = onResume) { it.row() }
                menuButton(I18N.mainMenu(), onClick = onMainMenu)
            }
        }
    }
}
