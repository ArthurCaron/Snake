package fr.mwet.tools.stylegenerator

import org.jetbrains.skia.BlendMode
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.ClipMode
import org.jetbrains.skia.Color
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ColorType
import org.jetbrains.skia.FilterMipmap
import org.jetbrains.skia.FilterMode
import org.jetbrains.skia.Font
import org.jetbrains.skia.FontMgr
import org.jetbrains.skia.Image
import org.jetbrains.skia.ImageInfo
import org.jetbrains.skia.Matrix33
import org.jetbrains.skia.MipmapMode
import org.jetbrains.skia.Paint
import org.jetbrains.skia.PaintMode
import org.jetbrains.skia.PaintStrokeCap
import org.jetbrains.skia.PaintStrokeJoin
import org.jetbrains.skia.Path
import org.jetbrains.skia.PathBuilder
import org.jetbrains.skia.PathOp
import org.jetbrains.skia.RRect
import org.jetbrains.skia.Rect
import org.jetbrains.skia.Surface
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

// Drawing primitives on top of Skia (through Skiko).
// Coordinates are pixels with y pointing down: (0, 0) is the top left corner of the image, (width, height) the bottom
// right. A path is a list of lines and Bezier curves that Skia fills or strokes with a Paint.
// Geometry is computed in Double and converted to Float only when it reaches Skia, as in the Python generator.

// The game font, read relative to the working directory: the Gradle run task starts in the repository root.
const val FONT_FILE = "sourceAssets/fonts/lilitaone/LilitaOne-Regular.ttf"
val TYPEFACE by lazy { FontMgr.default.makeFromFile(FONT_FILE) ?: error("Cannot load $FONT_FILE") }

// Sampling when an image is drawn scaled: LINEAR blends the four nearest pixels, LINEAR_MIPMAP also blends between
// pre-shrunk copies, which is smoother when the image is drawn much smaller (the previews).
val LINEAR = FilterMipmap(FilterMode.LINEAR, MipmapMode.NONE)
val LINEAR_MIPMAP = FilterMipmap(FilterMode.LINEAR, MipmapMode.LINEAR)

// An image to draw on, transparent at the start. Skia keeps it premultiplied (each color channel already multiplied by
// the alpha); pixels() reads it back with straight alpha, as the PNG files store it.
class Raster(val width: Int, val height: Int) {
    private val surface = Surface.makeRasterN32Premul(width, height)
    val canvas: Canvas = surface.canvas.also { it.clear(Color.TRANSPARENT) }

    fun image(): Image = surface.makeImageSnapshot()

    fun pixels(): Pixels {
        val bitmap = Bitmap()
        bitmap.allocPixels(ImageInfo(width, height, ColorType.RGBA_8888, ColorAlphaType.UNPREMUL))
        surface.readPixels(bitmap, 0, 0)
        return Pixels(width, height, bitmap.readPixels() ?: error("Cannot read the pixels"))
    }
}

// A fully transparent color (CLEAR, or any "#rrggbb00") erases instead of drawing nothing: see layer().
private fun Paint.erasing() = apply { if (Color.getA(color) == 0) blendMode = BlendMode.CLEAR }

// The two paints of the generator, always antialiased (smooth edges).
fun fill(color: Int) = Paint().apply {
    isAntiAlias = true
    this.color = color
}.erasing()

// A line of the given width, centred on the path: half of it is drawn inside the shape, half outside.
// Round joins and caps, so corners and line ends look soft, as in the cartoon style.
fun stroke(color: Int, width: Double) = Paint().apply {
    isAntiAlias = true
    this.color = color
    mode = PaintMode.STROKE
    strokeWidth = width.toFloat()
    strokeJoin = PaintStrokeJoin.ROUND
    strokeCap = PaintStrokeCap.ROUND
}.erasing()

// Builds a path: path { moveTo(...); lineTo(...); cubicTo(...); closePath() }. The overloads below take Double.
fun path(build: PathBuilder.() -> Unit): Path = PathBuilder().apply(build).detach()

fun PathBuilder.moveTo(x: Double, y: Double): PathBuilder = moveTo(x.toFloat(), y.toFloat())
fun PathBuilder.lineTo(x: Double, y: Double): PathBuilder = lineTo(x.toFloat(), y.toFloat())
fun PathBuilder.quadTo(x1: Double, y1: Double, x2: Double, y2: Double): PathBuilder =
    quadTo(x1.toFloat(), y1.toFloat(), x2.toFloat(), y2.toFloat())

fun PathBuilder.cubicTo(x1: Double, y1: Double, x2: Double, y2: Double, x3: Double, y3: Double): PathBuilder =
    cubicTo(x1.toFloat(), y1.toFloat(), x2.toFloat(), y2.toFloat(), x3.toFloat(), y3.toFloat())

fun Path.translated(dx: Double, dy: Double): Path = PathBuilder(this).offset(dx.toFloat(), dy.toFloat()).detach()

fun Path.transformed(matrix: Matrix33): Path = PathBuilder(this).transform(matrix).detach()

fun rectF(left: Double, top: Double, right: Double, bottom: Double) =
    Rect.makeLTRB(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat())

// One shape out of several overlapping ones, so the outline goes around the whole and not inside (head, grapes).
fun union(vararg paths: Path): Path =
    paths.reduce { result, path -> Path.makeCombining(result, path, PathOp.UNION) ?: error("Path union failed") }

// Basic shapes. oval and rect take the bounding box; radius rounds the corners of rect.
fun oval(left: Double, top: Double, right: Double, bottom: Double): Path = path { addOval(rectF(left, top, right, bottom)) }

fun circle(x: Double, y: Double, r: Double) = oval(x - r, y - r, x + r, y + r)

fun rect(left: Double, top: Double, right: Double, bottom: Double, radius: Double = 0.0): Path =
    path { addRRect(RRect.makeLTRB(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), radius.toFloat())) }

fun diamond(cx: Double, cy: Double, rx: Double, ry: Double): Path = path {
    moveTo(cx - rx, cy)
    lineTo(cx, cy - ry)
    lineTo(cx + rx, cy)
    lineTo(cx, cy + ry)
    closePath()
}

// SkMatrix::setRotate, in single precision: Skiko's Matrix33.makeRotate computes it in double.
// skia-python called setRotate, so this copy keeps the Python generator's matrices to the last bit: a matrix
// computed in double differs in the last float bits, which can change antialiased edge pixels.
fun rotation(degrees: Double, pivotX: Double = 0.0, pivotY: Double = 0.0): Matrix33 {
    val radians = degrees.toFloat() * (3.14159265f / 180)
    // SK_ScalarNearlyZero (1 / 4096): sin(180) or cos(90) become an exact 0 instead of a tiny float.
    fun snap(v: Float) = if (abs(v) <= 1f / (1 shl 12)) 0f else v
    val s = snap(sin(radians.toDouble()).toFloat())
    val c = snap(cos(radians.toDouble()).toFloat())
    val px = pivotX.toFloat()
    val py = pivotY.toFloat()
    val oneMinusCos = 1 - c
    // Rotation about the pivot: the last column moves the pivot back where it was.
    return Matrix33(c, -s, s * py + oneMinusCos * px, s, c, -s * px + oneMinusCos * py, 0f, 0f, 1f)
}

fun Canvas.translate(dx: Double, dy: Double): Canvas = translate(dx.toFloat(), dy.toFloat())
fun Canvas.scale(sx: Double, sy: Double): Canvas = scale(sx.toFloat(), sy.toFloat())
fun Canvas.rotate(degrees: Double): Canvas = rotate(degrees.toFloat())

// Draws only inside the path (antialiased clip), then removes the clip. The shading of a shape is drawn this way: a
// darker fill, a lighter fill shifted up, a highlight, all cut to the shape.
fun Canvas.clipped(path: Path, draw: Canvas.() -> Unit) {
    save()
    clipPath(path, ClipMode.INTERSECT, true)
    draw()
    restore()
}

fun Canvas.drawImageRect(image: Image, dst: Rect, sampling: FilterMipmap, paint: Paint? = null) {
    drawImageRect(image, Rect.makeWH(image.width.toFloat(), image.height.toFloat()), dst, sampling, paint, false)
}

// Outline first, then the fill on top: the outline shows only outside the shape.
// The stroke is width * 2 wide and centred on the edge, so the fill hides its inner half and exactly width pixels of
// outline stay visible outside. A shape is therefore width pixels bigger than its path on every side.
fun Canvas.outlined(path: Path, base: String, p: Palette, width: Double = OUTLINE) {
    drawPath(path, stroke(col(p.outline), width * 2))
    drawPath(path, fill(col(base)))
}

// The glyph outlines placed one after the other on the baseline; returns the path and the advance.
// The baseline is y = 0, so the letters stand on it at negative y; the advance is the width of the text.
fun textPath(text: String, size: Double): Pair<Path, Double> {
    val font = Font(TYPEFACE, size.toFloat())
    val glyphs = font.getStringGlyphs(text)
    val widths = font.getWidths(glyphs)
    val path = PathBuilder()
    var x = 0.0
    for (i in glyphs.indices) {
        font.getPath(glyphs[i])?.let { path.addPath(it.translated(x, 0.0)) }
        x += widths[i]
    }
    return path.detach() to x
}

enum class Align { LEFT, CENTER, RIGHT }

// Outlined text: a border in the shade color, and a drop shadow of the same color below. (x, y) is the point of the
// baseline given by align. Returns the advance.
fun Canvas.outlinedText(
    text: String, x: Double, y: Double, size: Double, p: Palette,
    color: String? = null, align: Align = Align.LEFT,
): Double {
    val (glyphs, advance) = textPath(text, size)
    val left = when (align) {
        Align.LEFT -> x
        Align.CENTER -> x - advance / 2
        Align.RIGHT -> x - advance
    }
    val path = glyphs.translated(left, y)
    // The border is a twelfth of the text size, at least 2 px; the shadow drops by almost one border width.
    val border = maxOf(2.0, size / 12)
    val shadow = path.translated(0.0, border * 0.9)
    val shade = col(p.shadeColor())
    drawPath(shadow, stroke(shade, border * 2))
    drawPath(shadow, fill(shade))
    drawPath(path, stroke(shade, border * 2))
    drawPath(path, fill(col(color ?: p.text)))
    return advance
}

// Repeats the image from (x, y), scaled, over a w by h area. The splash and the previews lay the floor this way.
fun Canvas.tileFill(image: Image, x: Double, y: Double, w: Double, h: Double, scale: Double = 1.0) {
    save()
    clipRect(rectF(x, y, x + w, y + h))
    val size = image.width * scale
    var ty = y
    while (ty < y + h) {
        var tx = x
        while (tx < x + w) {
            drawImageRect(image, rectF(tx, ty, tx + size, ty + size), LINEAR)
            tx += size
        }
        ty += size
    }
    restore()
}
