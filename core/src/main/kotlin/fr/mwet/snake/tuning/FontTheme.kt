package fr.mwet.snake.tuning

import fr.mwet.snake.generated.assets.FontAssets

enum class FontTheme(
    val tag: String,
    val displayName: String,
    val titleFile: String,
    val textFile: String,
    val pixel: Boolean,
    val smallSize: Int,
    val bigSize: Int,
) {
    Classic("classic", "Chewy + Fredoka", FontAssets.Chewy.ChewyRegular, FontAssets.Fredoka.FredokaRegular, false, 36, 60),
    Silkscreen("silkscreen", "Silkscreen", FontAssets.Silkscreen.SilkscreenBold, FontAssets.Silkscreen.SilkscreenRegular, true, 32, 64),
    PixelifySans("pixelify", "Pixelify Sans", FontAssets.Pixelifysans.PixelifySansVariable, FontAssets.Pixelifysans.PixelifySansVariable, true, 36, 64),
    PressStart2P("pressstart2p", "Press Start 2P", FontAssets.Pressstart2p.PressStart2PRegular, FontAssets.Pressstart2p.PressStart2PRegular, true, 24, 48),
    Vt323("vt323", "VT323", FontAssets.Vt323.VT323Regular, FontAssets.Vt323.VT323Regular, true, 40, 72);

    companion object {
        fun fromTag(tag: String): FontTheme? = entries.firstOrNull { it.tag == tag }
    }
}
