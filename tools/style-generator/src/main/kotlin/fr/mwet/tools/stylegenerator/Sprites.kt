package fr.mwet.tools.stylegenerator

import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Path
import org.jetbrains.skia.PathFillMode
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// Every sprite is one cell of CELL by CELL pixels, x to the right and y down, (0, 0) at the top left corner. The
// sprites are drawn for a snake that moves left: the head looks toward x = 0, the neck, the body and the tail connect
// toward the head on their left side. The game rotates them. The body runs past the cell on both sides so that neighbour
// cells overlap without seams: the rounded ends of the outline fall outside the canvas, so the band is open at the cell
// edges.
const val CELL = 64
// The middle line of the cell: the snake body is centred on y = MID.
const val MID = CELL / 2.0
// Half the thickness of the body: the body covers y = MID - 19 to MID + 19, 38 px of the 64, plus the outline on
// each side. A larger value gives a fatter snake: the turn, the tail and the neck use it too, the head oval does not.
const val BODY = 19
// Visible thickness of the dark outline around the snake and the fruits (see outlined()).
const val OUTLINE = 4.0
// The eye centre, in px from the left edge of the live head sprite (the front of the head is at x = 4).
const val EYE_FORWARD = 22
// The distance from the eye centre to the middle line: one eye above it, one below.
const val EYE_SIDE = 14.5
const val EYE_RADIUS = 9.0
const val PUPIL_RADIUS = 4.4
// How far the game moves the pupil from the eye centre toward the fruit. With a 9 px eye and a 4.4 px pupil, 3.6 keeps
// about 1 px of white around the pupil at the largest travel.
const val PUPIL_TRAVEL = 3.6
// Canvas size of the separate pupil sprite and of the back mark sprites.
const val PUPIL_SPRITE = 16
const val MARK_SPRITE = 40

// The darker rim along the inside edge of a body part, which makes the body look round. The 12 px stroke is centred
// on the edge and clipped to the inside, so 6 px of snakeDark remain. Change the width to make the rim thicker.
private fun Canvas.bodyHighlight(p: Palette, path: Path) = clipped(path) {
    drawPath(path, stroke(col(p.snakeDark), 12.0))
}

// The two thin light lines along the body, clipped to the body shape. 60% alpha of snakeLight. In the base layer
// snakeLight is CLEAR, so they are holes; in the detail layer it is translucent white (see Palette.layer).
private fun Canvas.sideStripes(p: Palette, path: Path, stripes: List<Path>) = clipped(path) {
    for (stripe in stripes) drawPath(stripe, fill(col(p.snakeLight, 0.6)))
}

// The two stripes are 2.5 px thick, between 8.5 and 11 px from the middle line: in the middle of the body, away from
// the dark rim. right is where they stop: past the cell for a straight part, before the taper for the tail.
private fun straightStripes(right: Double = CELL + 12.0) =
    listOf(rect(-12.0, MID - 11, right, MID - 8.5), rect(-12.0, MID + 8.5, right, MID + 11))

// A straight body part: a horizontal band 38 px thick across the whole cell, with its outline above and below, the
// dark rim and the two light stripes.
fun snakeBodyStraight(p: Palette) = Raster(CELL, CELL).apply {
    // From x = -12 to 76: 12 px past each side, so that the outline ends are cut off by the canvas.
    val path = rect(-12.0, MID - BODY, CELL + 12.0, MID + BODY)
    canvas.outlined(path, p.snake, p)
    canvas.bodyHighlight(p, path)
    canvas.sideStripes(p, path, straightStripes())
}

// A quarter ring around the bottom right corner joins the right and bottom edges. The ring is centred on the corner
// (64, 64), between the radii MID - BODY = 13 and MID + BODY = 51: where it meets the right edge it covers y = 13 to 51,
// the same band as a straight part, and the same x range on the bottom edge. Only the quarter inside the canvas shows.
fun snakeBodyTurn(p: Palette) = Raster(CELL, CELL).apply {
    // EVEN_ODD turns the two circles into a ring: the inner disc becomes a hole.
    val shape = path {
        setFillType(PathFillMode.EVEN_ODD)
        addCircle(CELL.toFloat(), CELL.toFloat(), (MID + BODY).toFloat())
        addCircle(CELL.toFloat(), CELL.toFloat(), (MID - BODY).toFloat())
    }
    canvas.outlined(shape, p.snake, p)
    canvas.bodyHighlight(p, shape)
    // The side stripes as two arcs: 9.75 px on each side of the middle radius, the middle of the straight stripes
    // (8.5 to 11), and 2.5 px wide like them, so that they continue the stripes of the straight parts.
    canvas.clipped(shape) {
        for (radius in listOf(MID - 9.75, MID + 9.75)) {
            val arc = path { addCircle(CELL.toFloat(), CELL.toFloat(), radius.toFloat()) }
            drawPath(arc, stroke(col(p.snakeLight, 0.6), 2.5))
        }
    }
}

// The tail: the body band from the left edge, then a taper to a point on the right. The tip is at x = 58, which leaves
// room for the 4 px outline in the 64 px cell.
fun snakeTail(p: Palette) = Raster(CELL, CELL).apply {
    val shape = path {
        moveTo(-12.0, MID - BODY)
        // Full thickness up to x = 14, then the taper starts.
        lineTo(14.0, MID - BODY)
        // Upper half of the taper. The first control point (34, top edge) keeps the width for a while, the second
        // (48, 8 px above the middle) pulls the curve to the tip. Move them right for a fatter tail, left for a thinner one.
        cubicTo(34.0, MID - BODY, 48.0, MID - 8, 58.0, MID)
        // Lower half, the mirror of the upper half.
        cubicTo(48.0, MID + 8, 34.0, MID + BODY, 14.0, MID + BODY)
        lineTo(-12.0, MID + BODY)
        closePath()
    }
    canvas.outlined(shape, p.snake, p)
    canvas.bodyHighlight(p, shape)
    // The stripes stop at x = 30, where the taper becomes too thin for them.
    canvas.sideStripes(p, shape, straightStripes(right = 30.0))
}

// An arrow head pointing right, centred on (0, 0), r is its half height: the tip at (0.8 r, 0), the arms go back to
// (-0.3 r, ±r) and (-0.8 r, ±0.65 r), and the notch between the arms is at (0, 0).
fun chevron(r: Double): Path = path {
    moveTo(r * 0.8, 0.0)
    lineTo(-r * 0.3, -r)
    lineTo(-r * 0.8, -r * 0.65)
    lineTo(0.0, 0.0)
    lineTo(-r * 0.8, r * 0.65)
    lineTo(-r * 0.3, r)
    closePath()
}

// Centred on (0, 0), pointing right. The marks are the pattern on the snake back, one per body part; the game turns
// them toward the head. r is the size in px of the 40 px mark sprite; the factors give the shapes the same visual weight.
fun markPath(shape: MarkShape, r: Double = 11.0): Path = when (shape) {
    MarkShape.DIAMOND -> diamond(0.0, 0.0, r, r)
    // A circle with the radius r looks bigger than the diamond: 82% of it.
    MarkShape.CIRCLE -> circle(0.0, 0.0, r * 0.82)
    // A square of 1.44 r per side with 3 px rounded corners.
    MarkShape.SQUARE -> rect(-r * 0.72, -r * 0.72, r * 0.72, r * 0.72, 3.0)
    MarkShape.CHEVRON -> chevron(r)
    // Five points: ten vertices, alternately on the outer radius (1.05 r) and the inner radius (0.45 r). The first
    // point is at angle 0, so the star points right. A smaller inner radius gives thinner points.
    MarkShape.STAR -> path {
        for (i in 0 until 10) {
            val radius = if (i % 2 == 0) r * 1.05 else r * 0.45
            val angle = PI * i / 5
            if (i == 0) moveTo(cos(angle) * radius, sin(angle) * radius) else lineTo(cos(angle) * radius, sin(angle) * radius)
        }
        closePath()
    }
    // A heart lying on its side: the tip at (r, 0) points right, the two lobes are on the left, the dip between them
    // at (-0.55 r, 0). Four curves: tip to top lobe, top lobe to dip, dip to bottom lobe, bottom lobe to tip.
    MarkShape.HEART -> path {
        moveTo(r, 0.0)
        cubicTo(r * 0.2, -r * 0.55, -r * 0.2, -r * 1.1, -r * 0.7, -r * 0.75)
        cubicTo(-r * 1.15, -r * 0.4, -r * 0.95, 0.0, -r * 0.55, 0.0)
        cubicTo(-r * 0.95, 0.0, -r * 1.15, r * 0.4, -r * 0.7, r * 0.75)
        cubicTo(-r * 0.2, r * 1.1, r * 0.2, r * 0.55, r, 0.0)
        closePath()
    }
}

// White, so that the game tints it with the mark color.
fun markSprite(p: Palette, path: Path) = Raster(MARK_SPRITE, MARK_SPRITE).apply {
    // Moves (0, 0) to the centre of the 40 px sprite, where markPath centres the shape.
    canvas.translate(MARK_SPRITE / 2.0, MARK_SPRITE / 2.0)
    if (p.tubeMarks) {
        // Neon look: only a 3.5 px white line along the shape, hollow inside.
        canvas.drawPath(path, stroke(col("#ffffff"), 3.5))
    } else {
        // A dark ring 1.75 px wide around the white shape. The 7 px stroke reaches 3.5 px past the edge; the white
        // 3.5 px stroke then covers the inner 1.75 px of it. The white stroke also rounds the corners of the shape.
        // The dark ring is at 90% alpha; the game multiplies it by the mark color too, so it stays dark.
        canvas.drawPath(path, stroke(col(p.markOutline ?: p.outline, 0.9), 7.0))
        canvas.drawPath(path, stroke(col("#ffffff"), 3.5))
        canvas.drawPath(path, fill(col("#ffffff")))
    }
}

// The head outline: the neck band (x = 30 to 76, the body thickness) joined to an oval 50 px high (25 px on each side
// of the middle, wider than the 19 px of the body) from x = front to 54. front is the left end of the snout.
private fun headShape(front: Double) = union(rect(30.0, MID - BODY, CELL + 12.0, MID + BODY), oval(front, MID - 25, 54.0, MID + 25))

// The pupil disc and the small white glint at its upper left, which makes the eye look shiny.
private fun Canvas.pupil(p: Palette, x: Double, y: Double) {
    drawPath(circle(x, y, PUPIL_RADIUS), fill(col(p.pupil)))
    drawPath(circle(x - 1.5, y - 1.7, 1.5), fill(col("#ffffff")))
}

// One pupil centred in a 16 px sprite. The game draws it twice on the head, at the eye centres moved toward the fruit.
fun snakePupil(p: Palette) = Raster(PUPIL_SPRITE, PUPIL_SPRITE).apply {
    canvas.pupil(p, PUPIL_SPRITE / 2.0, PUPIL_SPRITE / 2.0)
}

// The eyes have no pupil: the game draws snakePupil on top, looking at the fruit. look draws one in the sprite.
// The head looks left: the snout on the left, the neck goes off the right edge to join the body. look is the offset
// of the pupils from the eye centres, in px (the app icon uses it). dead draws the game over head: crosses instead of
// eyes and the tongue out. In the base layer the eyes, the nostrils and the tongue are CLEAR colors: they cut holes in
// the gray head, and the detail layer draws them.
fun snakeHead(p: Palette, dead: Boolean = false, look: Pair<Double, Double>? = null) = Raster(CELL, CELL).apply {
    val c = canvas
    // The dead head is 5 px shorter at the front, to leave room for the tongue.
    val front = if (dead) 9.0 else 4.0
    if (dead) {
        // A forked tongue out of the mouth: a line from inside the head (x = 15) to x = 2.5, then the two prongs from
        // x = 5 to the left edge, 4 px above and below. Drawn first, so the head covers its root.
        val tongue = path {
            moveTo(front + 6, MID)
            lineTo(2.5, MID)
            moveTo(5.0, MID)
            lineTo(1.0, MID - 4)
            moveTo(5.0, MID)
            lineTo(1.0, MID + 4)
        }
        // Outline then color: a 2 px tongue with 1.25 px of outline on each side.
        c.drawPath(tongue, stroke(col(p.outline), 4.5))
        c.drawPath(tongue, stroke(col(p.tongue), 2.0))
    }
    val path = headShape(front)
    c.outlined(path, p.snake, p)
    c.bodyHighlight(p, path)
    // The side stripes of the body continue on the neck, from x = 40 to the right edge.
    c.clipped(path) {
        drawPath(rect(40.0, MID - 11, CELL + 12.0, MID - 8.5), fill(col(p.snakeLight, 0.6)))
        drawPath(rect(40.0, MID + 8.5, CELL + 12.0, MID + 11), fill(col(p.snakeLight, 0.6)))
    }
    // side = -1 is the upper eye and nostril, 1 the lower one.
    for (side in listOf(-1, 1)) {
        // x = 22 for the live head. The eyes overlap the edge of the head, which makes them bulge.
        val ex = front + EYE_FORWARD - 4
        val ey = MID + side * EYE_SIDE
        if (dead) {
            // A white eye of 8 px (1 px smaller) with a 3 px outline ring, and an X of 7 by 7 px in the pupil color.
            c.drawPath(circle(ex, ey, 8.0), stroke(col(p.outline), 6.0))
            c.drawPath(circle(ex, ey, 8.0), fill(col(p.eye)))
            val cross = path {
                moveTo(ex - 3.5, ey - 3.5)
                lineTo(ex + 3.5, ey + 3.5)
                moveTo(ex + 3.5, ey - 3.5)
                lineTo(ex - 3.5, ey + 3.5)
            }
            c.drawPath(cross, stroke(col(p.pupil), 2.6))
        } else {
            // The white of the eye with a 3 px outline ring: the 6 px stroke is centred on the edge, the fill covers
            // its inner half.
            c.drawPath(circle(ex, ey, EYE_RADIUS), stroke(col(p.outline), 6.0))
            c.drawPath(circle(ex, ey, EYE_RADIUS), fill(col(p.eye)))
            if (look != null) c.pupil(p, ex + look.first, ey + look.second)
        }
        // The two nostrils: small dark dots near the snout, 4.5 px above and below the middle line.
        c.drawPath(circle(front + 5, MID + side * 4.5, 1.6), fill(col(p.outline)))
    }
}
