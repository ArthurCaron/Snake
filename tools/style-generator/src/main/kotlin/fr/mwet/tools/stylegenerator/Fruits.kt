package fr.mwet.tools.stylegenerator

import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Matrix33
import org.jetbrains.skia.Path
import org.jetbrains.skia.Rect
import kotlin.math.cos
import kotlin.math.sin

// The fruit kinds, and the color each one gets when the style gives no fruitColors.
enum class FruitKind(val naturalColor: String) {
    APPLE("#e0443a"),
    STRAWBERRY("#e8344a"),
    CHERRY("#c0203f"),
    BANANA("#f2cf3a"),
    GRAPES("#8a4aa8"),
    ORANGE("#f28a1f"),
    PEAR("#a8c43a"),
    LEMON("#f5e04a"),
    WATERMELON("#f0485a"),
    PRICKLY("#d8467b");

    val id = name.lowercase()
    val title = id.replaceFirstChar { it.uppercase() }
}

// A leaf of the given length and half width, from (x, y) toward the angle in degrees.
// The leaf is an almond: two curves from the base (0, 0) to the tip (length, 0), one above and one below. The control
// points are at width from the axis, so the leaf is about 0.75 width wide on each side. The angle turns clockwise
// because y is down: 0 points right, -90 points up, -160 points left and a little up.
private fun leafPath(x: Double, y: Double, length: Double, width: Double, angle: Double): Path = path {
    moveTo(0.0, 0.0)
    cubicTo(length * 0.3, -width, length * 0.75, -width, length, 0.0)
    cubicTo(length * 0.75, width, length * 0.3, width, 0.0, 0.0)
    closePath()
}.let { leaf ->
    // Rotation around the base, then the base moves to (x, y).
    val matrix = rotation(angle).mat
    matrix[2] += x.toFloat()
    matrix[5] += y.toFloat()
    leaf.transformed(Matrix33(*matrix))
}

// A short brown stem on top of the fruit and a leaf from its side. (x, y) is the top of the fruit.
private fun Canvas.stemAndLeaf(p: Palette, x: Double, y: Double, leafAngle: Double = -25.0, leafLength: Double = 15.0) {
    // From 6 px inside the fruit to 5 px above its top, curving to the right.
    val stem = path {
        moveTo(x, y + 6)
        quadTo(x + 1, y - 1, x + 4, y - 5)
    }
    // A 2.5 px stem with 1.75 px of outline on each side.
    drawPath(stem, stroke(col(p.outline), 6.0))
    drawPath(stem, stroke(col(p.stem), 2.5))
    // The leaf starts on the stem and has a thinner outline (2.5 px) than the fruit (4 px).
    outlined(leafPath(x + 3, y - 1, leafLength, 5.5, leafAngle), p.leaf, p, width = 2.5)
    // The vein: a darker line along the leaf axis, over 65% of its length.
    val radians = Math.toRadians(leafAngle)
    val vein = path {
        moveTo(x + 5, y - 2)
        lineTo(x + 5 + leafLength * 0.65 * cos(radians), y - 2 + leafLength * 0.65 * sin(radians))
    }
    drawPath(vein, stroke(col(p.leafDark), 1.5))
}

// A fan of leaves from the same point, one per angle: the crown of the strawberry and of the prickly fruit.
private fun Canvas.crown(p: Palette, x: Double, y: Double, angles: List<Double>, length: Double, width: Double) {
    for (angle in angles) outlined(leafPath(x, y, length, width, angle), p.leaf, p, width = 2.5)
}

// The silhouette of each fruit in the 64 px cell, y down. The fruits sit on the same base line, about y = 58, so that
// the shadow (y = 53 to 63) and the squash animation fit all of them. The top part (leaves, stems) is drawn by fruitTop.
private fun fruitBody(kind: FruitKind): Path = when (kind) {
    // An upright egg, 40 by 45 px.
    FruitKind.PRICKLY -> oval(MID - 20, 13.0, MID + 20, 58.0)
    // Wide shoulders at y = 24 and a point at the bottom (MID, 58). The top is a flat curve near y = 16, under the crown.
    FruitKind.STRAWBERRY -> path {
        moveTo(MID, 58.0)
        cubicTo(MID - 12, 54.0, MID - 22, 36.0, MID - 19, 24.0)
        cubicTo(MID - 16, 13.0, MID + 16, 13.0, MID + 19, 24.0)
        cubicTo(MID + 22, 36.0, MID + 12, 54.0, MID, 58.0)
        closePath()
    }
    // Two balls side by side, the right one 2 px higher. Their stems are drawn by fruitTop.
    FruitKind.CHERRY -> union(circle(21.0, 45.0, 11.5), circle(43.0, 43.0, 11.5))
    // A crescent, open at the top: the outer curve from the left tip (9, 25) down to the right tip (57, 30), a short
    // line across the right tip, then the inner curve back to the left tip. The gap between the two curves is the
    // thickness of the banana: move the inner control points (42, 45) and (22, 44) down for a thinner banana.
    FruitKind.BANANA -> path {
        moveTo(9.0, 25.0)
        cubicTo(12.0, 54.0, 46.0, 60.0, 57.0, 30.0)
        lineTo(54.0, 25.0)
        cubicTo(42.0, 45.0, 22.0, 44.0, 16.0, 22.0)
        closePath()
    }
    // A bunch of eight grapes of 8.2 px radius, in an upside-down triangle: three at the top (y = 24 to 25), four in
    // the middle row (y = 35) and one at the bottom (y = 46).
    FruitKind.GRAPES -> {
        val centers = listOf(22 to 25, 32 to 24, 42 to 25, 27 to 35, 37 to 35, 32 to 46, 21 to 35, 43 to 35)
        union(*centers.map { (x, y) -> circle(x.toDouble(), y.toDouble(), 8.2) }.toTypedArray())
    }
    // A disc of 20 px radius, centred low in the cell.
    FruitKind.ORANGE -> circle(MID, 37.0, 20.0)
    // A narrow neck at the top (MID, 16) that widens into a round bottom at y = 58. Each side has two curves: the neck
    // to the waist (MID ± 12, 33), then the waist around the bulb. The ± 23 control points set the width of the bulb.
    FruitKind.PEAR -> path {
        moveTo(MID, 16.0)
        cubicTo(MID + 8, 16.0, MID + 8, 27.0, MID + 12, 33.0)
        cubicTo(MID + 23, 43.0, MID + 18, 58.0, MID, 58.0)
        cubicTo(MID - 18, 58.0, MID - 23, 43.0, MID - 12, 33.0)
        cubicTo(MID - 8, 27.0, MID - 8, 16.0, MID, 16.0)
        closePath()
    }
    // A wide lens from x = 7 to 57 on y = 38, with a small round nub (3.5 px) at each tip.
    FruitKind.LEMON -> {
        val body = path {
            moveTo(7.0, 38.0)
            cubicTo(12.0, 18.0, 52.0, 18.0, 57.0, 38.0)
            cubicTo(52.0, 56.0, 12.0, 56.0, 7.0, 38.0)
            closePath()
        }
        union(body, circle(8.0, 38.0, 3.5), circle(56.0, 38.0, 3.5))
    }
    // A half disc, flat side up.
    // The circle is centred on (32, 24) with a 27 px radius; the arc from 0 to 180 degrees is its lower half, because
    // y is down. The flat top at y = 24 is the cut flesh.
    FruitKind.WATERMELON -> path {
        moveTo(5.0, 24.0)
        lineTo(59.0, 24.0)
        arcTo(Rect.makeLTRB(5f, -3f, 59f, 51f), 0f, 180f, false)
        closePath()
    }
    // Two round lobes with a dip at the top (MID, 20), where the stem goes, and a smaller dip at the bottom (MID, 55).
    // Each side has two curves: the dip to the widest point (MID ± 22, 36), then down to the bottom dip.
    FruitKind.APPLE -> path {
        moveTo(MID, 20.0)
        cubicTo(MID + 10, 12.0, MID + 24, 18.0, MID + 22, 36.0)
        cubicTo(MID + 20, 52.0, MID + 8, 58.0, MID, 55.0)
        cubicTo(MID - 8, 58.0, MID - 20, 52.0, MID - 22, 36.0)
        cubicTo(MID - 24, 18.0, MID - 10, 12.0, MID, 20.0)
        closePath()
    }
}

// Seeds and pips are placed at random, with the seed of the Python generator.
// The details inside the fruit body, drawn in the clip of the body: they cannot overflow the outline.
private fun Canvas.fruitInside(p: Palette, kind: FruitKind, body: Path) {
    val bounds = body.computeTightBounds()
    val rng = PythonRandom(7)
    if (kind == FruitKind.PRICKLY || kind == FruitKind.STRAWBERRY) {
        // Nine seeds in the middle 60% of the width and the lower 60% of the height, away from the leaves and the
        // light spot.
        repeat(9) {
            val x = bounds.left + bounds.width.toDouble() * rng.uniform(0.2, 0.8)
            val y = bounds.top + bounds.height.toDouble() * rng.uniform(0.3, 0.9)
            if (kind == FruitKind.PRICKLY) {
                // A dark dot with a light seed 0.6 px higher: the dark crescent under the seed makes it look sunk in.
                drawPath(circle(x, y, 2.2), fill(col(p.fruitDark)))
                drawPath(circle(x, y - 0.6, 1.4), fill(col(p.seed)))
            } else {
                // A small upright oval seed, 2.4 by 4 px.
                drawPath(oval(x - 1.2, y - 2, x + 1.2, y + 2), fill(col(p.seed)))
            }
        }
    }
    if (kind == FruitKind.ORANGE) {
        // The pores of the peel: 14 tiny dark dots spread over the disc.
        repeat(14) {
            val x = rng.uniform(16.0, 48.0)
            val y = rng.uniform(22.0, 54.0)
            drawPath(circle(x, y, 0.9), fill(col(p.fruitDark)))
        }
    }
    if (kind == FruitKind.WATERMELON) {
        // The rind along the round edge only. The 13 px green stroke is centred on the edge, so 6.5 px of it is inside
        // the clip; the 4 px dark stroke on the same edge leaves a 2 px dark green line at the very edge.
        val rind = path { addArc(Rect.makeLTRB(5f, -3f, 59f, 51f), 0f, 180f) }
        drawPath(rind, stroke(col(p.rind), 13.0))
        drawPath(rind, stroke(col(p.rindDark), 4.0))
        // Five black pips, 3.2 by 5.2 px, in two rows: three at y = 31 to 35, two at y = 40.
        for ((x, y) in listOf(20 to 31, 32 to 35, 44 to 31, 26 to 40, 38 to 40)) {
            drawPath(oval(x - 1.6, y - 2.6, x + 1.6, y + 2.6), fill(col(p.pip)))
        }
    }
    if (kind == FruitKind.BANANA) {
        // The dark brown ends of the banana, on the two tips of the crescent.
        drawPath(circle(10.5, 25.0, 3.2), fill(col(p.stem)))
        drawPath(circle(55.5, 27.5, 3.2), fill(col(p.stem)))
    }
}

// What grows out of the top of the fruit: leaves, stems. Drawn after the body, outside its clip, so it covers the
// outline of the body.
private fun Canvas.fruitTop(p: Palette, kind: FruitKind) {
    when (kind) {
        // Three leaves fanned upward, 30 degrees apart.
        FruitKind.PRICKLY -> crown(p, MID, 15.0, listOf(-120.0, -90.0, -60.0), 11.0, 4.5)
        // Five leaves fanned from almost left (-160) to almost right (-20): the green calyx of the strawberry.
        FruitKind.STRAWBERRY -> crown(p, MID, 16.0, listOf(-160.0, -125.0, -90.0, -55.0, -20.0), 12.0, 4.5)
        FruitKind.CHERRY -> {
            // Two stems from the top of each cherry that curve inward and meet at (34, 10), with a leaf at the joint.
            val stems = path {
                moveTo(21.0, 35.0)
                quadTo(24.0, 18.0, 34.0, 10.0)
                moveTo(43.0, 33.0)
                quadTo(40.0, 18.0, 34.0, 10.0)
            }
            drawPath(stems, stroke(col(p.outline), 6.0))
            drawPath(stems, stroke(col(p.stem), 2.5))
            outlined(leafPath(34.0, 10.0, 13.0, 5.0, -20.0), p.leaf, p, width = 2.5)
        }
        FruitKind.GRAPES -> stemAndLeaf(p, MID, 14.0, leafAngle = -30.0, leafLength = 13.0)
        // A leaf only, without a stem.
        FruitKind.ORANGE -> outlined(leafPath(MID, 18.0, 13.0, 5.0, -35.0), p.leaf, p, width = 2.5)
        FruitKind.PEAR -> stemAndLeaf(p, MID, 13.0, leafAngle = -20.0, leafLength = 13.0)
        FruitKind.LEMON -> outlined(leafPath(MID + 2, 22.0, 12.0, 4.5, -30.0), p.leaf, p, width = 2.5)
        FruitKind.APPLE -> stemAndLeaf(p, MID, 13.0)
        FruitKind.BANANA, FruitKind.WATERMELON -> Unit
    }
}

// squash flattens the fruit on its base, for the bounce frame.
// A fruit in a 64 px cell: an oval shadow on the ground, the outlined body shaded dark at the bottom right, a light
// spot at the top left, the details inside, then the leaves and stems. The light comes from the top left.
// In the base layer only the fruit and fruitDark colors remain (as grays that the game tints); the outline (unless
// tintOutline), the shadow, the light spot, the seeds and the leaves are CLEAR there and go to the detail layer (see
// Palette.layer).
fun fruitSprite(p: Palette, kind: FruitKind, squash: Double = 0.0, shadow: Boolean = true) = Raster(CELL, CELL).apply {
    val c = canvas
    if (shadow) {
        // A flat oval on the ground under the fruit, 40 by 10 px at 25% alpha. It is drawn before the squash
        // transform: it gets 20 px wider per unit of squash but keeps its height.
        c.drawPath(oval(MID - 20 - squash * 20, 53.0, MID + 20 + squash * 20, 63.0), fill(col(p.fruitShadow ?: p.shadeColor(), 0.25)))
    }
    // The squash scales around (MID, 58), the bottom of the fruit: the fruit gets wider and lower, its base stays on
    // the ground. The bounce frame uses squash = 0.07: 7% wider and 7% lower.
    c.save()
    c.translate(MID, 58.0)
    c.scale(1 + squash, 1 - squash)
    c.translate(-MID, -58.0)
    val body = fruitBody(kind)
    val bounds = body.computeTightBounds()
    // The outline: an 8 px stroke centred on the edge. The fills below cover its inner half, 4 px stays outside.
    c.drawPath(body, stroke(col(p.outline), OUTLINE * 2))
    c.clipped(body) {
        // The shading: the whole body in fruitDark, then the body again in the fruit color, moved 4 px left and 5 px
        // up. A dark crescent remains along the bottom right edge. A larger offset gives a thicker shadow crescent.
        drawPath(body, fill(col(p.fruitDark)))
        drawPath(body.translated(-4.0, -5.0), fill(col(p.fruit)))
        val left = bounds.left.toDouble()
        val top = bounds.top.toDouble()
        val w = bounds.width.toDouble()
        val h = bounds.height.toDouble()
        // The light spot: an oval in the top left of the body, from 16% to 36% of its width and 14% to 30% of its
        // height, so that it follows the size of each fruit.
        drawPath(oval(left + w * 0.16, top + h * 0.14, left + w * 0.36, top + h * 0.3), fill(col(p.fruitLight)))
        fruitInside(p, kind, body)
    }
    // A thin line on the edge of the watermelon, over the rind: it separates the rind from the outline and marks the
    // flat cut at the top.
    if (kind == FruitKind.WATERMELON) c.drawPath(body, stroke(col(p.outline), 2.5))
    c.fruitTop(p, kind)
    c.restore()
}

// Each fruit kind with its color: the fruitColors of the style in turn, or the natural color of the kind.
fun Palette.fruitEntries(): List<Pair<FruitKind, String>> {
    val colors = fruitColors ?: return FruitKind.entries.map { it to it.naturalColor }
    return FruitKind.entries.mapIndexed { i, kind -> kind to colors[i % colors.size] }
}
