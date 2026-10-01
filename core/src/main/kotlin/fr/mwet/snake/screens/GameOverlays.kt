package fr.mwet.snake.screens

import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import fr.mwet.snake.assets.I18N
import fr.mwet.snake.ui.GameUiStyles
import ktx.actors.onClick

class ReadyPrompt(styles: GameUiStyles, onStart: () -> Unit) {
    private val tapToStart = Label(I18N.tapToStart(), styles.label).apply {
        touchable = Touchable.disabled
    }

    val actor = Table().apply {
        setFillParent(true)
        touchable = Touchable.enabled
        bottom().padBottom(96f)
        add(tapToStart)
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
