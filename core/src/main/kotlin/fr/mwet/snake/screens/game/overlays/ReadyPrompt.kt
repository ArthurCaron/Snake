package fr.mwet.snake.screens.game.overlays

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Peripheral
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.utils.Align
import fr.mwet.snake.generated.assets.I18N
import fr.mwet.snake.ui.UiMetrics.READY_TEXT_SIDE_PAD
import fr.mwet.snake.ui.UiMetrics.READY_TITLE_GAP
import fr.mwet.snake.ui.UiStyle
import ktx.scene2d.label
import ktx.scene2d.scene2d
import ktx.scene2d.table

class ReadyPrompt {
    val actor = scene2d.table {
        setFillParent(true)
        touchable = Touchable.disabled
        background(UiStyle.LIGHT_DIM)
        label(I18N.ready(), UiStyle.TITLE) { it.padBottom(READY_TITLE_GAP).row() }
        label(if (Gdx.input.isPeripheralAvailable(Peripheral.MultitouchScreen)) I18N.readyTouch() else I18N.readyKeys()) {
            wrap = true
            setAlignment(Align.center)
            it.growX().padLeft(READY_TEXT_SIDE_PAD).padRight(READY_TEXT_SIDE_PAD)
        }
    }
}
