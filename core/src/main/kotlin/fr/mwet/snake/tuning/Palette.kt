package fr.mwet.snake.tuning

import com.badlogic.gdx.graphics.Color
import fr.mwet.snake.render.StyleColors

object Palette {
    val screenClear: Color = StyleColors.clear
    val boardSurround: Color = StyleColors.surround
    val gridLine: Color = Color(1f, 1f, 1f, 0.1f)

    val snakeShadow: Color = StyleColors.outline.withAlpha(0.28f)

    val fontFill: Color = StyleColors.text
    val fontOutline: Color = StyleColors.outline
    val fontShadow: Color = StyleColors.outline
    val label: Color = StyleColors.text
    val title: Color = StyleColors.text
    val highlight: Color = StyleColors.primary
    val buttonLabel: Color = StyleColors.text
    val bigButtonLabel: Color = StyleColors.text

    val pauseDim: Color = StyleColors.outline.withAlpha(0.55f)
    val readyDim: Color = StyleColors.outline.withAlpha(0.3f)

    val loadingBarPixel: Color = StyleColors.text
    val loadingBarTrack: Color = StyleColors.outline.withAlpha(0.6f)
    val loadingBarFill: Color = StyleColors.primary
}

private fun Color.withAlpha(alpha: Float) = Color(r, g, b, alpha)
