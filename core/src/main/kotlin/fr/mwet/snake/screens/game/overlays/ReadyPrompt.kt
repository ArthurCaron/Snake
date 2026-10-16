package fr.mwet.snake.screens.game.overlays

import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Align
import fr.mwet.snake.assets.I18N
import fr.mwet.snake.ui.UiStyles
import ktx.actors.onClick

class ReadyPrompt(styles: UiStyles, onStart: () -> Unit) {
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
