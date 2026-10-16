package fr.mwet.snake.screens.game.overlays

import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import fr.mwet.snake.assets.I18N
import fr.mwet.snake.ui.UiStyles
import ktx.actors.onClick

class PauseOverlay(styles: UiStyles, onResume: () -> Unit, onMainMenu: () -> Unit) {
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
