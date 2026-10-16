package fr.mwet.snake.render

import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion
import fr.mwet.snake.generated.assets.TextureAssets

enum class MarkShape {
    Diamond,
    Circle,
    Square,
    Star,
    Chevron,
    Heart;

    fun region(skin: TextureAssets.Game.Skin): AtlasRegion = when (this) {
        Diamond -> skin.markDiamond
        Circle -> skin.markCircle
        Square -> skin.markSquare
        Star -> skin.markStar
        Chevron -> skin.markChevron
        Heart -> skin.markHeart
    }
}
