package fr.mwet.tools.stylegenerator

import java.io.File
import kotlin.system.exitProcess

// Run from the repository root: ./gradlew :tools:style-generator:run --args="desert --out <folder>"
// The output folder, relative to the working directory: the Gradle run task starts in the repository root.
private const val DEFAULT_OUT = "tools/style-generator/out"

// Draws every file of one style into out (out/<style>), then its preview into the previews folder next to it.
// Each drawing function returns a Raster or Pixels; the file name decides where the image goes in the game: the skin
// folder is packed into the atlas, and a file named x.png becomes the region game/skin/x.
private fun generate(name: String, out: File) {
    val p = PALETTES.getValue(name)
    val skin = out.resolve("textures/game/skin")
    // The style folder is drawn again from nothing: a sprite removed from the code does not stay behind.
    out.deleteRecursively()
    skin.mkdirs()
    // One palette per group, with the outline color of that group.
    val snake = p.withLine(LineGroup.SNAKE)
    val fruit = p.withLine(LineGroup.FRUIT)
    val ui = p.withLine(LineGroup.UI)
    // The sprites drawn in one layer only, by file name without .png.
    val sprites = linkedMapOf<String, Pixels>(
        "snakePupil" to snakePupil(snake).pixels(),
        "snakeHeadMark" to markSprite(snake, chevron(10.0)).pixels(),
        "gridCell" to gridCell(p).pixels(),
        "glow" to lightGlow(),
        "iconPause" to iconPause(ui).pixels(),
        "iconSoundOn" to iconSound(ui, true).pixels(),
        "iconSoundOff" to iconSound(ui, false).pixels(),
        "iconMusicOn" to iconMusic(p, true).pixels(),
        "iconMusicOff" to iconMusic(p, false).pixels(),
        "flagEnglish" to flagEnglish(ui).pixels(),
        "flagFrench" to flagFrench(ui).pixels(),
    )
    // Every mark shape is drawn; SnakeLook.markShape picks the one the game shows.
    for (shape in MarkShape.entries) sprites["mark${shape.title}"] = markSprite(snake, markPath(shape)).pixels()
    // The two-layer sprites: snakeHead.png is the gray base that the game tints, snakeHeadDetail.png the fixed layer.
    for (kind in Layer.entries) {
        val suffix = if (kind == Layer.BASE) "" else "Detail"
        val snakeLayer = snake.layer(Part.SNAKE, kind)
        val fruitLayer = fruit.layer(Part.FRUIT, kind)
        sprites["snakeHead$suffix"] = snakeHead(snakeLayer).pixels()
        sprites["snakeHeadDead$suffix"] = snakeHead(snakeLayer, dead = true).pixels()
        sprites["snakeBodyStraight$suffix"] = snakeBodyStraight(snakeLayer).pixels()
        sprites["snakeBodyTurn$suffix"] = snakeBodyTurn(snakeLayer).pixels()
        sprites["snakeTail$suffix"] = snakeTail(snakeLayer).pixels()
        for (fruitKind in FruitKind.entries) {
            val sprite = "fruit${fruitKind.title}"
            // Still: without a shadow. _0 and _1: the two frames of the bounce, the second squashed by 7 %.
            sprites["${sprite}Still$suffix"] = fruitSprite(fruitLayer, fruitKind, shadow = false).pixels()
            sprites["$sprite${suffix}_0"] = fruitSprite(fruitLayer, fruitKind, 0.0).pixels()
            sprites["$sprite${suffix}_1"] = fruitSprite(fruitLayer, fruitKind, 0.07).pixels()
        }
    }
    for ((spriteName, pixels) in sprites) pixels.write(skin.resolve("$spriteName.png"))
    // The checker ground is kept as a Raster: the splash screen repeats it.
    val tile = groundTile(p)
    tile.pixels().write(skin.resolve("ground.png"))
    for ((groundName, drawGround) in GROUNDS) drawGround(p).pixels().write(skin.resolve("$groundName.png"))
    boardFrame(p).write(skin.resolve("boardFrame.png"))
    // The nine-patches. Pressed, a button takes its dark color as its face and its base color as its light strip.
    buttonImage(ui, p.button, p.buttonLight, p.buttonDark).write(skin.resolve("button.9.png"))
    buttonImage(ui, p.buttonDark, p.button, p.buttonDark, pressed = true).write(skin.resolve("buttonDown.9.png"))
    buttonImage(ui, p.primary, p.primaryLight, p.primaryDark, line = p.primary).write(skin.resolve("buttonPrimary.9.png"))
    buttonImage(ui, p.primaryDark, p.primary, p.primaryDark, pressed = true, line = p.primary)
        .write(skin.resolve("buttonPrimaryDown.9.png"))
    // Under the mouse, the face and the bevel are HOVER_TINT of the way to white.
    buttonImage(ui, tintHex(p.button, HOVER_TINT), p.buttonLight, tintHex(p.buttonDark, HOVER_TINT)).write(skin.resolve("buttonOver.9.png"))
    buttonImage(ui, tintHex(p.primary, HOVER_TINT), p.primaryLight, tintHex(p.primaryDark, HOVER_TINT), line = p.primary)
        .write(skin.resolve("buttonPrimaryOver.9.png"))
    panelImage(ui).write(skin.resolve("panel.9.png"))
    skin.resolve("pack.json").writeText(PACK_JSON)

    // The images that the game shows before the atlas is loaded.
    splash(p, tile).pixels().write(out.resolve("root/SplashScreen.png"))
    loadingText(p).pixels().write(out.resolve("root/LoadingAssets.png"))

    // The launcher icons, all resized from one 512 px image. The head is drawn in one layer, in its real colors, with
    // the pupils moved forward (toward x = 0).
    val icon = appIcon(p, snakeHead(snake, look = -PUPIL_TRAVEL to 0.0)).pixels()
    val icons = out.resolve("icons")
    for (size in listOf(16, 32, 64, 128)) icon.resized(size, size, Resample.LANCZOS).write(icons.resolve("icon$size.png"))
    for ((density, size) in listOf("mdpi" to 48, "hdpi" to 72, "xhdpi" to 96, "xxhdpi" to 144, "xxxhdpi" to 192)) {
        icon.resized(size, size, Resample.LANCZOS).write(icons.resolve("drawable-$density/ic_launcher.png"))
    }
    icon.write(icons.resolve("logo.png"))
    writeIco(icon, listOf(16, 32, 48, 64, 128, 256), icons.resolve("logo.ico"))
    writeIco(icon, listOf(16, 32, 48), icons.resolve("favicon.ico"))
    writeIcns(icon, icons.resolve("logo.icns"))

    // The style classes of the game, and the same colors in palette.json.
    out.resolve("palette.json").writeText(paletteJson(kotlinPalette(p)))
    val kotlin = out.resolve("kotlin")
    kotlin.mkdirs()
    for ((fileName, source) in kotlinSources(p)) kotlin.resolve(fileName).writeText(source)

    // The post-effect reworks the files written above, in place.
    when (p.post) {
        PostEffect.GAMEBOY -> postGameboy(out)
        PostEffect.NEON -> postNeon(out)
        PostEffect.LASER -> postLaser(out, p)
        null -> Unit
    }

    // The preview is drawn from the files on disk, so it shows the files that would be copied to the game. The gameboy
    // preview also goes through the four greens: it draws its texts and backgrounds with the gray palette, where the
    // game uses the greens of styleColors.
    var sheet = renderPreview(p, out)
    if (p.post == PostEffect.GAMEBOY) sheet = gameboyPixels(sheet, factor = 2, hardAlpha = false)
    sheet.write(out.parentFile.resolve("previews/$name.png"))
}

// Arguments: style names (none means every style) and --out <folder>.
// The summary sheets at the end read the styles already in out, drawn by this run or by an earlier one: drawing a single
// style updates its own column and keeps the others.
fun main(args: Array<String>) {
    var out = File(DEFAULT_OUT)
    val styles = mutableListOf<String>()
    var i = 0
    while (i < args.size) {
        when (val arg = args[i]) {
            "--out" -> out = File(args.getOrNull(++i) ?: usage("--out needs a folder"))
            else -> if (arg in PALETTES) styles += arg else usage("Unknown style: $arg")
        }
        i++
    }
    for (name in styles.ifEmpty { PALETTES.keys.toList() }) {
        generate(name, out.resolve(name))
        println("$name: ${out.resolve(name)}")
    }
    val previews = out.resolve("previews")
    val names = PALETTES.keys.filter { out.resolve(it).isDirectory }
    overview(previews, names).write(previews.resolve("all.png"))
    val markStyles = listOf("desert", "laser").filter { it in names }
    if (markStyles.isNotEmpty()) marksSheet(out, markStyles).write(previews.resolve("marks.png"))
    fruitsSheet(out, names).write(previews.resolve("fruits.png"))
    groundsSheet(out, names).write(previews.resolve("grounds.png"), withAlpha = false)
}

private fun usage(message: String): Nothing {
    System.err.println(message)
    System.err.println("Usage: [style ...] [--out folder]. Styles: ${PALETTES.keys.joinToString(", ")}")
    exitProcess(1)
}
