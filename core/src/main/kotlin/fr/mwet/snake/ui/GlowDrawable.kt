package fr.mwet.snake.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import fr.mwet.snake.save.settings.SettingsRepository
import fr.mwet.snake.tuning.Palette
import fr.mwet.snake.tuning.RenderTuning.Menu.GLOW_ALPHA
import fr.mwet.snake.tuning.RenderTuning.Menu.GLOW_WIDTH_SCALE
import fr.mwet.snake.tuning.RenderTuning.Menu.GLOW_HEIGHT_SCALE

// The button keeps its normal face. Only its active states add a soft halo.
class GlowDrawable(
    private val face: Drawable,
    private val glow: TextureRegion,
    private val settings: SettingsRepository,
) : BaseDrawable(face) {
    private val savedColor = Color()

    override fun draw(batch: Batch, x: Float, y: Float, width: Float, height: Float) {
        if (settings.appearance.menuGlow) {
            savedColor.set(batch.color)
            val src = batch.blendSrcFunc
            val dst = batch.blendDstFunc
            val srcAlpha = batch.blendSrcFuncAlpha
            val dstAlpha = batch.blendDstFuncAlpha
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE)
            batch.setColor(Palette.highlight.r, Palette.highlight.g, Palette.highlight.b, savedColor.a * GLOW_ALPHA)
            batch.draw(glow, x - width * (GLOW_WIDTH_SCALE - 1) / 2, y - height * (GLOW_HEIGHT_SCALE - 1) / 2,
                width * GLOW_WIDTH_SCALE, height * GLOW_HEIGHT_SCALE)
            batch.setBlendFunctionSeparate(src, dst, srcAlpha, dstAlpha)
            batch.color = savedColor
        }
        face.draw(batch, x, y, width, height)
    }
}
