package fr.mwet.tools.stylegenerator

import org.jetbrains.skia.Color
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round

// Color helpers. Every color of the generator is a hex string ("#rrggbb" or "#rrggbbaa") until it reaches Skia, where
// col() turns it into an ARGB Int. The arithmetic copies the Python generator so the output stays pixel-identical.

// A color that erases what is under it: fill() and stroke() draw it with the CLEAR blend mode.
// layer() sets the colors that do not belong to a layer to CLEAR, so drawing them punches a hole in that layer.
const val CLEAR = "clear"

data class Rgb(val r: Int, val g: Int, val b: Int)

// Reads the first six hex digits only: an alpha pair after them is ignored here, col() reads it.
fun rgb(hex: String): Rgb {
    val h = hex.removePrefix("#")
    return Rgb(h.substring(0, 2).toInt(16), h.substring(2, 4).toInt(16), h.substring(4, 6).toInt(16))
}

fun hex(r: Int, g: Int, b: Int) = "#%02x%02x%02x".format(r, g, b)

// The Skia color of a hex string. alpha multiplies the alpha of an "#rrggbbaa" color: col("#ffffff80", 0.5) is 25 %.
// The rounding follows Python's round(): half to even.
fun col(hex: String, alpha: Double = 1.0): Int {
    if (hex == CLEAR) return Color.TRANSPARENT
    val (r, g, b) = rgb(hex)
    val h = hex.removePrefix("#")
    val a = if (h.length == 8) alpha * (h.substring(6, 8).toInt(16) / 255.0) else alpha
    return Color.makeARGB(round(a * 255).toInt(), r, g, b)
}

// Perceived brightness from 0 (black) to 1 (white), with the Rec. 601 weights: green counts most, blue least.
fun luminance(hex: String): Double {
    val (r, g, b) = rgb(hex)
    return (0.299 * r + 0.587 * g + 0.114 * b) / 255
}

// The gray that gives the color back once the game multiplies it by the tint.
// gray / 255 = luminance(hex) / luminance(tint), so gray x tint has the luminance of hex and the hue of the tint: the
// shading between snake and snakeDark survives whatever tint the game applies. The tint color itself becomes white.
// A color brighter than the tint clips to white (min 1.0); 1e-3 avoids a division by zero for a black tint.
// toInt() truncates, as Python's int() did.
fun gray(hex: String, tint: String): String {
    val value = (min(1.0, luminance(hex) / max(luminance(tint), 1e-3)) * 255).toInt()
    return hex(value, value, value)
}

// Moves the color toward white (amount > 0) or black (amount < 0).
// amount is the fraction of the way: 0.25 goes a quarter of the way to white, -1.0 gives black.
fun tintHex(hex: String, amount: Double): String {
    val (r, g, b) = rgb(hex)
    val target = if (amount > 0) 255 else 0
    val t = abs(amount)
    fun channel(v: Int) = round(v + (target - v) * t).toInt()
    return hex(channel(r), channel(g), channel(b))
}

// The color of text borders and shadows, of the dims behind the panels, and of the game's StyleColors.outline.
fun Palette.shadeColor() = shade ?: outline

// The game multiplies the floor tiles outside the board by this color: the clear color moved 35 % of the way to white,
// so the floor around the board looks like the board seen through a tinted glass. Truncated, as Python's int() did.
fun Palette.surround(): String {
    val (r, g, b) = rgb(clear)
    fun channel(v: Int) = (v + (255 - v) * 0.35).toInt()
    return hex(channel(r), channel(g), channel(b))
}

// The groups of assets that can have their own outline color (lineSnake, lineFruit, lineUi).
enum class LineGroup { SNAKE, FRUIT, UI }

fun Palette.lineColor(group: LineGroup) = when (group) {
    LineGroup.SNAKE -> lineSnake
    LineGroup.FRUIT -> lineFruit
    LineGroup.UI -> lineUi
} ?: outline

// The palette that a group draws with: the same palette, with the outline of that group. Main.kt passes it to the
// drawing functions of the group, so these functions only ever read p.outline.
fun Palette.withLine(group: LineGroup) = copy(outline = lineColor(group))

enum class Part { SNAKE, FRUIT }

enum class Layer { BASE, DETAIL }

// The snake and the fruit are drawn twice: a gray base layer that the game tints, and a detail layer that it draws
// untinted on top. Each layer keeps only its own colors and erases the others.
// The same drawing code runs for both layers; only the palette changes:
// - BASE: the body colors become grays (see gray()), everything else is CLEAR. The game multiplies this layer by the
//   snake or fruit color, so the snake can change color without new images.
// - DETAIL: the body colors are CLEAR, so the body is erased and only the outline, the eyes, the leaves, the seeds and
//   the highlights stay. The game draws this layer as it is, over the tinted base.
fun Palette.layer(part: Part, kind: Layer): Palette {
    val base = kind == Layer.BASE
    // Resolved first: the shadow keeps its color even when the outline it defaults to is cleared below.
    var q = copy(fruitShadow = fruitShadow ?: shadeColor())
    q = when (part) {
        Part.SNAKE -> {
            val tint = snakeTint ?: snake
            q.copy(
                snake = if (base) gray(snake, tint) else CLEAR,
                snakeDark = if (base) gray(snakeDark, tint) else CLEAR,
            ).let {
                if (base) it.copy(eye = CLEAR, pupil = CLEAR, tongue = CLEAR, snakeLight = CLEAR)
                // The light stripes become a half-transparent white (alpha 0x80) that lightens any tint: the
                // snakeLight of the palette shows only on the app icon, which is drawn without layers.
                else it.copy(snakeLight = "#ffffff80")
            }
        }
        Part.FRUIT -> {
            val tint = fruitTint ?: fruit
            q.copy(
                fruit = if (base) gray(fruit, tint) else CLEAR,
                fruitDark = if (base) gray(fruitDark, tint) else CLEAR,
            ).let {
                if (base) {
                    it.copy(
                        seed = CLEAR, leaf = CLEAR, leafDark = CLEAR, fruitShadow = CLEAR, fruitLight = CLEAR,
                        stem = CLEAR, rind = CLEAR, rindDark = CLEAR, pip = CLEAR,
                    )
                } else {
                    // The highlight spot becomes a 75 % white (alpha 0xc0) for every fruit: the fruitLight of the
                    // palette never reaches a generated file.
                    it.copy(fruitLight = "#ffffffc0")
                }
            }
        }
    }
    return when {
        // The outline goes to the base layer in white, so the game tints it with the snake or fruit color (laser).
        tintOutline -> q.copy(outline = if (base) "#ffffff" else CLEAR)
        // Otherwise the outline is a fixed color and belongs to the detail layer.
        base -> q.copy(outline = CLEAR)
        else -> q
    }
}
