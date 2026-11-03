package fr.mwet.tools.stylegenerator

import org.jetbrains.skia.Paint
import org.jetbrains.skia.PathBuilder
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.pow
import kotlin.math.sin

// The ground tiles are 8 by 8 cells and repeat seamlessly.
// A tile is 512 by 512 px, y down. A pattern repeats seamlessly when what leaves one edge comes back on the opposite
// edge: the patterns below use periods that divide 512, or draw the wrapped copies.
const val TILE_CELLS = 8
const val GROUND_SIZE = TILE_CELLS * CELL

// The grid line of one cell: a rounded square inset 2 px, 12 px corner radius, 2.5 px line in the outline color. The
// game draws it on every board cell at a low alpha (10% in the previews).
fun gridCell(p: Palette) = Raster(CELL, CELL).apply {
    canvas.drawPath(rect(2.0, 2.0, CELL - 2.0, CELL - 2.0, 12.0), stroke(col(p.outline), 2.5))
}

// The checker board: one cell in two darkened by the grout.
// The grout color at 35% alpha over the ground color; raise the alpha for a stronger contrast between the squares.
fun groundTile(p: Palette) = Raster(GROUND_SIZE, GROUND_SIZE).apply {
    canvas.drawRect(rectF(0.0, 0.0, GROUND_SIZE.toDouble(), GROUND_SIZE.toDouble()), fill(col(p.ground)))
    val alternate = fill(col(p.grout, 0.35))
    for (i in 0 until TILE_CELLS) {
        for (j in 0 until TILE_CELLS) {
            if ((i + j) % 2 != 0) {
                canvas.drawRect(rectF(i * CELL.toDouble(), j * CELL.toDouble(), (i + 1) * CELL.toDouble(), (j + 1) * CELL.toDouble()), alternate)
            }
        }
    }
}

// The grout, or a faint outline when the grout is too close to the ground to show.
// "Too close" is a luminance difference of 4% or less; the outline then gets 30% of the alpha.
private fun groundLine(p: Palette, alpha: Double = 1.0) =
    if (abs(luminance(p.grout) - luminance(p.ground)) > 0.04) col(p.grout, alpha) else col(p.outline, alpha * 0.3)

// A tile filled with the ground color, the background of the patterns.
private fun groundCanvas(p: Palette) = Raster(GROUND_SIZE, GROUND_SIZE).apply {
    canvas.drawRect(rectF(0.0, 0.0, GROUND_SIZE.toDouble(), GROUND_SIZE.toDouble()), fill(col(p.ground)))
}

// Draws at (x, y) and at the eight copies one tile away, so that what crosses an edge comes back on the other side.
private inline fun wrapped(x: Double, y: Double, draw: (Double, Double) -> Unit) {
    for (dx in listOf(-GROUND_SIZE, 0, GROUND_SIZE)) {
        for (dy in listOf(-GROUND_SIZE, 0, GROUND_SIZE)) draw(x + dx, y + dy)
    }
}

// The ground color only.
private fun groundPlain(p: Palette) = groundCanvas(p)

// Sand grains: 320 dots of 1.4 to 3 px radius at random places, 70% dark (grout) and 30% light (the ground 25% toward
// white). Change the count for a denser or sparser grain, the radius range for coarser grains.
private fun groundGrain(p: Palette) = groundCanvas(p).apply {
    val rng = PythonRandom(7)
    val dark = fill(groundLine(p, 0.9))
    val light = fill(col(tintHex(p.ground, 0.25), 0.8))
    repeat(320) {
        val x = rng.uniform(0.0, GROUND_SIZE.toDouble())
        val y = rng.uniform(0.0, GROUND_SIZE.toDouble())
        val radius = rng.uniform(1.4, 3.0)
        val paint = if (rng.random() < 0.7) dark else light
        wrapped(x, y) { px, py -> canvas.drawCircle(px.toFloat(), py.toFloat(), radius.toFloat(), paint) }
    }
}

// Diagonal bands half a cell wide, one per cell.
// Each band is a parallelogram that goes down and right at 45 degrees: 32 px wide at the top edge, moved by 512 px at
// the bottom edge. The bands start 64 px apart, so the pattern repeats on all edges. The grout is at 45% alpha.
private fun groundStripes(p: Palette) = groundCanvas(p).apply {
    val paint = fill(groundLine(p, 0.45))
    val half = CELL / 2.0
    // From 16 cells left of the tile to its right edge: enough bands to cover the tile once they slant.
    for (k in -TILE_CELLS * 2..TILE_CELLS) {
        val x0 = k * CELL.toDouble()
        val band = path {
            moveTo(x0, 0.0)
            lineTo(x0 + half, 0.0)
            lineTo(x0 + half + GROUND_SIZE, GROUND_SIZE.toDouble())
            lineTo(x0 + GROUND_SIZE, GROUND_SIZE.toDouble())
            closePath()
        }
        canvas.drawPath(band, paint)
    }
}

// Flagstones of 2 by 2 cells on a grout background, each a little lighter or darker.
private fun groundTiles(p: Palette) = Raster(GROUND_SIZE, GROUND_SIZE).apply {
    val all = rectF(0.0, 0.0, GROUND_SIZE.toDouble(), GROUND_SIZE.toDouble())
    // The grout color over the ground color: the joints between the stones.
    canvas.drawRect(all, fill(col(p.ground)))
    canvas.drawRect(all, fill(groundLine(p)))
    val rng = PythonRandom(3)
    val size = CELL * 2.0
    for (i in 0 until TILE_CELLS / 2) {
        for (j in 0 until TILE_CELLS / 2) {
            val x = i * size
            val y = j * size
            // Each stone is up to 5% lighter or darker than the ground.
            val base = tintHex(p.ground, rng.uniform(-0.05, 0.05))
            // The stone, inset 3 px: the joints are 6 px wide. Corner radius 8.
            canvas.drawPath(rect(x + 3, y + 3, x + size - 3, y + size - 3, 8.0), fill(col(base)))
            // A light line 4 px inside the edge of the stone (20% toward white, 70% alpha): the bevel of the stone.
            canvas.drawPath(rect(x + 7, y + 7, x + size - 7, y + size - 7, 6.0), stroke(col(tintHex(base, 0.2), 0.7), 2.0))
        }
    }
}

// Overlapping circles, each row shifted by half a cell and drawn over the one above.
// The circles have a radius of half a cell and the rows are half a cell apart: each row hides the lower part of the
// row above, so only the round tops show, like fish scales. The rows alternate slightly darker (-3%) and lighter (+2%).
private fun groundScales(p: Palette) = groundCanvas(p).apply {
    val radius = CELL / 2.0
    val line = stroke(groundLine(p), 3.0)
    val light = stroke(col(tintHex(p.ground, 0.25), 0.7), 2.0)
    // Two extra rows and columns on each side, so that the tile edges are covered.
    for (j in -2 until TILE_CELLS * 2 + 2) {
        val y = j * radius
        val odd = Math.floorMod(j, 2) == 1
        val offset = if (odd) radius else 0.0
        val base = fill(col(tintHex(p.ground, if (odd) -0.03 else 0.02)))
        for (i in -1 until TILE_CELLS + 1) {
            val x = i * CELL + offset
            canvas.drawCircle(x.toFloat(), y.toFloat(), radius.toFloat(), base)
            // The edge line of the scale, 3 px, just inside the circle.
            canvas.drawCircle(x.toFloat(), y.toFloat(), (radius - 1.5).toFloat(), line)
            // A light arc 8 px inside the circle, from 30 to 150 degrees: the bottom of the scale (y is down). The
            // next row hides most of it; a short glint stays at the bottom tip of each scale.
            canvas.drawArc(
                (x - radius + 8).toFloat(), (y - radius + 8).toFloat(), (x + radius - 8).toFloat(), (y + radius - 8).toFloat(),
                30f, 120f, false, light,
            )
        }
    }
}

// Wavy crests, two per cell; the sines have whole periods over the tile, so the waves join at the edges.
// Sixteen crests, 32 px apart, the first 16 px from the top. Each crest is a dark 3.5 px line with a light 2 px line
// 3.5 px above it: the lit side of the dune.
private fun groundDunes(p: Palette) = groundCanvas(p).apply {
    val dark = stroke(groundLine(p), 3.5)
    val light = stroke(col(tintHex(p.ground, 0.3), 0.8), 2.0)
    for (k in 0 until TILE_CELLS * 2) {
        val base = k * CELL / 2.0 + CELL / 4.0
        for ((paint, shift) in listOf<Pair<Paint, Double>>(light to -3.5, dark to 0.0)) {
            val path = PathBuilder()
            // A polyline with a point every 4 px, from 8 px before the left edge to 8 px after the right edge.
            for ((step, x) in (-8..GROUND_SIZE + 8 step 4).withIndex()) {
                val t = x.toDouble() / GROUND_SIZE * (2 * PI)
                // Two sines: 2 waves of 7 px and 5 waves of 3 px over the tile. The phase changes with k, so that the
                // crests do not line up. The amplitudes set how wavy the dunes are.
                val y = base + shift + 7 * sin(2 * t + k * 0.7) + 3 * sin(5 * t + k * 1.3)
                if (step == 0) path.moveTo(x.toDouble(), y) else path.lineTo(x.toDouble(), y)
            }
            canvas.drawPath(path.detach(), paint)
        }
    }
}

// Flat-top hexagons: 8 columns and 7 rows over the tile, odd columns shifted by half a row.
private fun groundHex(p: Palette) = groundCanvas(p).apply {
    val columns = TILE_CELLS
    val rows = 7
    val stepX = GROUND_SIZE.toDouble() / columns
    val stepY = GROUND_SIZE.toDouble() / rows
    // The half width of a hexagon. Flat-top hexagons that touch are 1.5 half widths apart. With 7 rows the hexagons
    // are almost regular (36.6 px half height for 36.9).
    val rx = stepX / 1.5
    val half = stepY / 2
    val line = stroke(groundLine(p), 3.0)
    // One extra column and row on each side, so that the tile edges are covered.
    for (i in -1..columns) {
        for (j in -1..rows) {
            val cx = i * stepX
            val cy = j * stepY + if (Math.floorMod(i, 2) == 1) half else 0.0
            val hexagon = path {
                moveTo(cx - rx, cy)
                lineTo(cx - rx / 2, cy - half)
                lineTo(cx + rx / 2, cy - half)
                lineTo(cx + rx, cy)
                lineTo(cx + rx / 2, cy + half)
                lineTo(cx - rx / 2, cy + half)
                closePath()
            }
            // Four tones from -3% to +3% of the ground color, in a fixed mixed order. The column and row are taken
            // modulo the tile, so the copies past the edges get the same tone as the hexagon they repeat.
            val tone = (Math.floorMod(i, columns) * 3 + Math.floorMod(j, rows) * 5) % 4
            canvas.drawPath(hexagon, fill(col(tintHex(p.ground, (tone - 1.5) * 0.02))))
            canvas.drawPath(hexagon, line)
        }
    }
}

// The alternative ground patterns, each written to its own file next to the checker ground.png.
val GROUNDS: Map<String, (Palette) -> Raster> = linkedMapOf(
    "groundPlain" to ::groundPlain,
    "groundGrain" to ::groundGrain,
    "groundStripes" to ::groundStripes,
    "groundTiles" to ::groundTiles,
    "groundScales" to ::groundScales,
    "groundDunes" to ::groundDunes,
    "groundHex" to ::groundHex,
)

// The values are computed in single precision, as numpy did.

// A white disc whose alpha fades from the centre: the game tints it and adds it under the snake and the fruit.
// r is the distance to the centre, 0 at the centre and 1 at the edge. The alpha is (1 - r) to the power 2.2: a higher
// power keeps the light closer to the centre, a lower one spreads it.
fun lightGlow(size: Int = 128): Pixels {
    val out = Pixels(size, size)
    val half = size / 2f
    for (y in 0 until size) {
        for (x in 0 until size) {
            val r = hypot(x + 0.5f - half, y + 0.5f - half) / half
            val alpha = (1 - r).coerceIn(0f, 1f).toDouble().pow(2.2f.toDouble()).toFloat()
            out[x, y, 0] = 255
            out[x, y, 1] = 255
            out[x, y, 2] = 255
            out[x, y, 3] = (alpha * 255).toInt()
        }
    }
    return out
}

// A shadow along the board edges, under a 2 px line of the frame color.
// The image is the board, 16 by 20 cells of 16 px; the game stretches it over the board. d is the distance from the
// pixel to the nearest edge, in cells. The shadow fades over 0.9 cell, from 45% alpha at the edge to 0, squared so that
// it falls off quickly. The middle of the board stays transparent.
fun boardFrame(p: Palette): Pixels {
    val w = 256
    val h = 320
    val cell = w / 16f
    val (r, g, b) = rgb(p.frame)
    val out = Pixels(w, h)
    for (y in 0 until h) {
        for (x in 0 until w) {
            val xx = x + 0.5f
            val yy = y + 0.5f
            val d = minOf(minOf(xx, yy), minOf(w - xx, h - yy)) / cell
            val fade = (1 - d / 0.9f).coerceIn(0f, 1f)
            val alpha = if (d * cell < 2.0f) 1f else fade * fade * 0.45f
            out[x, y, 0] = r
            out[x, y, 1] = g
            out[x, y, 2] = b
            out[x, y, 3] = (alpha * 255).toInt()
        }
    }
    return out
}
