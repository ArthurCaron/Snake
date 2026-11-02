package fr.mwet.snake.tuning

import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator

object FontTuning {
    const val MIN_OUTLINE = 2
    const val TITLE_SIZE = 160
    const val TITLE_CHARS = "Snek"
    const val TITLE_BORDER = 5f
    const val OUTLINE_RATIO = 12
    const val MIN_SHADOW = 1
    const val SHADOW_RATIO = 18
    const val FRENCH_CHARS = "ÀÂÄÇÉÈÊËÎÏÔÖÙÛÜŸàâäçéèêëîïôöùûüÿœŒæÆ"
    const val TEXT_CHARS = FreeTypeFontGenerator.DEFAULT_CHARS + FRENCH_CHARS
}
