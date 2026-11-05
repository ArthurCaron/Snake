package fr.mwet.snake.render

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion
import com.badlogic.gdx.utils.Array
import fr.mwet.snake.assets.GameSkin

class FruitStyle(val kind: FruitKind, val color: Color)

class FruitSprites(
    val bob: Array<AtlasRegion>,
    val bobDetail: Array<AtlasRegion>,
    val still: AtlasRegion,
    val stillDetail: AtlasRegion,
)

enum class FruitKind {
    Apple,
    Strawberry,
    Cherry,
    Banana,
    Grapes,
    Orange,
    Pear,
    Lemon,
    Watermelon,
    Prickly;

    fun sprites(skin: GameSkin): FruitSprites = when (this) {
        Apple -> FruitSprites(skin.fruitApple, skin.fruitAppleDetail, skin.fruitAppleStill, skin.fruitAppleStillDetail)
        Strawberry -> FruitSprites(skin.fruitStrawberry, skin.fruitStrawberryDetail, skin.fruitStrawberryStill, skin.fruitStrawberryStillDetail)
        Cherry -> FruitSprites(skin.fruitCherry, skin.fruitCherryDetail, skin.fruitCherryStill, skin.fruitCherryStillDetail)
        Banana -> FruitSprites(skin.fruitBanana, skin.fruitBananaDetail, skin.fruitBananaStill, skin.fruitBananaStillDetail)
        Grapes -> FruitSprites(skin.fruitGrapes, skin.fruitGrapesDetail, skin.fruitGrapesStill, skin.fruitGrapesStillDetail)
        Orange -> FruitSprites(skin.fruitOrange, skin.fruitOrangeDetail, skin.fruitOrangeStill, skin.fruitOrangeStillDetail)
        Pear -> FruitSprites(skin.fruitPear, skin.fruitPearDetail, skin.fruitPearStill, skin.fruitPearStillDetail)
        Lemon -> FruitSprites(skin.fruitLemon, skin.fruitLemonDetail, skin.fruitLemonStill, skin.fruitLemonStillDetail)
        Watermelon -> FruitSprites(skin.fruitWatermelon, skin.fruitWatermelonDetail, skin.fruitWatermelonStill, skin.fruitWatermelonStillDetail)
        Prickly -> FruitSprites(skin.fruitPrickly, skin.fruitPricklyDetail, skin.fruitPricklyStill, skin.fruitPricklyStillDetail)
    }
}
