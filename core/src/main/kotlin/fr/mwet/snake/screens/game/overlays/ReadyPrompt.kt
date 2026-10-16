package fr.mwet.snake.screens.game.overlays

import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Align
import fr.mwet.snake.generated.assets.I18N
import fr.mwet.snake.ui.UiStyles

class ReadyPrompt(styles: UiStyles) {
    private val tapToStart = Label(I18N.tapToStart(), styles.label).apply {
        touchable = Touchable.disabled
        wrap = true
        setAlignment(Align.center)
    }

    val actor = Table().apply {
        setFillParent(true)
        touchable = Touchable.disabled
        bottom().padBottom(96f)
        add(tapToStart).growX().padLeft(32f).padRight(32f)
    }
}
