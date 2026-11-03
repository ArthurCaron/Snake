package fr.mwet.tools.stylegenerator

// The files of a style that are not images: the style classes of the game, palette.json and the atlas settings.

// The colors the game reads from the style classes, also written to palette.json.
// The game draws its own text, dims and backgrounds with these colors (through core's tuning/Palette.kt), and tints
// the gray layers of the sprites with snake, marks and fruits: they are the colors that change without new images.
data class KotlinPalette(
    val clear: String,
    val outline: String,
    val text: String,
    val primary: String,
    val surround: String,
    val snake: String,
    val marks: String,
    val markShape: MarkShape,
    val fruits: List<Pair<FruitKind, String>>,
    val snakeTakesFruitColor: Boolean,
    val snakeGlow: Double,
    val fruitGlow: Double,
)

fun kotlinPalette(p: Palette): KotlinPalette {
    val overrides = p.styleColors
    return KotlinPalette(
        // The screen behind everything.
        clear = overrides?.clear ?: p.clear,
        // The game's text outlines and shadows, the screen dims, the shadow under the snake: the shade, not the line
        // color of the sprites.
        outline = overrides?.outline ?: p.shadeColor(),
        text = overrides?.text ?: p.text,
        // The highlighted labels (the new record) and the fill of the loading bar.
        primary = overrides?.primary ?: p.primaryLight,
        // Multiplies the floor outside the board.
        surround = overrides?.surround ?: p.surround(),
        // The tint of the gray layers: the gray() of Colors.kt is computed against these same colors.
        snake = p.snakeTint ?: p.snake,
        marks = p.markColor ?: p.snakePattern,
        markShape = p.markShape,
        fruits = p.fruitEntries(),
        snakeTakesFruitColor = p.snakeTakesFruitColor,
        snakeGlow = p.snakeGlow,
        fruitGlow = p.fruitGlow,
    )
}

// The layout of Python's json.dumps(indent=2), so that the file does not change with the port.
// palette.json sums up the game colors of the style in out/<style>; the game does not read it.
fun paletteJson(k: KotlinPalette): String {
    fun string(value: String) = "\"$value\""
    val fruits = k.fruits.joinToString(",\n") { (kind, color) -> "    [\n      ${string(kind.id)},\n      ${string(color)}\n    ]" }
    val entries = listOf(
        "clear" to string(k.clear),
        "outline" to string(k.outline),
        "text" to string(k.text),
        "primary" to string(k.primary),
        "surround" to string(k.surround),
        "snake" to string(k.snake),
        "marks" to string(k.marks),
        "markShape" to string(k.markShape.id),
        "fruits" to "[\n$fruits\n  ]",
        "snakeTakesFruitColor" to k.snakeTakesFruitColor.toString(),
        "snakeGlow" to k.snakeGlow.toString(),
        "fruitGlow" to k.fruitGlow.toString(),
    )
    return entries.joinToString(",\n", "{\n", "\n}\n") { (key, value) -> "  ${string(key)}: $value" }
}

// StyleColors.kt, SnakeLook.kt and FruitLook.kt of core/src/main/kotlin/fr/mwet/snake/render, by file name.
// Copied over the game files, they switch the game to this style (with the images of the same style).
fun kotlinSources(p: Palette): Map<String, String> {
    val k = kotlinPalette(p)
    fun color(value: String) = "Color.valueOf(\"${value.removePrefix("#")}\")"
    val fruits = k.fruits.joinToString("") { (kind, c) -> "\n        FruitStyle(FruitKind.${kind.title}, ${color(c)})," }
    val header = "package fr.mwet.snake.render\n\nimport com.badlogic.gdx.graphics.Color\n\n"
    return linkedMapOf(
        "StyleColors.kt" to header + """
            |object StyleColors {
            |    val clear: Color = ${color(k.clear)}
            |    val outline: Color = ${color(k.outline)}
            |    val text: Color = ${color(k.text)}
            |    val primary: Color = ${color(k.primary)}
            |    val surround: Color = ${color(k.surround)}
            |}
            |""".trimMargin(),
        "SnakeLook.kt" to header + """
            |object SnakeLook {
            |    val body: Color = ${color(k.snake)}
            |    val marks: Color = ${color(k.marks)}
            |    val markShape = MarkShape.${k.markShape.title}
            |    val takesFruitColor = ${k.snakeTakesFruitColor}
            |    val glow = ${k.snakeGlow}f
            |}
            |""".trimMargin(),
        "FruitLook.kt" to header + """
            |object FruitLook {
            |    val fruits: Array<FruitStyle> = arrayOf($fruits
            |    )
            |    val glow = ${k.fruitGlow}f
            |}
            |""".trimMargin(),
    )
}

// libGDX TexturePacker settings of the skin folder.
// They override sourceAssets/textures/pack.json for the skin folder. pot: pages of power-of-two sizes. padding 4,
// bleed, edgePadding and duplicatePadding: transparent pixels and the gap between two sprites get the edge colors, so
// linear filtering and mipmaps do not show dark fringes or a neighbour sprite. stripWhitespace false: each sprite keeps
// its full cell, so the game can draw it as a 1 by 1 square. useIndexes: fruitApple_0 and fruitApple_1 become the
// frames 0 and 1 of the region fruitApple. postGameboy switches the filters to Nearest.
val PACK_JSON = """
    |{
    |	pot: true,
    |	paddingX: 4,
    |	paddingY: 4,
    |	bleed: true,
    |	bleedIterations: 4,
    |	edgePadding: true,
    |	duplicatePadding: true,
    |	rotation: false,
    |	maxWidth: 2048,
    |	maxHeight: 2048,
    |	stripWhitespaceX: false,
    |	stripWhitespaceY: false,
    |	filterMin: MipMapLinearLinear,
    |	filterMag: Linear,
    |	wrapX: ClampToEdge,
    |	wrapY: ClampToEdge,
    |	format: RGBA8888,
    |	useIndexes: true,
    |	premultiplyAlpha: false,
    |	legacyOutput: true,
    |	prettyPrint: true
    |}
    |""".trimMargin()
