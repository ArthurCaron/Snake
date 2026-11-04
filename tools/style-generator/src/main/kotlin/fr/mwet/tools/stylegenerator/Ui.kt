package fr.mwet.tools.stylegenerator

import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Path
import org.jetbrains.skia.PathBuilder

// libGDX nine-patch: a 1 px transparent border whose black marks on the top and left edges give the stretchable part.
// left, right, top and bottom are the fixed margins of the image, in px: the corners keep their size, only the middle
// stretches. The image must keep its rounded corners and bevels inside these margins.
fun ninePatch(img: Pixels, left: Int, right: Int, top: Int, bottom: Int): Pixels {
    val out = Pixels(img.width + 2, img.height + 2)
    out.paste(img, 1, 1)
    for (x in 1 + left until 1 + img.width - right) out[x, 0, 3] = 255
    for (y in 1 + top until 1 + img.height - bottom) out[0, y, 3] = 255
    return out
}

const val HOVER_TINT = 0.18

// Pressed, the face moves down and the bevel shrinks.
// A 72 px rounded button: the dark outline, the face in base and the darker band at the bottom in dark (the bevel, the
// thickness of the button). Pressed, the top edge is 4 px lower and the bevel thinner, so the face looks pushed in.
// light only lights up the pressed tube buttons. line is the outline color of the tube buttons.
fun buttonImage(
    p: Palette, base: String, light: String, dark: String,
    pressed: Boolean = false, size: Int = 72, line: String? = null,
): Pixels {
    val raster = Raster(size, size)
    val c = raster.canvas
    // The margin of the shape: the outline is o px thick and ends on the canvas edge.
    val o = 5.0
    if (p.tubeButtons) {
        // Neon look: a flat dark plate with a 4.5 px line, no bevel. Pressed, a 30% light veil lights it up.
        val shape = rect(o, o, size - o, size - o, 18.0)
        c.drawPath(shape, fill(col(dark)))
        if (pressed) c.drawPath(shape, fill(col(light, 0.3)))
        c.drawPath(shape, stroke(col(line ?: p.outline), 4.5))
        return ninePatch(raster.pixels(), 24, 24, 24, 24)
    }
    val top = o + if (pressed) 4 else 0
    // The height of the bevel band at the bottom: 7 px up, 3 px pressed. A larger value gives a thicker button.
    val bevel = if (pressed) 3 else 7
    // Corner radius 18: it must stay under the 24 px nine-patch margins.
    val shape = rect(o, top, size - o, size - o, 18.0)
    // Outline first: a 10 px stroke centred on the edge, the fills below cover its inner half.
    c.drawPath(shape, stroke(col(p.outline), o * 2))
    c.clipped(shape) {
        // The whole shape in dark, then the face in base over all but the bottom bevel px: the dark band remains.
        drawPath(shape, fill(col(dark)))
        drawPath(rect(o, top, size - o, size - o - bevel, 16.0), fill(col(base)))
    }
    return ninePatch(raster.pixels(), 24, 24, 24, 24)
}

// A plate with an inner groove.
// A 96 px rounded plate like the button: a 6 px outline, the face in panel and an 8 px bevel band in panelDark at the
// bottom. The groove is a 2.5 px line 9 px inside the plate (15 px at the bottom, to stay above the bevel).
fun panelImage(p: Palette, size: Int = 96): Pixels {
    val raster = Raster(size, size)
    val c = raster.canvas
    val o = 6.0
    val shape = rect(o, o, size - o, size - o, 22.0)
    c.drawPath(shape, stroke(col(p.outline), o * 2))
    c.clipped(shape) {
        drawPath(shape, fill(col(p.panelDark)))
        drawPath(rect(o, o, size - o, size - o - 8, 20.0), fill(col(p.panel)))
    }
    c.drawPath(rect(o + 9, o + 9, size - o - 9, size - o - 15, 13.0), stroke(col(p.panelLine), 2.5))
    // 32 px margins: the corners do not stretch.
    return ninePatch(raster.pixels(), 32, 32, 32, 32)
}

// The icons are 64 px, y down: a shape in the text color with a dark outline around it.
private const val ICON = 64

// Outline first, then the fill: the 9 px stroke leaves a 4.5 px outline outside the shape.
private fun iconCanvas(p: Palette, shape: Path) = Raster(ICON, ICON).apply {
    canvas.drawPath(shape, stroke(col(p.outline), 9.0))
    canvas.drawPath(shape, fill(col(p.text)))
}

// Two rounded bars, 11 by 36 px, 8 px apart, centred in the icon.
fun iconPause(p: Palette) = iconCanvas(p, union(rect(17.0, 14.0, 28.0, 50.0, 3.5), rect(36.0, 14.0, 47.0, 50.0, 3.5)))

// The text-colored stroke rounds the corners of the triangle.
// A loudspeaker on the left half of the icon: a box from x = 9 to 19, y = 25 to 39, and a cone that opens to x = 31,
// y = 14 to 50.
private fun speaker() = path {
    moveTo(9.0, 25.0)
    lineTo(19.0, 25.0)
    lineTo(31.0, 14.0)
    lineTo(31.0, 50.0)
    lineTo(19.0, 39.0)
    lineTo(9.0, 39.0)
    closePath()
}

// Two sound waves, or a cross when off.
// All the outlines go first, then all the text-colored parts, so that an outline never covers a light part where the
// speaker and the marks overlap.
fun iconSound(p: Palette, on: Boolean) = Raster(ICON, ICON).apply {
    val c = canvas
    val outline = col(p.outline)
    val text = col(p.text)
    val shape = speaker()
    c.drawPath(shape, stroke(outline, 9.0))
    val marks = PathBuilder()
    if (on) {
        // Two arcs of 90 degrees centred on (33, 32), radius 9 and 18, opening to the right.
        for (radius in listOf(9.0, 18.0)) marks.addArc(rectF(33 - radius, 32 - radius, 33 + radius, 32 + radius), -45f, 90f)
    } else {
        // An X of 16 by 16 px right of the speaker.
        marks.moveTo(39.0, 24.0)
        marks.lineTo(55.0, 40.0)
        marks.moveTo(55.0, 24.0)
        marks.lineTo(39.0, 40.0)
    }
    // The marks are 4.5 px lines with a 3.75 px outline on each side (12 px stroke).
    c.drawPath(marks.snapshot(), stroke(outline, 12.0))
    // The 1.5 px stroke rounds the corners of the speaker a little.
    c.drawPath(shape, fill(text))
    c.drawPath(shape, stroke(text, 1.5))
    c.drawPath(marks.snapshot(), stroke(text, 4.5))
}

// An eighth note: a tilted head, a stem and a flag.
// The head is an 18 by 13 px oval at the bottom left, turned 20 degrees counter-clockwise around its centre. The stem
// is 5 px wide on the right of the head, from y = 12 to 46. The flag leaves the top of the stem and curls down to the
// right to (45, 37).
private fun musicNote(): Path {
    val head = oval(13.0, 39.0, 31.0, 52.0).transformed(rotation(-20.0, 22.0, 45.5))
    val stem = rect(27.0, 12.0, 32.0, 46.0, 1.5)
    val flag = path {
        moveTo(29.0, 12.0)
        cubicTo(36.0, 19.0, 49.0, 21.0, 45.0, 37.0)
        cubicTo(43.0, 29.0, 37.0, 26.0, 29.0, 26.0)
        closePath()
    }
    return union(head, stem, flag)
}

// A slash when off.
// The slash goes from the top left (13, 13) to the bottom right (51, 51), drawn like the marks of iconSound.
fun iconMusic(p: Palette, on: Boolean) = Raster(ICON, ICON).apply {
    val c = canvas
    val outline = col(p.outline)
    val text = col(p.text)
    val shape = musicNote()
    c.drawPath(shape, stroke(outline, 9.0))
    val slash = PathBuilder()
    if (!on) {
        slash.moveTo(13.0, 13.0)
        slash.lineTo(51.0, 51.0)
        c.drawPath(slash.snapshot(), stroke(outline, 12.0))
    }
    c.drawPath(shape, fill(text))
    c.drawPath(shape, stroke(text, 1.5))
    if (!on) c.drawPath(slash.snapshot(), stroke(text, 4.5))
}

private const val FLAG_WIDTH = 96
private const val FLAG_HEIGHT = 64

// A national flag on a rounded plate, outlined and shaded at the bottom like the buttons. The colors are the real ones,
// not the style's.
private fun flag(p: Palette, drawStripes: Canvas.(left: Double, top: Double, right: Double, bottom: Double) -> Unit) =
    Raster(FLAG_WIDTH, FLAG_HEIGHT).apply {
        val o = 5.0
        val right = FLAG_WIDTH - o
        val bottom = FLAG_HEIGHT - o
        val shape = rect(o, o, right, bottom, 14.0)
        canvas.drawPath(shape, stroke(col(p.outline), o * 2))
        canvas.clipped(shape) {
            drawStripes(o, o, right, bottom)
            drawPath(rect(o, bottom - 7, right, bottom), fill(col(p.outline, 0.25)))
            drawPath(rect(o + 12, o + 4, right - 12, o + 7, 1.5), fill(col("#ffffff", 0.6)))
        }
    }

// Three vertical bands: blue, white, red.
fun flagFrench(p: Palette) = flag(p) { left, top, right, bottom ->
    val third = (right - left) / 3
    for ((index, color) in listOf("#0055a4", "#ffffff", "#ef4135").withIndex()) {
        drawPath(rect(left + third * index, top, left + third * (index + 1), bottom), fill(col(color)))
    }
}

// The Union Jack, simplified for an icon: white and red diagonals, then the white and red cross.
fun flagEnglish(p: Palette) = flag(p) { left, top, right, bottom ->
    val centerX = (left + right) / 2
    val centerY = (top + bottom) / 2
    drawPath(rect(left, top, right, bottom), fill(col("#012169")))
    val diagonals = path {
        moveTo(left, top)
        lineTo(right, bottom)
        moveTo(right, top)
        lineTo(left, bottom)
    }
    drawPath(diagonals, stroke(col("#ffffff"), 11.0))
    drawPath(diagonals, stroke(col("#c8102e"), 4.0))
    drawPath(rect(centerX - 8, top, centerX + 8, bottom), fill(col("#ffffff")))
    drawPath(rect(left, centerY - 8, right, centerY + 8), fill(col("#ffffff")))
    drawPath(rect(centerX - 4.5, top, centerX + 4.5, bottom), fill(col("#c8102e")))
    drawPath(rect(left, centerY - 4.5, right, centerY + 4.5), fill(col("#c8102e")))
}
