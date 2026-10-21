package fr.mwet.tools.translationpreview

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.scenes.scene2d.ui.Widget
import fr.mwet.tools.translationpreview.PreviewMetrics.BLOCK_MARGIN
import fr.mwet.tools.translationpreview.PreviewMetrics.BLOCK_OUTLINE


class BlockPreview(private val occurrence: Occurrence, private val pixel: Texture) : Widget() {
    private val previous = Matrix4()
    private val shifted = Matrix4()

    override fun getPrefWidth(): Float = maxOf(occurrence.block.width, occurrence.label.prefWidth) + BLOCK_MARGIN * 2

    override fun getPrefHeight(): Float = occurrence.block.height + BLOCK_MARGIN * 2

    override fun draw(batch: Batch, parentAlpha: Float) {
        val block = occurrence.block
        previous.set(batch.transformMatrix)
        batch.transformMatrix = shifted.set(previous).translate(x + BLOCK_MARGIN - block.x, y + BLOCK_MARGIN - block.y, 0f)
        block.draw(batch, parentAlpha)
        batch.transformMatrix = previous
        if (occurrence.overflows) drawOutline(batch)
    }

    private fun drawOutline(batch: Batch) {
        batch.color = PreviewPalette.overflow
        batch.draw(pixel, x, y, width, BLOCK_OUTLINE)
        batch.draw(pixel, x, y + height - BLOCK_OUTLINE, width, BLOCK_OUTLINE)
        batch.draw(pixel, x, y, BLOCK_OUTLINE, height)
        batch.draw(pixel, x + width - BLOCK_OUTLINE, y, BLOCK_OUTLINE, height)
        batch.color = Color.WHITE
    }
}
