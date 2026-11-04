package fr.mwet.tools.stylegenerator

// The post-effect that reworks the written files of an "other style": see PostEffects.kt.
enum class PostEffect { NEON, GAMEBOY, LASER }

// The shape of the marks along the snake's back. Every shape is drawn for every style (markDiamond.png and so on);
// the game shows the one of SnakeLook.markShape. id is the name in palette.json, title the one in file and class names.
enum class MarkShape {
    DIAMOND, CIRCLE, SQUARE, STAR, CHEVRON, HEART;

    val id = name.lowercase()
    val title = id.replaceFirstChar { it.uppercase() }
}

// Replaces the game colors computed from the palette in StyleColors.kt and palette.json.
// For a style whose post-effect recolors the images: the game colors are not post-processed, so gameboy gives its four
// greens here. Each field replaces the KotlinPalette field of the same name (see Outputs.kt).
data class StyleColorOverrides(
    val clear: String,
    val outline: String,
    val text: String,
    val primary: String,
    val surround: String,
)

// Every color and switch of one style. The drawing functions read the keys they need; nothing else in the code
// depends on the style, so a new entry in PALETTES is a new style.
// Colors are "#rrggbb", "#rrggbbaa" or CLEAR. The optional colors fall back on another one where they are read.
// The snake and the fruits are drawn in two layers (see layer() in Colors.kt): a gray base layer that the game
// multiplies by a color, and a detail layer drawn as it is. In the base layer, a body color only gives the shading:
// the hue on screen is the tint of the game. A key below that "shades the base layer" changes how light or dark that
// part looks under the tint, not its hue.
data class Palette(
    // The style name in the title of the preview.
    val label: String,

    // Lines, background and text.
    // The dark line around the shapes: snake, fruits, buttons, panel, icons, flags, grid cell, app icon. Each
    // group can have its own color (lineSnake and the others below). Also the default of shade, and with it of the
    // text borders and of the game's StyleColors.outline.
    val outline: String,
    // The color behind everything in the game (StyleColors.clear). Lightened, it gives StyleColors.surround, which
    // multiplies the floor outside the board (see surround() in Colors.kt).
    val clear: String,
    // The fill of all texts (splash, loading, previews, the game's StyleColors.text) and the symbols of the icons.
    val text: String,

    // Floor.
    // The board floor: the base color of every ground tile. The lighter and darker details of the grounds are computed
    // from it with tintHex().
    val ground: String,
    // The darker cells of the checker ground (drawn at 35 %) and the lines of the other grounds. When it is too close
    // to ground (a luminance gap of 0.04 or less), the grounds draw their lines in outline at 30 % instead.
    val grout: String,

    // Snake.
    // The snake body. It shades the base layer: the base draws it in white when there is no snakeTint. Without
    // snakeTint, it is also the snake color in the game (SnakeLook.body): changing it recolors the snake.
    val snake: String,
    // The two light stripes along the body and the head. The layers replace it with a half-transparent white, so this
    // value shows only on the app icon.
    val snakeLight: String,
    // The dark band, 6 px wide, inside the edge of the body. It shades the base layer: a darker value gives a deeper
    // band under any tint.
    val snakeDark: String,
    // The color of the marks along the back (SnakeLook.marks) when markColor is not set. The marks are white sprites
    // that the game tints, so this color changes without new images.
    val snakePattern: String,
    // The white of the eyes (detail layer).
    val eye: String,
    // The pupil sprite that the game moves toward the fruit (snakePupil.png) and the cross of the dead eyes.
    val pupil: String,

    // Fruits.
    // The fruit body. It shades the base layer only: the game tints each fruit with its color from FruitLook.fruits
    // (fruitColors, or the natural color of the kind). The base draws it in white when there is no fruitTint.
    val fruit: String,
    // The highlight spot of the fruits. The layers replace it with a 75 % white: this value has no visible effect.
    val fruitLight: String,
    // The darker lower right part of the fruits, and the dots of the prickly fruit and the orange. It shades the base
    // layer: a darker value gives a deeper shadow under any tint.
    val fruitDark: String,
    // The seeds of the strawberry and of the prickly fruit (detail layer).
    val seed: String,
    // The leaves of the fruits (detail layer); leafDark is the vein of the apple, pear and grapes leaves.
    val leaf: String,
    val leafDark: String,

    // Buttons and panel.
    // The button nine-patch (button.9.png): the face, the light strip near the top, and the dark bevel at the bottom.
    // Pressed (buttonDown.9.png), the face takes buttonDark and the strip takes button. The app icon uses the same three.
    val button: String,
    val buttonLight: String,
    val buttonDark: String,
    // The primary button (buttonPrimary.9.png) with the same three roles. primaryLight is also the game's highlight
    // (StyleColors.primary: the highlighted labels and the loading bar).
    val primary: String,
    val primaryLight: String,
    val primaryDark: String,
    // The panel nine-patch (panel.9.png): the face, the bottom bevel and the inner groove line.
    val panel: String,
    val panelDark: String,
    val panelLine: String,
    // The 2 px line and the soft shadow along the board edges (boardFrame.png).
    val frame: String,

    // Optional colors.
    // Text shadows and dims; defaults to the outline.
    // Also the border of the texts, the fruit shadow and the game's StyleColors.outline (text outlines, screen dims,
    // the shadow under the snake). Set it when the outline is a bright line color (neon, laser): the shadows stay dark.
    val shade: String? = null,
    // Outline of one group of assets; defaults to the outline.
    // lineSnake: the snake parts, the outline of the marks and the head of the app icon. lineFruit: the fruits. lineUi:
    // the buttons, the panel, the icons and the flags. The laser effect finds the lines of the UI by its color.
    val lineSnake: String? = null,
    val lineFruit: String? = null,
    val lineUi: String? = null,
    // Color the game tints the gray layer with; defaults to the snake or fruit color.
    // The grays of the base layer are computed against it: gray = luminance(color) / luminance(tint). With the default,
    // the body is white and takes the full tint. A bright tint over a dark snake or fruit, as in the laser styles,
    // keeps the body dark and gives the full color to the white outline only. snakeTint is also SnakeLook.body;
    // the fruits are tinted with FruitLook.fruits, so fruitTint only sets the grays.
    val snakeTint: String? = null,
    val fruitTint: String? = null,

    // Switches.
    // The outline goes to the tinted layer, so it takes the snake or fruit color.
    val tintOutline: Boolean = false,
    // SnakeLook.takesFruitColor: the snake takes the color of the fruit it eats, from the head to the tail.
    val snakeTakesFruitColor: Boolean = false,
    // Back marks drawn as a white line only, without outline.
    // A 3.5 px line without fill; markOutline is then not used.
    val tubeMarks: Boolean = false,
    // Defaults to the snake pattern.
    // markColor: the color the game tints the marks with (SnakeLook.marks). markOutline: the ring around the filled
    // marks, 90 % opaque, 1.75 px visible; defaults to the snake outline.
    val markColor: String? = null,
    val markOutline: String? = null,
    // SnakeLook.markShape: the mark the game draws along the back.
    val markShape: MarkShape = MarkShape.DIAMOND,
    // Spread over the fruit kinds in order; without them, each kind keeps its natural color.
    // FruitLook.fruits: the list cycles over the ten kinds of FruitKind (kind i takes color i modulo the list size).
    val fruitColors: List<String>? = null,
    // SnakeLook.glow and FruitLook.glow: the opacity of the additive light that the game draws under each part of the
    // snake and under the fruit (glow.png). 0 turns it off.
    val snakeGlow: Double = 0.0,
    val fruitGlow: Double = 0.0,
    // Buttons drawn as a neon tube: a 4.5 px line on the dark color, without bevel or light strip. Pressed, the light
    // color covers the face at 30 %. The primary buttons draw their line in primary.
    val tubeButtons: Boolean = false,
    // The post-effect that reworks the files once they are written; null for the cartoon styles.
    val post: PostEffect? = null,
    // Replaces the game colors computed from this palette; see StyleColorOverrides.
    val styleColors: StyleColorOverrides? = null,

    // Colors shared by every style unless a style sets them.
    // The tongue of the dead head (detail layer).
    val tongue: String = "#e0475a",
    // Defaults to the shade.
    // The shadow under the fruit, drawn at 25 %. The Still frames have none.
    val fruitShadow: String? = null,
    // The stems of the apple, pear, grapes and cherry, and the two ends of the banana.
    val stem: String = "#6b4a2a",
    // The watermelon rind (a 6.5 px band along the round edge) and the 2 px dark line at its outer edge.
    val rind: String = "#4f9a46",
    val rindDark: String = "#2d6233",
    // The watermelon pips.
    val pip: String = "#2a1a18",
)

// The styles, in the order of the previews. The key is the style name on the command line and the folder name in the
// output. The six cartoon styles only change colors; the "other styles" also use the switches and a post-effect.
val PALETTES: Map<String, Palette> = linkedMapOf(
    // The style of the game files today.
    "desert" to Palette(
        label = "Desert",
        outline = "#3b2220",
        clear = "#8a4a32",
        text = "#fff3da",
        ground = "#ecca94",
        grout = "#dcb078",
        snake = "#3f9b92",
        snakeLight = "#73c7b4",
        snakeDark = "#2a6d6d",
        snakePattern = "#f5e2b0",
        eye = "#fff8e6",
        pupil = "#2a1918",
        fruit = "#d8467b",
        fruitLight = "#f589aa",
        fruitDark = "#9b2a58",
        seed = "#ffe0a8",
        leaf = "#8fb05a",
        leafDark = "#5d7a3a",
        button = "#e27b3e",
        buttonLight = "#f6a465",
        buttonDark = "#a54d28",
        primary = "#3fa3a6",
        primaryLight = "#78d0c8",
        primaryDark = "#296f78",
        panel = "#f7e1b2",
        panelDark = "#e5bf86",
        panelLine = "#c4925c",
        frame = "#3b2220",
    ),
    "oasis" to Palette(
        label = "Oasis",
        outline = "#1f2a22",
        clear = "#2f5a3c",
        text = "#fbf6dc",
        ground = "#8fb46a",
        grout = "#759a52",
        snake = "#e46a2e",
        snakeLight = "#f7a14f",
        snakeDark = "#a8401f",
        snakePattern = "#2b2420",
        eye = "#fffbe8",
        pupil = "#1d1a14",
        fruit = "#f2b630",
        fruitLight = "#ffe07a",
        fruitDark = "#c27a1c",
        seed = "#fff1b0",
        leaf = "#4f9a46",
        leafDark = "#2d6233",
        button = "#3d8a5a",
        buttonLight = "#68b77d",
        buttonDark = "#25583d",
        primary = "#e9a23b",
        primaryLight = "#ffd27a",
        primaryDark = "#a8641f",
        panel = "#f3ecc8",
        panelDark = "#d9cf98",
        panelLine = "#9fae6a",
        frame = "#1f2a22",
    ),
    "tundra" to Palette(
        label = "Tundra",
        outline = "#22263f",
        clear = "#3b4a7a",
        text = "#ffffff",
        ground = "#dde9f3",
        grout = "#c7d8ea",
        snake = "#7a5cc6",
        snakeLight = "#a68be6",
        snakeDark = "#523d93",
        snakePattern = "#ffffff",
        eye = "#ffffff",
        pupil = "#1d1b33",
        fruit = "#e2424f",
        fruitLight = "#ff8a8f",
        fruitDark = "#9c2234",
        seed = "#ffd7d0",
        leaf = "#3f8f86",
        leafDark = "#26605e",
        button = "#4f8fd1",
        buttonLight = "#86bdf0",
        buttonDark = "#30609c",
        primary = "#e2424f",
        primaryLight = "#ff8a8f",
        primaryDark = "#9c2234",
        panel = "#f4f8fd",
        panelDark = "#cfdcee",
        panelLine = "#93a9cc",
        frame = "#22263f",
    ),
    "volcano" to Palette(
        label = "Volcano",
        outline = "#1a1012",
        clear = "#2a1418",
        text = "#ffe9c9",
        ground = "#4a3434",
        grout = "#e5562a",
        snake = "#f2c13a",
        snakeLight = "#ffe27a",
        snakeDark = "#b8802a",
        snakePattern = "#2a1414",
        eye = "#fff4dc",
        pupil = "#1a0e0e",
        fruit = "#45c6e8",
        fruitLight = "#a8ecff",
        fruitDark = "#24789c",
        seed = "#ffffff",
        leaf = "#3c2a2a",
        leafDark = "#1a1012",
        button = "#c8402a",
        buttonLight = "#f07a4a",
        buttonDark = "#82231e",
        primary = "#f2c13a",
        primaryLight = "#ffe27a",
        primaryDark = "#b8802a",
        panel = "#5b4140",
        panelDark = "#433030",
        panelLine = "#e5562a",
        frame = "#1a1012",
    ),
    "dusk" to Palette(
        label = "Dusk dunes",
        outline = "#1f1630",
        clear = "#2b1f45",
        text = "#fff0e0",
        ground = "#a2709a",
        grout = "#8e6290",
        snake = "#f2b04a",
        snakeLight = "#ffd98a",
        snakeDark = "#c06f30",
        snakePattern = "#2d1f3f",
        eye = "#fff8ea",
        pupil = "#1f1630",
        fruit = "#5fd0c8",
        fruitLight = "#a8f0e6",
        fruitDark = "#2f8b94",
        seed = "#e8fff8",
        leaf = "#e98bb0",
        leafDark = "#a85380",
        button = "#6f4c9c",
        buttonLight = "#9a78c8",
        buttonDark = "#47306e",
        primary = "#f2b04a",
        primaryLight = "#ffd98a",
        primaryDark = "#c06f30",
        panel = "#f6e3d6",
        panelDark = "#ddbfb6",
        panelLine = "#a07ba0",
        frame = "#1f1630",
    ),
    "lagoon" to Palette(
        label = "Lagoon",
        outline = "#16303a",
        clear = "#1f7a8c",
        text = "#ffffff",
        ground = "#f4e2b8",
        grout = "#f4e2b8",
        snake = "#f0716c",
        snakeLight = "#ffa59a",
        snakeDark = "#b8444c",
        snakePattern = "#fff3d8",
        eye = "#ffffff",
        pupil = "#16303a",
        fruit = "#ffcf3f",
        fruitLight = "#fff09a",
        fruitDark = "#e08f1f",
        seed = "#fff6c8",
        leaf = "#3fae74",
        leafDark = "#25784d",
        button = "#2f9fb2",
        buttonLight = "#6fd0dc",
        buttonDark = "#1f6b80",
        primary = "#f0716c",
        primaryLight = "#ffa59a",
        primaryDark = "#b8444c",
        panel = "#fff6e0",
        panelDark = "#efd9a8",
        panelLine = "#7cc6c8",
        frame = "#16303a",
    ),
    // Bright cyan lines on a night floor. shade keeps the text borders dark; the NEON effect adds the glow.
    "neon" to Palette(
        label = "Neon (other style)",
        outline = "#3ef3ff",
        shade = "#0b0620",
        clear = "#0b0620",
        text = "#ffffff",
        ground = "#140c30",
        grout = "#0e0824",
        snake = "#103a3a",
        snakeLight = "#3ef3ff",
        snakeDark = "#0a2424",
        snakePattern = "#b6ff3e",
        eye = "#ffffff",
        pupil = "#0b0620",
        fruit = "#ff2b6e",
        fruitLight = "#ff9ac0",
        fruitDark = "#a0104a",
        seed = "#ffffff",
        leaf = "#b6ff3e",
        leafDark = "#4a8a10",
        button = "#2a1060",
        buttonLight = "#ff3fd8",
        buttonDark = "#160838",
        primary = "#ff2b9e",
        primaryLight = "#ff9ad6",
        primaryDark = "#8a0f5a",
        panel = "#160c34",
        panelDark = "#0e0824",
        panelLine = "#ff3fd8",
        frame = "#3ef3ff",
        post = PostEffect.NEON,
    ),
    // Drawn in grays, then turned into four greens by the GAMEBOY effect. The white tints leave the grays as they are
    // in the game, and styleColors gives the game its greens.
    "gameboy" to Palette(
        label = "Game Boy (other style)",
        outline = "#101010",
        clear = "#306230",
        text = "#ffffff",
        ground = "#f0f0f0",
        grout = "#b4b4b4",
        snake = "#707070",
        snakeLight = "#a8a8a8",
        snakeDark = "#404040",
        snakePattern = "#e8e8e8",
        eye = "#ffffff",
        pupil = "#000000",
        fruit = "#606060",
        fruitLight = "#c8c8c8",
        fruitDark = "#303030",
        seed = "#e8e8e8",
        leaf = "#a8a8a8",
        leafDark = "#707070",
        button = "#a0a0a0",
        buttonLight = "#e8e8e8",
        buttonDark = "#686868",
        primary = "#787878",
        primaryLight = "#c8c8c8",
        primaryDark = "#404040",
        panel = "#f0f0f0",
        panelDark = "#b8b8b8",
        panelLine = "#888888",
        frame = "#101010",
        snakeTint = "#ffffff",
        fruitTint = "#ffffff",
        markColor = "#ffffff",
        post = PostEffect.GAMEBOY,
        styleColors = StyleColorOverrides(clear = "#306230", outline = "#0f380f", text = "#9bbc0f", primary = "#8bac0f", surround = "#7c9b7c"),
    ),
    // Lines of one color per group on a black floor. The white outlines go to the tinted layer (tintOutline) and the
    // snake takes the fruit colors; tubes for the marks and buttons; lights under the snake and the fruit; the
    // LASER effect makes the lines white hot and blooms them.
    "laser" to Palette(
        label = "Laser (other style)",
        outline = "#22f2ff",
        lineSnake = "#22f2ff",
        lineFruit = "#ff3f9a",
        lineUi = "#ff3fe0",
        shade = "#05030b",
        clear = "#05030b",
        text = "#ffffff",
        ground = "#07040f",
        grout = "#05030b",
        snake = "#062a2e",
        snakeLight = "#5ff8ff",
        snakeDark = "#041a1d",
        snakePattern = "#e8ffff",
        eye = "#ffffff",
        pupil = "#02060a",
        fruit = "#4a0622",
        fruitLight = "#ff7ab0",
        fruitDark = "#2a0314",
        seed = "#ffffff",
        leaf = "#7dff3e",
        leafDark = "#2f8a10",
        button = "#12082a",
        buttonLight = "#ff3fe0",
        buttonDark = "#0a0418",
        primary = "#ff2bb0",
        primaryLight = "#ffa6e6",
        primaryDark = "#6a0a4a",
        panel = "#0c0620",
        panelDark = "#07030f",
        panelLine = "#ff3fe0",
        frame = "#22f2ff",
        snakeTint = "#22f2ff",
        fruitTint = "#ff3f9a",
        tintOutline = true,
        snakeTakesFruitColor = true,
        tubeMarks = true,
        markColor = "#ffffff",
        markOutline = "#ffffff",
        markShape = MarkShape.DIAMOND,
        fruitColors = listOf("#ff3f9a", "#22f2ff", "#b6ff3e", "#ffd23f", "#a46bff", "#ff7a1f"),
        snakeGlow = 0.44,
        fruitGlow = 0.8,
        tubeButtons = true,
        post = PostEffect.LASER,
    ),
    // The laser style with an orange snake, cyan fruits and round marks.
    "laser-sunset" to Palette(
        label = "Laser sunset (other style)",
        outline = "#ff9a1f",
        lineSnake = "#ff9a1f",
        lineFruit = "#3ff0ff",
        lineUi = "#ff5a3c",
        shade = "#07030a",
        clear = "#07030a",
        text = "#fff6e8",
        ground = "#0b0610",
        grout = "#07030a",
        snake = "#2e1204",
        snakeLight = "#ffc25a",
        snakeDark = "#1d0b02",
        snakePattern = "#fff2c8",
        eye = "#ffffff",
        pupil = "#0a0402",
        fruit = "#06303e",
        fruitLight = "#8ff4ff",
        fruitDark = "#031a22",
        seed = "#ffffff",
        leaf = "#ff5a9a",
        leafDark = "#8a1f4a",
        button = "#1a0a0a",
        buttonLight = "#ff5a3c",
        buttonDark = "#0e0505",
        primary = "#ff7a1f",
        primaryLight = "#ffd0a0",
        primaryDark = "#7a2a05",
        panel = "#120708",
        panelDark = "#0a0404",
        panelLine = "#ff5a3c",
        frame = "#ff9a1f",
        snakeTint = "#ff9a1f",
        fruitTint = "#3ff0ff",
        tintOutline = true,
        snakeTakesFruitColor = true,
        tubeMarks = true,
        markColor = "#fff2c8",
        markOutline = "#ffffff",
        markShape = MarkShape.CIRCLE,
        fruitColors = listOf("#3ff0ff", "#ff9a1f", "#ff3f6a", "#ffe23f", "#7a8aff"),
        snakeGlow = 0.44,
        fruitGlow = 0.8,
        tubeButtons = true,
        post = PostEffect.LASER,
    ),
)
