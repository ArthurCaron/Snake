package fr.mwet.snake.ui

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.backends.headless.HeadlessFiles
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import ktx.scene2d.Scene2DSkin
import ktx.scene2d.scene2d
import ktx.scene2d.table
import kotlin.test.Test
import kotlin.test.assertEquals

class WidgetsTest {
    @Test
    fun `both button helpers report one activation after the action and ignore disabled or cancelled clicks`() {
        val previousFiles = Gdx.files
        Gdx.files = HeadlessFiles()
        val font = BitmapFont(BitmapFont.BitmapFontData(), TextureRegion(), false)
        val skin = Skin().apply { add("default", TextButton.TextButtonStyle().apply { this.font = font }) }
        Scene2DSkin.defaultSkin = skin
        try {
            val calls = mutableListOf<String>()
            val widgets = Widgets { calls += "activated" }
            val standalone = widgets.menuButton("Standalone", onClick = { calls += "action" })
            val table = scene2d.table {}
            val nested = with(widgets) { table.menuButton("Nested", onClick = { calls += "action" }) }

            for (button in listOf(standalone, nested)) {
                button.setBounds(0f, 0f, 100f, 50f)
                button.click()
                assertEquals(listOf("action", "activated"), calls)
                calls.clear()

                button.isChecked = !button.isChecked
                button.click(releaseX = 200f)
                button.isDisabled = true
                button.click()
                assertEquals(emptyList(), calls)
            }
        } finally {
            skin.dispose()
            Gdx.files = previousFiles
        }
    }

    @Test
    fun `feedback observes the audio setting after the action`() {
        var soundOn = true
        var sounds = 0
        val widgets = Widgets { if (soundOn) sounds++ }

        widgets.activateButtonAction { soundOn = false }
        assertEquals(0, sounds)
        widgets.activateButtonAction { soundOn = true }
        assertEquals(1, sounds)
    }
}

private fun TextButton.click(releaseX: Float = 10f) {
    fire(InputEvent().apply {
        type = InputEvent.Type.touchDown
        touchFocus = false
        stageX = 10f
        stageY = 10f
        pointer = 0
        button = 0
    })
    fire(InputEvent().apply {
        type = InputEvent.Type.touchUp
        stageX = releaseX
        stageY = 10f
        pointer = 0
        button = 0
    })
}
