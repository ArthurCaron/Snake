package fr.mwet.snake.tuning

import com.badlogic.gdx.graphics.Color
import fr.mwet.snake.render.StyleColors

object TransitionTuning {
    val FADE_COLOR: Color = StyleColors.outline
    val WIPE_COLOR: Color = StyleColors.outline
    const val FADE_OUT_SECONDS = 0.1f
    const val FADE_IN_SECONDS = 0.15f
    const val SLIDE_SECONDS = 0.35f
    const val WIPE_SECONDS = 0.4f
    const val WIPE_CELLS_ON_SHORT_SIDE = 9
}
