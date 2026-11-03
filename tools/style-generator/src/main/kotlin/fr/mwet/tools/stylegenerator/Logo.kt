package fr.mwet.tools.stylegenerator

import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Rect

// How far the drop shadow of the logo is moved right and down, in px.
private const val LOGO_SHADOW = 14.0

// The "SNEK" title on a transparent 640 by 260 px canvas, y down. From back to front: a dark drop shadow, a dark
// outline, a light rim in the text color, the letters in two tones of primary split by a zigzag, a light gloss band
// across the top of the letters, and two eyes on the S.
fun logo(p: Palette, width: Int = 640, height: Int = 260) = Raster(width, height).apply {
    val c = canvas
    // The letter outlines at 190 px font size, as one path.
    val (glyphs, _) = textPath("SNEK", 190.0)
    val raw = glyphs.computeTightBounds()
    // Shift the text up and left by about half the shadow offset: text and shadow together stay centred.
    val dx = (width - raw.width.toDouble()) / 2 - raw.left - LOGO_SHADOW / 2
    val dy = (height - raw.height.toDouble()) / 2 - raw.top - 12
    val path = glyphs.translated(dx, dy)
    val bounds = path.computeTightBounds()
    if (p.tubeLogo) {
        // Neon look: flat letters in the panel color with an 8 px line on their edge, no shadow and no shading.
        c.drawPath(path, fill(col(p.panel)))
        c.drawPath(path, stroke(col(p.outline), 8.0))
        c.logoEyes(p, bounds, 5.0)
        return@apply
    }
    // The drop shadow: the letters grown by 17 px, filled with the outline color, LOGO_SHADOW px right and down.
    val shadow = path.translated(LOGO_SHADOW, LOGO_SHADOW)
    c.drawPath(shadow, stroke(col(p.outline), 34.0))
    c.drawPath(shadow, fill(col(p.outline)))
    // Two strokes centred on the letter edges: the dark one reaches 17 px out, the text-colored one 8 px out. They
    // leave an 8 px light rim around the letters and a 9 px dark outline around the rim.
    c.drawPath(path, stroke(col(p.outline), 34.0))
    c.drawPath(path, stroke(col(p.text), 16.0))
    c.clipped(path) {
        // The lower part of the letters, in primaryDark.
        drawPath(path, fill(col(p.primaryDark)))
        // The lighter top half ends on a zigzag.
        // The top 55% of the letters in primary. The lower edge of this area goes from right to left through 9
        // points, alternately 6 px below and above the split line: a gentle zigzag. Change steps for more teeth, the
        // 6 for deeper ones. The 20 px margins make sure the area covers the letters entirely.
        val left = bounds.left.toDouble()
        val top = bounds.top.toDouble()
        val right = bounds.right.toDouble()
        val split = top + bounds.height * 0.55
        val wave = path {
            moveTo(left - 20, top - 20)
            lineTo(right + 20, top - 20)
            lineTo(right + 20, split)
            val steps = 8
            for (i in steps downTo 0) {
                val x = left - 20 + (bounds.width + 40.0) * i / steps
                lineTo(x, split + if (i % 2 != 0) 6 else -6)
            }
            closePath()
        }
        drawPath(wave, fill(col(p.primary)))
        // The gloss: a 12 px band in primaryLight at 80% alpha, 10 px below the top of the letters.
        drawPath(rect(left, top + 10, right, top + 22), fill(col(p.primaryLight, 0.8)))
    }
    c.logoEyes(p, bounds, 9.0)
}

// Two eyes on the S, looking right.
// The eyes are 13 px discs, 26 px apart, on the top of the S: 62 px right of the left edge of the letters and 30 px
// below their top. The pupils (6.5 px) are moved 4 px right and 2 px down; each has a white glint at its top left.
// outlineWidth is the stroke width of the outline ring: half of it shows outside the eye.
private fun Canvas.logoEyes(p: Palette, bounds: Rect, outlineWidth: Double) {
    val eyeY = bounds.top + 30.0
    val eyeX = bounds.left + 62.0
    for (ox in listOf(0, 26)) {
        drawPath(circle(eyeX + ox, eyeY, 13.0), stroke(col(p.outline), outlineWidth))
        drawPath(circle(eyeX + ox, eyeY, 13.0), fill(col(p.eye)))
        drawPath(circle(eyeX + ox + 4, eyeY + 2, 6.5), fill(col(p.pupil)))
        drawPath(circle(eyeX + ox + 1.5, eyeY - 1, 2.2), fill(col("#ffffff")))
    }
}

// The "Loading..." text alone, 92 px, centred on a 1500 by 120 px canvas.
fun loadingText(p: Palette) = Raster(1500, 120).apply {
    canvas.outlinedText("Loading...", 750.0, 92.0, 92.0, p, align = Align.CENTER)
}

// The 640 by 400 px splash screen: the ground tiled at 75% scale, the board frame shadow stretched over the whole
// image, the logo at 75% scale (480 by 195 px) in the upper part, and "Loading..." below it.
fun splash(p: Palette, tile: Raster, logo: Raster) = Raster(640, 400).apply {
    val c = canvas
    c.tileFill(tile.image(), 0.0, 0.0, 640.0, 400.0, 0.75)
    c.drawImage(boardFrame(p).resized(640, 400, Resample.LANCZOS).toImage(), 0f, 0f)
    c.drawImageRect(logo.image(), rectF(80.0, 70.0, 560.0, 265.0), LINEAR)
    c.outlinedText("Loading...", 320.0, 340.0, 34.0, p, align = Align.CENTER)
}
