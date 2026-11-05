package fr.mwet.snake.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.ui.Widget
import fr.mwet.snake.assets.TextureHandler
import fr.mwet.snake.tuning.Palette

// A light border and a dark well keep both light and dark choices visible on either theme.
class SelectionBadge(textures: TextureHandler) : Widget() {
    private val pixel = textures.rectangleTexture(1, 1, Color.WHITE)
    private val tint = Color()
    private var symbol: TextureRegion? = null
    private var colorSwatch = true

    fun showColor(color: Color) { tint.set(color); colorSwatch = true }
    fun showSymbol(region: TextureRegion?, color: Color) { symbol = region; tint.set(color); colorSwatch = false }

    override fun getPrefWidth() = UiMetrics.CHOICE_BADGE_SIZE
    override fun getPrefHeight() = UiMetrics.CHOICE_BADGE_SIZE

    override fun draw(batch: Batch, parentAlpha: Float) {
        val saved = batch.packedColor
        val edge = UiMetrics.CHOICE_BADGE_BORDER
        batch.setColor(Palette.choiceBorder.r, Palette.choiceBorder.g, Palette.choiceBorder.b, parentAlpha)
        batch.draw(pixel, x, y, width, height)
        batch.setColor(Palette.choiceInnerBorder.r, Palette.choiceInnerBorder.g, Palette.choiceInnerBorder.b, parentAlpha)
        batch.draw(pixel, x + edge, y + edge, width - edge * 2, height - edge * 2)
        val inset = edge * 2
        if (colorSwatch) batch.setColor(tint.r, tint.g, tint.b, tint.a * parentAlpha)
        else batch.setColor(Palette.choiceBorder.r, Palette.choiceBorder.g, Palette.choiceBorder.b, parentAlpha)
        batch.draw(pixel, x + inset, y + inset, width - inset * 2, height - inset * 2)
        if (!colorSwatch) symbol?.let {
            batch.setColor(tint.r, tint.g, tint.b, tint.a * parentAlpha)
            batch.draw(it, x + inset, y + inset, width - inset * 2, height - inset * 2)
        }
        batch.packedColor = saved
    }
}
