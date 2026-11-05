package fr.mwet.snake.ui

import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.backends.headless.HeadlessFiles
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.ui.TextField
import com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable
import java.lang.reflect.Proxy
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MenuSizingTest {
    @Test
    fun `a vertical scrollbar never clips button borders or changes the preferred width`() = withFont { font ->
        val content = Table()
        val style = TextButton.TextButtonStyle().apply { this.font = font }
        val buttons = List(12) {
            TextButton("Glow du serpent : désactivé", style).apply {
                pad(16f, 24f, 16f, 24f)
                content.add(this).fillX().row()
            }
        }
        val scroll = MenuScrollPane(content, ScrollPane.ScrollPaneStyle().apply {
            vScrollKnob = BaseDrawable().apply { minWidth = 6f; minHeight = 24f }
        }).apply { setScrollingDisabled(true, false) }
        val preferredWidth = scroll.prefWidth
        scroll.setSize(preferredWidth, 300f)
        scroll.validate()
        content.validate()

        assertTrue(scroll.isScrollY)
        assertEquals(preferredWidth, scroll.prefWidth)
        buttons.forEach {
            assertTrue(it.x >= 0f, "Left border outside the viewport")
            assertTrue(it.x + it.width <= scroll.scrollWidth, "Right border outside the viewport")
        }
    }

    @Test
    fun `hex text stays fully visible with or without the hash and with the cursor at either end`() = withFont { font ->
        val style = TextField.TextFieldStyle().apply {
            this.font = font
            background = BaseDrawable().apply { leftWidth = 24f; rightWidth = 24f }
            cursor = BaseDrawable().apply { minWidth = 2f }
        }
        val field = VisibleTextField(style)
        field.width = field.widthForCharacters("#0123456789abcdefABCDEF", 7)
        for (text in listOf("abcdef", "f5e2b0", "#12ABef")) {
            field.text = text
            for (cursor in listOf(0, text.length)) {
                field.cursorPosition = cursor
                assertEquals(0 to text.length, field.visibleRange())
            }
        }
    }

    private class VisibleTextField(style: TextFieldStyle) : TextField("", style) {
        fun visibleRange(): Pair<Int, Int> {
            calculateOffsets()
            return visibleTextStart to visibleTextEnd
        }
    }

    private fun withFont(test: (BitmapFont) -> Unit) {
        val previousFiles = Gdx.files
        val previousApp = Gdx.app
        Gdx.files = HeadlessFiles()
        Gdx.app = Proxy.newProxyInstance(Application::class.java.classLoader, arrayOf(Application::class.java)) { _, _, _ -> null } as Application
        val font = BitmapFont(BitmapFont.BitmapFontData(), TextureRegion(), false)
        font.data.apply {
            lineHeight = 32f
            capHeight = 24f
            spaceXadvance = 24f
            for (code in 32..255) setGlyph(code, BitmapFont.Glyph().apply {
                id = code
                xadvance = 24
                width = 24
                height = 32
            })
        }
        try { test(font) } finally {
            font.dispose()
            Gdx.files = previousFiles
            Gdx.app = previousApp
        }
    }
}
