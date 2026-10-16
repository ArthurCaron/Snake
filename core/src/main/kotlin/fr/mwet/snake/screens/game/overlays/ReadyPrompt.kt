package fr.mwet.snake.screens.game.overlays

import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.utils.Align
import fr.mwet.snake.generated.assets.I18N
import fr.mwet.snake.ui.UiStyle
import ktx.scene2d.label
import ktx.scene2d.scene2d
import ktx.scene2d.table

class ReadyPrompt {
    val actor = scene2d.table {
        setFillParent(true)
        touchable = Touchable.disabled
        background(UiStyle.LIGHT_DIM)
        label(I18N.ready(), UiStyle.TITLE) { it.padBottom(24f).row() }
        label(I18N.tapToStart()) {
            wrap = true
            setAlignment(Align.center)
            it.growX().padLeft(32f).padRight(32f)
        }
    }
}
