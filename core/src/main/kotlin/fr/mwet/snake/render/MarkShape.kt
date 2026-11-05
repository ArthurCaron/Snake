package fr.mwet.snake.render

import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion
import fr.mwet.snake.assets.GameSkin

enum class MarkShape {
    None,
    Diamond,
    Circle,
    Square,
    Star,
    Chevron,
    Heart;

    fun region(skin: GameSkin): AtlasRegion? = when (this) {
        None -> null
        Diamond -> skin.markDiamond
        Circle -> skin.markCircle
        Square -> skin.markSquare
        Star -> skin.markStar
        Chevron -> skin.markChevron
        Heart -> skin.markHeart
    }
}
