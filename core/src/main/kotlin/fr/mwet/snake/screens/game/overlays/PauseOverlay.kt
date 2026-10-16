package fr.mwet.snake.screens.game.overlays

import com.badlogic.gdx.scenes.scene2d.Touchable
import fr.mwet.snake.generated.assets.I18N
import fr.mwet.snake.ui.UiStyle
import fr.mwet.snake.ui.menuButton
import ktx.scene2d.label
import ktx.scene2d.scene2d
import ktx.scene2d.table

class PauseOverlay(onResume: () -> Unit, onMainMenu: () -> Unit) {
    val actor = scene2d.table {
        setFillParent(true)
        touchable = Touchable.enabled
        background(UiStyle.DIM)
        table {
            background(UiStyle.PANEL)
            pad(32f)
            defaults().space(16f).fillX()
            label(I18N.paused(), UiStyle.TITLE) { it.row() }
            menuButton(I18N.resume(), onClick = onResume) { it.row() }
            menuButton(I18N.mainMenu(), onClick = onMainMenu)
        }
    }
}
