package fr.mwet.tools.stylegenerator

import java.io.File
import kotlin.math.abs
import kotlin.math.ceil

// The other styles rework the PNG files once they are written. The arithmetic is in single precision, as numpy did.
// Palette.post picks the effect, and Main.kt runs it after every file of the style is written and before the preview,
// so the preview shows the reworked files. The effects read and write the files of out/<style>, in place:
// - GAMEBOY: big pixels in four greens, and a pack.json with nearest filtering.
// - NEON: a soft glow under every sprite.
// - LASER: the lines turn white hot inside and bloom, the halo split between the base and the detail layers.

// The four greens of the original Game Boy screen, darkest first.
private val GAMEBOY = listOf("#0f380f", "#306230", "#8bac0f", "#9bbc0f").map(::rgb)
// One Game Boy pixel is 4 by 4 image pixels: a 64 px cell becomes 16 by 16 big pixels.
private const val PIXEL = 4

// Big pixels in the four Game Boy greens, chosen by luminance.
// The gameboy palette draws in grays: only the brightness of a color matters, the post effect gives the green.
// hardAlpha makes each big pixel fully opaque or fully transparent, so the sprites have no soft edges.
fun gameboyPixels(img: Pixels, factor: Int = PIXEL, hardAlpha: Boolean = true): Pixels {
    // BOX averages each factor by factor block into one pixel.
    val small = img.resized(img.width / factor, img.height / factor, Resample.BOX)
    for (y in 0 until small.height) {
        for (x in 0 until small.width) {
            val luminance = small[x, y, 0] * 0.299f + small[x, y, 1] * 0.587f + small[x, y, 2] * 0.114f
            // The luminance thresholds, from 0 to 255, between the four greens: below 60 is the darkest.
            val level = listOf(60, 120, 185).count { luminance >= it }
            val (r, g, b) = GAMEBOY[level]
            small[x, y, 0] = r
            small[x, y, 1] = g
            small[x, y, 2] = b
            // A big pixel that is less than 110 / 255 covered disappears.
            if (hardAlpha) small[x, y, 3] = if (small[x, y, 3] > 110) 255 else 0
        }
    }
    return small.enlarged(factor)
}

private fun File.isNinePatch() = name.endsWith(".9.png")

// Applies effect to the image inside the nine-patch border, and keeps the border marks.
// The 1 px border of a nine-patch holds the black marks that tell libGDX which part stretches: an effect on the border
// would break them.
private fun File.rework(effect: (Pixels) -> Pixels) {
    val img = Pixels.read(this)
    if (!isNinePatch()) {
        effect(img).write(this)
        return
    }
    val inner = effect(img.crop(1, 1, img.width - 1, img.height - 1))
    img.paste(inner, 1, 1)
    img.write(this)
}

private fun File.pngs() = listFiles { f -> f.name.endsWith(".png") }.orEmpty().sortedBy { it.name }

// The skin sprites and the splash and loading images. The icons keep the gray drawing.
// The atlas filters become Nearest, so the game scales the big pixels without blurring them.
fun postGameboy(out: File) {
    val skin = out.resolve("textures/game/skin")
    for (file in skin.pngs() + out.resolve("root").pngs()) file.rework { gameboyPixels(it) }
    val pack = skin.resolve("pack.json")
    pack.writeText(
        pack.readText().replace("filterMin: MipMapLinearLinear", "filterMin: Nearest").replace("filterMag: Linear", "filterMag: Nearest"),
    )
}

// The image as four Float planes (red, green, blue, alpha, 0 to 255): the numpy arrays of the Python generator.
private class Channels(val width: Int, val height: Int, val values: Array<FloatArray>) {
    constructor(img: Pixels) : this(img.width, img.height, Array(4) { c -> FloatArray(img.width * img.height) { i -> (img.rgba[i * 4 + c].toInt() and 0xff).toFloat() } })

    fun toPixels() = Pixels(width, height).also { out ->
        for (c in 0 until 4) for (i in 0 until width * height) out.rgba[i * 4 + c] = values[c][i].coerceIn(0f, 255f).toInt().toByte()
    }
}

// The Python generator blurred each plane with Pillow, through an 8-bit image: the plane is clamped and truncated to
// bytes first. radius is the standard deviation of the blur, in pixels.
private fun blur(channel: FloatArray, width: Int, height: Int, radius: Int): FloatArray {
    val bytes = IntArray(channel.size) { channel[it].coerceIn(0f, 255f).toInt() }
    return gaussianBlur(bytes, width, height, radius.toFloat()).let { r -> FloatArray(r.size) { r[it].toFloat() } }
}

// The blurred premultiplied halo goes back to straight alpha, and the image is drawn over it.
// The halo shows only around the shapes, where the image is transparent: the opaque pixels of the image do not change.
private fun overHalo(img: Pixels, halo: Channels): Pixels {
    val alpha = halo.values[3]
    for (c in 0 until 3) {
        val v = halo.values[c]
        for (i in v.indices) {
            val a = alpha[i] / 255
            v[i] = if (a > 0) v[i] / maxOf(a, 1e-3f) else 0f
        }
    }
    return alphaComposite(halo.toPixels(), img)
}

// A premultiplied copy, with the pixels that keep rejects set to transparent: only the kept pixels will glow.
// Premultiplied, the blur does not spread the color of the transparent pixels around the shapes.
private fun premultipliedChannels(arr: Channels, keep: (Int) -> Boolean = { true }): Channels {
    val source = Channels(arr.width, arr.height, Array(4) { arr.values[it].copyOf() })
    for (i in 0 until arr.width * arr.height) {
        if (!keep(i)) {
            for (c in 0 until 4) source.values[c][i] = 0f
            continue
        }
        val a = source.values[3][i] / 255
        for (c in 0 until 3) source.values[c][i] *= a
    }
    return source
}

// Neon: a blurred copy of the image, made stronger, glows under it.
// radius is the blur size in pixels: a bigger one gives a wider, fainter glow. strength multiplies the halo: above 1,
// the glow is denser than a plain blur.
fun glow(img: Pixels, radius: Int, strength: Double): Pixels {
    val source = premultipliedChannels(Channels(img))
    val blurred = Channels(img.width, img.height, Array(4) { c ->
        blur(source.values[c], img.width, img.height, radius).also { v ->
            for (i in v.indices) v[i] = (v[i] * strength.toFloat()).coerceIn(0f, 255f)
        }
    })
    return overHalo(img, blurred)
}

// Every skin sprite glows, except the board frame and the grid cell, which cover the whole board.
// The nine-patches glow a little less (1.4) than the other sprites (1.5).
fun postNeon(out: File) {
    for (file in out.resolve("textures/game/skin").pngs()) {
        when {
            file.isNinePatch() -> file.rework { glow(it, 3, 1.4) }
            file.nameWithoutExtension !in setOf("boardFrame", "gridCell") -> file.rework { glow(it, 3, 1.5) }
        }
    }
}

// Laser: the bright pixels bloom at several radii; the inside of the lines turns white hot.
// A bloom is a sum of blurs: a small radius gives the bright rim close to the line, a big one the wide, faint halo.
// strength is the weight of each blur in the sum.
private class Bloom(val radius: Int, val strength: Double)

// The file names, without .png, that each bloom applies to. A new sprite that must glow goes in one of these lists.
private val SNAKE_STEMS = listOf("snakeHead", "snakeHeadDead", "snakeBodyStraight", "snakeBodyTurn", "snakeTail")
private val FRUIT_STEMS = FruitKind.entries.flatMap { kind -> listOf("Still", "_0", "_1").map { "fruit${kind.title}$it" } }
private val UI_STEMS = listOf(
    "button.9", "buttonDown.9", "buttonOver.9", "buttonPrimary.9", "buttonPrimaryDown.9", "buttonPrimaryOver.9", "panel.9",
    "iconPause", "iconPlay", "iconSoundOn", "iconSoundOff", "iconMusicOn", "iconMusicOff",
)
// The logo has the widest halo (radius 7), the UI the tightest (a single blur of radius 2).
private val SNAKE_BLOOM = listOf(Bloom(2, 1.6), Bloom(5, 1.1))
private val FRUIT_BLOOM = listOf(Bloom(2, 1.6), Bloom(6, 1.2))
private val MARKS_BLOOM = listOf(Bloom(2, 1.4), Bloom(4, 0.8))
private val UI_BLOOM = listOf(Bloom(2, 1.8))
private val LOGO_BLOOM = listOf(Bloom(2, 1.0), Bloom(7, 0.6))

// Draws the image over the bloom of its bright pixels.
private fun bloomUnder(arr: Channels, bright: BooleanArray, bloom: List<Bloom>): Pixels {
    val source = premultipliedChannels(arr) { bright[it] }
    val halo = Channels(arr.width, arr.height, Array(4) { FloatArray(arr.width * arr.height) })
    for (b in bloom) {
        for (c in 0 until 4) {
            val blurred = blur(source.values[c], arr.width, arr.height, b.radius)
            val h = halo.values[c]
            for (i in h.indices) h[i] += blurred[i] * b.strength.toFloat()
        }
    }
    for (c in 0 until 4) halo.values[c].let { v -> for (i in v.indices) v[i] = v[i].coerceIn(0f, 255f) }
    return overHalo(arr.toPixels(), halo)
}

// The luminance of a pixel, from 0 to 255, with the weights of luminance() in Colors.kt.
private fun Channels.brightness(i: Int) = values[0][i] * 0.299f + values[1][i] * 0.587f + values[2][i] * 0.114f

// More than half covered: the antialiased edges do not count as part of a line.
private fun Channels.opaque(i: Int) = values[3][i] > 128

// The pixels of the mask at a distance of at least depth from every pixel outside it (Euclidean distance transform).
// The middle of a line: with depth 1.4, a line 4 px wide keeps its 2 middle pixels. Pixels past the image edges do not
// count as outside the mask.
private fun core(mask: BooleanArray, width: Int, height: Int, depth: Double): BooleanArray {
    val reach = ceil(depth).toInt()
    val limit = depth * depth
    return BooleanArray(mask.size) { i ->
        if (!mask[i]) return@BooleanArray false
        val x = i % width
        val y = i / width
        for (dy in -reach..reach) {
            for (dx in -reach..reach) {
                val nx = x + dx
                val ny = y + dy
                if (nx !in 0 until width || ny !in 0 until height) continue
                if (!mask[ny * width + nx] && dx * dx + dy * dy < limit) return@BooleanArray false
            }
        }
        true
    }
}

// The lines of line color become white hot inside, then the lines and the bright pixels bloom.
// For the UI and the logo, which are not tinted by the game: the effect is drawn into the image itself.
private fun laser(img: Pixels, lineHex: String, bloom: List<Bloom>, coreDepth: Double): Pixels {
    val (lr, lg, lb) = rgb(lineHex)
    val arr = Channels(img)
    val size = img.width * img.height
    val onLine = BooleanArray(size) { i ->
        val distance = abs(arr.values[0][i] - lr) + abs(arr.values[1][i] - lg) + abs(arr.values[2][i] - lb)
        // A pixel is on a line when its color is within 70 (sum of the three channel gaps) of the line color.
        distance < 70 && arr.opaque(i)
    }
    val hot = core(onLine, img.width, img.height, coreDepth)
    for (i in 0 until size) {
        // The core moves 75 % of the way to white.
        if (hot[i]) for (c in 0 until 3) arr.values[c][i] = arr.values[c][i] * 0.25f + 255 * 0.75f
    }
    val bright = BooleanArray(size) { i -> (onLine[i] || arr.brightness(i) > 170) && arr.opaque(i) }
    return bloomUnder(arr, bright, bloom)
}

// The white base layer blooms; its white-hot core goes to the detail layer, which the game does not tint.
// With tintOutline, the outline is white in the base layer: the game tints the line and its halo with the snake or
// fruit color, and the core, in the untinted detail layer, stays white.
private fun laserLayers(basePath: File, detailPath: File, bloom: List<Bloom>, coreDepth: Double) {
    val base = Channels(Pixels.read(basePath))
    val size = base.width * base.height
    // The white outline: almost white (above 235) and opaque.
    val line = BooleanArray(size) { i -> base.brightness(i) > 235 && base.opaque(i) }
    val hot = core(line, base.width, base.height, coreDepth)
    val glowCore = Pixels(base.width, base.height)
    for (i in 0 until size) {
        if (!hot[i]) continue
        glowCore.rgba[i * 4] = 255.toByte()
        glowCore.rgba[i * 4 + 1] = 255.toByte()
        glowCore.rgba[i * 4 + 2] = 255.toByte()
        // A 59 % white (150 / 255), under the other details: in the game, the tinted line still shows through it.
        glowCore.rgba[i * 4 + 3] = 150.toByte()
    }
    alphaComposite(glowCore, Pixels.read(detailPath)).write(detailPath)
    bloomUnder(base, line, bloom).write(basePath)
}

// The detail file of a base file: snakeHead to snakeHeadDetail, fruitApple_0 to fruitAppleDetail_0. The frame index
// stays last, as TexturePacker reads it there.
private fun detailName(stem: String): String {
    if ("_" !in stem) return "${stem}Detail"
    val (name, index) = stem.split("_")
    return "${name}Detail_$index"
}

// The snake and fruit layers, the marks, then the UI and the logo. A core depth of 1.4 suits the 4 to 5 px lines;
// the logo lines are 8 px wide, so its core is deeper (2.4).
fun postLaser(out: File, p: Palette) {
    val skin = out.resolve("textures/game/skin")
    for ((stems, bloom) in listOf(SNAKE_STEMS to SNAKE_BLOOM, FRUIT_STEMS to FRUIT_BLOOM)) {
        for (stem in stems) {
            val depth = 1.4
            laserLayers(skin.resolve("$stem.png"), skin.resolve("${detailName(stem)}.png"), bloom, depth)
        }
    }
    // The marks are white: every opaque pixel blooms, and the game tints mark and halo with the mark color.
    val marks = skin.pngs().filter { it.name.startsWith("mark") } + skin.resolve("snakeHeadMark.png")
    for (file in marks) {
        val arr = Channels(Pixels.read(file))
        bloomUnder(arr, BooleanArray(arr.width * arr.height) { arr.opaque(it) }, MARKS_BLOOM).write(file)
    }
    for ((stems, group) in listOf(UI_STEMS to LineGroup.UI, listOf("logo") to LineGroup.LOGO)) {
        val color = p.lineColor(group)
        val bloom = if (group == LineGroup.UI) UI_BLOOM else LOGO_BLOOM
        val depth = if (group == LineGroup.UI) 1.4 else 2.4
        for (stem in stems) skin.resolve("$stem.png").rework { laser(it, color, bloom, depth) }
    }
}
