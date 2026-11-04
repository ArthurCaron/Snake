package fr.mwet.tools.stylegenerator

import org.jetbrains.skia.BlendMode
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Color
import org.jetbrains.skia.ColorFilter
import org.jetbrains.skia.Font
import org.jetbrains.skia.Image
import org.jetbrains.skia.Paint
import org.jetbrains.skia.Rect
import java.io.File
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

// Mock-ups of the game screens drawn from the generated files, at half the size of a 900 px wide phone.
// The phone is 450 by 975 px, y down. The board is 16 by 20 cells across the full width, centred vertically.
// UNIT converts the sizes of the game UI, given for a 900 px wide screen, to the preview.
private const val PHONE_W = 450.0
private const val PHONE_H = 975
private const val UNIT = PHONE_W / 900
private const val COLUMNS = 16
private const val ROWS = 20
private const val CELL_PX = PHONE_W / COLUMNS
private const val BOARD_H = CELL_PX * ROWS
private const val BOARD_Y = (PHONE_H - BOARD_H) / 2
// The size of the light under each snake part and under the fruit, in cells.
private const val SNAKE_GLOW = 3.2
private const val FRUIT_GLOW = 4.5
// The space between the phones of a preview sheet, and the dark background of the sheets.
private const val GAP = 24
private val BACKGROUND = intArrayOf(30, 26, 28, 255)

// A step on the board, y up as in the game. angle is the rotation of a sprite drawn with its head side on the left
// (see Sprites.kt) so that its head side points in this direction: 0 for LEFT, 180 for RIGHT.
private enum class Direction(val angle: Double, val dx: Int, val dy: Int) {
    UP(270.0, 0, 1),
    RIGHT(180.0, 1, 0),
    DOWN(90.0, 0, -1),
    LEFT(0.0, -1, 0),
}

private typealias CellXY = Pair<Int, Int>

// Board cells, y up, head first.
private val DEMO_SNAKE = listOf(
    5 to 9, 6 to 9, 7 to 9, 8 to 9, 8 to 10, 8 to 11, 9 to 11, 10 to 11, 11 to 11, 11 to 10, 11 to 9,
    11 to 8, 11 to 7, 10 to 7, 9 to 7, 8 to 7, 7 to 7, 6 to 7, 5 to 7, 4 to 7, 4 to 6, 4 to 5,
)
private val GAME_SNAKE = listOf(
    9 to 13, 9 to 12, 9 to 11, 8 to 11, 7 to 11, 6 to 11, 5 to 11, 5 to 10, 5 to 9, 5 to 8, 6 to 8,
    7 to 8, 8 to 8, 9 to 8, 10 to 8, 11 to 8, 11 to 7, 11 to 6, 11 to 5, 10 to 5, 9 to 5,
)
private val MARK_SNAKE = listOf(9 to 10, 8 to 10, 7 to 10, 6 to 10, 6 to 9, 7 to 9, 8 to 9, 9 to 9, 10 to 9)

// In cells. The 40 px mark sprite is drawn at 40/64 of a cell. The offsets move the mark from the cell centre: back
// toward the neck for the head (to x = 39.5 in the head sprite, where its stripes start), toward the head for the
// tail (onto the end of the full width part, before the taper), and toward the inner corner for a turn: 9.37 = 32 -
// 32 / sqrt(2) puts the mark on the middle radius of the ring, at 45 degrees.
private const val MARK_SIZE = 40.0 / CELL
private const val TURN_MARK_OFFSET = 9.37 / CELL
private const val TAIL_MARK_OFFSET = 18.0 / CELL
private const val HEAD_MARK_OFFSET = 7.5 / CELL

// The step from the cell a to the neighbour cell b.
private fun direction(a: CellXY, b: CellXY) = when {
    b.first > a.first -> Direction.RIGHT
    b.first < a.first -> Direction.LEFT
    b.second > a.second -> Direction.UP
    else -> Direction.DOWN
}

// The rotation of the turn sprite, which joins the right and bottom edges at 0 degrees.
private fun turnAngle(first: Direction, second: Direction): Double {
    val pair = setOf(first, second)
    return when (pair) {
        setOf(Direction.RIGHT, Direction.DOWN) -> 0.0
        setOf(Direction.UP, Direction.RIGHT) -> 90.0
        setOf(Direction.LEFT, Direction.UP) -> 180.0
        else -> 270.0
    }
}

private class Segment(val cell: CellXY, var name: String, val angle: Double, val head: Direction, val tail: Direction? = null)

// The sprite and the rotation of each cell of the snake: the head, the tail, and a straight part or a turn between
// them. head is the step toward the head, tail the step toward the tail.
private fun segments(cells: List<CellXY>): List<Segment> = cells.mapIndexed { i, cell ->
    when (i) {
        0 -> direction(cells[1], cell).let { Segment(cell, "snakeHead", it.angle, it) }
        cells.lastIndex -> direction(cell, cells[i - 1]).let { Segment(cell, "snakeTail", it.angle, it) }
        else -> {
            val towardHead = direction(cell, cells[i - 1])
            val towardTail = direction(cell, cells[i + 1])
            if (towardHead.dx == -towardTail.dx && towardHead.dy == -towardTail.dy) {
                Segment(cell, "snakeBodyStraight", towardHead.angle, towardHead, towardTail)
            } else {
                Segment(cell, "snakeBodyTurn", turnAngle(towardHead, towardTail), towardHead, towardTail)
            }
        }
    }
}

private class MarkPlacement(val x: Double, val y: Double, val angle: Double, val scale: Double)

// Marks sit on the middle line of each part: behind the head, before the tail tip, and inside the bend of a turn.
private fun markPlacement(part: Segment): MarkPlacement {
    val x = part.cell.first + 0.5
    val y = part.cell.second + 0.5
    val hx = part.head.dx.toDouble()
    val hy = part.head.dy.toDouble()
    val headAngle = Math.toDegrees(atan2(hy, hx))
    if (part.name == "snakeHead") return MarkPlacement(x - hx * HEAD_MARK_OFFSET, y - hy * HEAD_MARK_OFFSET, headAngle, 1.0)
    if (part.name == "snakeTail") return MarkPlacement(x + hx * TAIL_MARK_OFFSET, y + hy * TAIL_MARK_OFFSET, headAngle, 0.85)
    val tail = part.tail!!
    val tx = tail.dx.toDouble()
    val ty = tail.dy.toDouble()
    val angle = Math.toDegrees(atan2(hy - ty, hx - tx))
    if (part.name == "snakeBodyTurn") return MarkPlacement(x + (hx + tx) * TURN_MARK_OFFSET, y + (hy + ty) * TURN_MARK_OFFSET, angle, 1.0)
    return MarkPlacement(x, y, angle, 1.0)
}

// Draws the image centred on (x, y) in board cells, y up, turned by angle (counter-clockwise on screen), size cells
// wide.
private fun Canvas.drawAt(image: Image, x: Double, y: Double, angle: Double, size: Double, paint: Paint?) {
    val half = size * CELL_PX / 2
    save()
    translate(x * CELL_PX, BOARD_Y + (ROWS - y) * CELL_PX)
    rotate(-angle)
    drawImageRect(image, rectF(-half, -half, half, half), LINEAR_MIPMAP, paint)
    restore()
}

// Replaces or multiplies the image colors by a fixed color, as the game does with the batch color.
private fun colorFilterPaint(hex: String, alpha: Double, mode: BlendMode) = Paint().apply {
    isAntiAlias = true
    val (r, g, b) = rgb(hex)
    colorFilter = ColorFilter.makeBlend(Color.makeARGB((alpha * 255).toInt(), r, g, b), mode)
}

// The game tints the gray layers by multiplying them by a color.
private fun multiplyPaint(hex: String) = colorFilterPaint(hex, 1.0, BlendMode.MODULATE)

// The image silhouette in a flat color at the given alpha: the shadow of the snake.
private fun tintPaint(hex: String, alpha: Double) = colorFilterPaint(hex, alpha, BlendMode.SRC_IN)

// The screen rectangle of a board cell: x to the right, y up from the bottom row.
private fun cellRect(x: Int, y: Int): Rect {
    val left = x * CELL_PX
    val top = BOARD_Y + (ROWS - 1 - y) * CELL_PX
    return rectF(left, top, left + CELL_PX, top + CELL_PX)
}

private fun Rect.centerX() = (left.toDouble() + right) * 0.5
private fun Rect.centerY() = (top.toDouble() + bottom) * 0.5

// Draws a one-cell sprite on a board cell, turned by angle; offset is in cells, y up.
private fun Canvas.drawSprite(image: Image, cell: CellXY, angle: Double = 0.0, offset: Pair<Double, Double> = 0.0 to 0.0, paint: Paint? = null) {
    val r = cellRect(cell.first, cell.second)
    save()
    translate(r.centerX() + offset.first * CELL_PX, r.centerY() - offset.second * CELL_PX)
    rotate(-angle)
    val half = CELL_PX / 2
    drawImageRect(image, rectF(-half, -half, half, half), LINEAR_MIPMAP, paint)
    restore()
}

// The generated sprites of a style, read back from the skin folder, and the three nine-patches without their 1 px
// border of marks.
private class Skin(folder: File) {
    val sprites: Map<String, Image> = folder.listFiles { f -> f.name.endsWith(".png") && !f.name.endsWith(".9.png") }.orEmpty()
        .associate { it.name.removeSuffix(".png") to Pixels.read(it).toImage() }
    val nines: Map<String, Image> = listOf("button", "buttonPrimary", "panel").associateWith { name ->
        val img = Pixels.read(folder.resolve("$name.9.png"))
        img.crop(1, 1, img.width - 1, img.height - 1).toImage()
    }

    operator fun get(name: String) = sprites[name] ?: error("Missing sprite $name")
}

// The board as the game draws it, from back to front: the ground, the grid, the frame shadow, the fruit (glow, tinted
// base, detail), the snake glows, the snake shadow, the tinted snake base, the tinted marks, the snake detail and the
// pupils.
private fun Canvas.drawBoard(
    p: Palette, skin: Skin, cells: List<CellXY>, fruitCell: CellXY?,
    dead: Boolean = false, snakeColor: String? = null, fruit: Pair<FruitKind, String>? = null,
) {
    val colors = kotlinPalette(p)
    val snake = snakeColor ?: colors.snake
    val (fruitKind, fruitColor) = fruit ?: colors.fruits[0]
    val fruitName = "fruit${fruitKind.title}"
    val tile = skin["ground"]
    // One ground tile covers 8 by 8 cells.
    tileFill(tile, 0.0, BOARD_Y, PHONE_W, BOARD_H, CELL_PX * 8 / tile.width)
    val grid = skin["gridCell"]
    val gridPaint = Paint().apply {
        isAntiAlias = true
        alpha = (0.10 * 255).toInt()
    }
    // The grid line on every cell, at 10% alpha.
    for (x in 0 until COLUMNS) for (y in 0 until ROWS) drawSprite(grid, x to y, paint = gridPaint)
    drawImageRect(skin["boardFrame"], rectF(0.0, BOARD_Y, PHONE_W, BOARD_Y + BOARD_H), LINEAR_MIPMAP)
    if (fruitCell != null) {
        drawLight(skin["glow"], fruitCell, FRUIT_GLOW, fruitColor, colors.fruitGlow)
        // The gray base multiplied by the fruit color, then the untinted detail on top.
        drawSprite(skin["${fruitName}_0"], fruitCell, paint = multiplyPaint(fruitColor))
        drawSprite(skin["${fruitName}Detail_0"], fruitCell)
    }
    val parts = segments(cells)
    if (dead) {
        parts[0].name = "snakeHeadDead"
    } else {
        for (part in parts) drawLight(skin["glow"], part.cell, SNAKE_GLOW, snake, colors.snakeGlow)
    }
    // The snake shadow: each part as a flat shade silhouette at 28% alpha, 0.07 cell right and 0.09 cell down.
    val shadow = tintPaint(p.shadeColor(), 0.28)
    for (part in parts) drawSprite(skin[part.name], part.cell, part.angle, offset = 0.07 to -0.09, paint = shadow)
    val body = multiplyPaint(snake)
    for (part in parts) drawSprite(skin[part.name], part.cell, part.angle, paint = body)
    if (!dead) {
        // The head has its own chevron mark; the other parts get the mark shape of the style.
        val marks = multiplyPaint(colors.marks)
        val shape = skin["mark${colors.markShape.title}"]
        for (part in parts) {
            val m = markPlacement(part)
            val image = if (part.name == "snakeHead") skin["snakeHeadMark"] else shape
            drawAt(image, m.x, m.y, m.angle, MARK_SIZE * m.scale, marks)
        }
    }
    for (part in parts) drawSprite(skin[part.name + "Detail"], part.cell, part.angle)
    if (!dead && fruitCell != null) drawPupils(skin["snakePupil"], parts[0].cell, parts[0].angle, fruitCell)
}

// An additive light under the snake or the fruit, as in the game.
private fun Canvas.drawLight(image: Image, cell: CellXY, size: Double, hex: String, alpha: Double) {
    if (alpha <= 0) return
    val paint = colorFilterPaint(hex, alpha, BlendMode.SRC_IN).apply { blendMode = BlendMode.PLUS }
    val rect = cellRect(cell.first, cell.second)
    val half = size * CELL_PX / 2
    drawImageRect(image, rectF(rect.centerX() - half, rect.centerY() - half, rect.centerX() + half, rect.centerY() + half), LINEAR_MIPMAP, paint)
}

// The pupils move toward the fruit, up to PUPIL_TRAVEL from the eye centre.
private fun Canvas.drawPupils(image: Image, head: CellXY, angle: Double, fruitCell: CellXY) {
    val cos = cos(Math.toRadians(angle))
    val sin = sin(Math.toRadians(angle))
    val centerX = head.first + 0.5
    val centerY = head.second + 0.5
    val forward = (EYE_FORWARD - MID) / CELL
    for (side in listOf(-1, 1)) {
        val lateral = side * EYE_SIDE / CELL
        val eyeX = centerX + forward * cos - lateral * sin
        val eyeY = centerY + forward * sin + lateral * cos
        val dx = fruitCell.first + 0.5 - eyeX
        val dy = fruitCell.second + 0.5 - eyeY
        val distance = hypot(dx, dy)
        val travel = PUPIL_TRAVEL / CELL
        val k = if (distance > travel) travel / distance else 1.0
        val x = eyeX + dx * k
        val y = eyeY + dy * k
        val half = PUPIL_SPRITE.toDouble() / CELL / 2 * CELL_PX
        val sx = x * CELL_PX
        val sy = BOARD_Y + (ROWS - y) * CELL_PX
        drawImageRect(image, rectF(sx - half, sy - half, sx + half, sy + half), LINEAR_MIPMAP)
    }
}

// Draws the nine-patch at UNIT scale: the corners keep their size, the edges and the centre stretch.
// The split is the nine-patch margin: 24 px for the 72 px buttons, 32 px for the 96 px panel.
private fun Canvas.nine(img: Image, rect: Rect) {
    val w = img.width.toDouble()
    val h = img.height.toDouble()
    val split = if (img.width < 90) 24.0 else 32.0
    val sx = listOf(0.0, split, w - split, w)
    val sy = listOf(0.0, split, h - split, h)
    val dx = listOf(rect.left.toDouble(), rect.left + split * UNIT, rect.right - split * UNIT, rect.right.toDouble())
    val dy = listOf(rect.top.toDouble(), rect.top + split * UNIT, rect.bottom - split * UNIT, rect.bottom.toDouble())
    for (i in 0 until 3) {
        for (j in 0 until 3) {
            val src = rectF(sx[i], sy[j], sx[i + 1], sy[j + 1])
            val dst = rectF(dx[i], dy[j], dx[i + 1], dy[j + 1])
            drawImageRect(img, src, dst, LINEAR_MIPMAP, null, true)
        }
    }
}

// A text button centred on (cx, cy): the nine-patch sized to the label, and the label on its face. size is the font
// size in game units.
private fun Canvas.button(
    p: Palette, skin: Skin, label: String, cx: Double, cy: Double,
    size: Double = 36.0, primary: Boolean = false, minWidth: Double = 0.0,
): Rect {
    val advance = textPath(label, size * UNIT).second
    // The primary button ("Jouer !") is much larger around its label than the others.
    val padX = (if (primary) 72 else 24) * UNIT
    val padY = (if (primary) 24 else 8) * UNIT
    val width = maxOf(advance + padX * 2, minWidth)
    // The extra 10 units are the bevel at the bottom of the button.
    val height = size * UNIT * 1.05 + padY * 2 + 10 * UNIT
    val r = rectF(cx - width / 2, cy - height / 2, cx + width / 2, cy + height / 2)
    nine(skin.nines.getValue(if (primary) "buttonPrimary" else "button"), r)
    // The baseline is 0.32 font size below the centre, which about centres the capitals; 4 units higher puts them on the
    // face, above the bevel.
    outlinedText(label, cx, cy + size * UNIT * 0.32 - 4 * UNIT, size * UNIT, p, align = Align.CENTER)
    return r
}

// A square button of size game units in the top right corner, with the icon inside it.
private fun Canvas.iconButton(skin: Skin, icon: Image, right: Double, top: Double, size: Double = 80.0) {
    val s = size * UNIT
    val r = rectF(right - s, top, right, top + s)
    nine(skin.nines.getValue("button"), r)
    // The icon inset 18% on the sides, a little higher than centred (0.8 and 1.2 of the inset) to sit on the face.
    val inset = s * 0.18
    drawImageRect(icon, rectF(r.left + inset, r.top + inset * 0.8, r.right - inset, r.bottom - inset * 1.2), LINEAR_MIPMAP)
}

// The clear color, then the floor tiles from two tiles above the board, multiplied by the surround color.
private fun phone(p: Palette, tile: Image) = Raster(PHONE_W.toInt(), PHONE_H).apply {
    val c = canvas
    c.clear(col(p.clear))
    // The tiles start two tiles above the board, so that they line up with the board ground.
    val tileSize = CELL_PX * 8
    val start = BOARD_Y - tileSize * 2
    c.saveLayer(null, null)
    c.tileFill(tile, 0.0, start, PHONE_W, PHONE_H - start, tileSize / tile.width)
    c.drawRect(rectF(0.0, 0.0, PHONE_W, PHONE_H.toDouble()), Paint().apply {
        color = col(kotlinPalette(p).surround)
        blendMode = BlendMode.MODULATE
    })
    c.restore()
}

// Darkens the whole phone with the shade color, under the menus.
private fun Canvas.dim(p: Palette, alpha: Double) = drawRect(rectF(0.0, 0.0, PHONE_W, PHONE_H.toDouble()), fill(col(p.shadeColor(), alpha)))

// The main menu: the demo snake on the board, dimmed, then the title, the buttons and the record.
private fun menuScreen(p: Palette, skin: Skin) = phone(p, skin["ground"]).apply {
    val c = canvas
    val colors = kotlinPalette(p)
    val fruits = colors.fruits
    val snake = if (colors.snakeTakesFruitColor) fruits[2 % fruits.size].second else null
    c.drawBoard(p, skin, DEMO_SNAKE, 13 to 12, snakeColor = snake, fruit = fruits[3 % fruits.size])
    c.dim(p, 0.35)
    c.outlinedText("Snek", PHONE_W / 2, 330.0, 130.0, p, align = Align.CENTER)
    c.button(p, skin, "Jouer !", PHONE_W / 2, 520.0, size = 60.0, primary = true)
    c.outlinedText("Record : 42", PHONE_W / 2, 625.0, 36 * UNIT, p, align = Align.CENTER)
    c.button(p, skin, "Meilleurs scores", PHONE_W * 0.3, 700.0)
    c.button(p, skin, "Paramètres", PHONE_W * 0.73, 700.0)
    c.iconButton(skin, skin["iconSoundOn"], PHONE_W - 12, 12.0)
    c.iconButton(skin, skin["iconMusicOn"], PHONE_W - 12, 12 + 92 * UNIT)
}

// A game: the snake looking at the fruit, the score and the record above the board, the pause button.
private fun gameScreen(p: Palette, skin: Skin) = phone(p, skin["ground"]).apply {
    val c = canvas
    c.drawBoard(p, skin, GAME_SNAKE, 3 to 14)
    c.outlinedText("Score : 21", 10.0, 62.0, 36 * UNIT, p)
    c.outlinedText("Record : 42", PHONE_W / 2, 62.0, 36 * UNIT, p, align = Align.CENTER)
    c.iconButton(skin, skin["iconPause"], PHONE_W - 10, 22.0)
}

// The game over screen: the dead snake, dimmed, under the result panel.
private fun panelScreen(p: Palette, skin: Skin) = phone(p, skin["ground"]).apply {
    val c = canvas
    val colors = kotlinPalette(p)
    val fruits = colors.fruits
    val snake = if (colors.snakeTakesFruitColor) fruits[3 % fruits.size].second else null
    c.drawBoard(p, skin, GAME_SNAKE, null, dead = true, snakeColor = snake)
    c.dim(p, 0.55)
    val r = rectF(PHONE_W * 0.14, 300.0, PHONE_W * 0.86, 690.0)
    c.nine(skin.nines.getValue("panel"), r)
    c.outlinedText("Perdu !", PHONE_W / 2, 380.0, 60 * UNIT, p, align = Align.CENTER)
    c.outlinedText("Score : 21", PHONE_W / 2, 430.0, 36 * UNIT, p, align = Align.CENTER)
    c.outlinedText("Record : 42", PHONE_W / 2, 470.0, 36 * UNIT, p, align = Align.CENTER)
    c.outlinedText("Nouveau record !", PHONE_W / 2, 510.0, 36 * UNIT, p, color = p.primaryLight, align = Align.CENTER)
    // The two buttons are as wide as the panel minus 64 units on each side.
    val width = r.width - 64 * UNIT * 2
    c.button(p, skin, "Rejouer", PHONE_W / 2, 570.0, minWidth = width)
    c.button(p, skin, "Menu principal", PHONE_W / 2, 635.0, minWidth = width)
}

// The menu, a game and the end screen side by side, under the style name.
fun renderPreview(p: Palette, out: File): Pixels {
    val skin = Skin(out.resolve("textures/game/skin"))
    val screens = listOf(menuScreen(p, skin), gameScreen(p, skin), panelScreen(p, skin))
    val phoneW = PHONE_W.toInt()
    val sheet = background(phoneW * 3 + GAP * 4, PHONE_H + GAP * 2 + 40)
    screens.forEachIndexed { i, screen -> sheet.paste(screen.pixels(), GAP + i * (phoneW + GAP), GAP + 40) }
    val label = Raster(sheet.width, 60)
    label.canvas.outlinedText(p.label, GAP.toDouble(), 46.0, 34.0, p)
    sheet.composite(label.pixels())
    return sheet
}

// A sheet filled with the dark background color.
private fun background(width: Int, height: Int) = Pixels.filled(width, height, BACKGROUND[0], BACKGROUND[1], BACKGROUND[2], BACKGROUND[3])

// The label and the middle screen of each style preview, four per row.
fun overview(folder: File, names: List<String>): Pixels {
    val columns = 4
    val sheets = names.map { folder.resolve("$it.png") }.filter { it.exists() }.map { Pixels.read(it) }
    val rows = (sheets.size + columns - 1) / columns
    val header = GAP + 40
    val phoneW = PHONE_W.toInt()
    val cellW = phoneW + GAP
    val cellH = PHONE_H + header + GAP
    val result = background(columns * cellW + GAP, rows * cellH + GAP)
    sheets.forEachIndexed { i, sheet ->
        val x = GAP + (i % columns) * cellW
        val y = GAP + (i / columns) * cellH
        result.paste(sheet.crop(GAP, 0, phoneW + GAP, header), x, y)
        val phoneLeft = GAP + phoneW + GAP
        result.paste(sheet.crop(phoneLeft, header, phoneLeft + phoneW, header + PHONE_H), x, y + header)
    }
    return result
}

// Every mark shape on a short snake, for each style.
fun marksSheet(stylesFolder: File, names: List<String>): Pixels {
    val cropTop = BOARD_Y + (ROWS - 12) * CELL_PX
    val cropBottom = BOARD_Y + (ROWS - 7) * CELL_PX
    val cropLeft = 4 * CELL_PX
    val cropRight = 13 * CELL_PX
    val width = (cropRight - cropLeft).toInt()
    val height = (cropBottom - cropTop).toInt()
    val gap = 16
    val shapes = MarkShape.entries
    val sheet = background(gap + shapes.size * (width + gap), names.size * (height + gap + 40) + gap)
    names.forEachIndexed { row, name ->
        val skin = Skin(stylesFolder.resolve("$name/textures/game/skin"))
        shapes.forEachIndexed { column, shape ->
            val p = PALETTES.getValue(name).copy(markShape = shape)
            val screen = phone(p, skin["ground"])
            screen.canvas.drawBoard(p, skin, MARK_SNAKE, 12 to 10)
            val x = gap + column * (width + gap)
            val y = gap + row * (height + gap + 40)
            val label = Raster(width, 40)
            label.canvas.outlinedText("$name - ${shape.id}", 4.0, 30.0, 20.0, PALETTES.getValue("desert"))
            sheet.composite(label.pixels(), x, y)
            sheet.paste(screen.pixels().crop(cropLeft.toInt(), cropTop.toInt(), cropRight.toInt(), cropBottom.toInt()), x, y + 40)
        }
    }
    return sheet
}

// The ten fruits of every style, tinted as in the game, on the style ground.
fun fruitsSheet(stylesFolder: File, names: List<String>): Pixels {
    val size = 96
    val gap = 12
    val labelWidth = 230
    val columns = kotlinPalette(PALETTES.getValue(names[0])).fruits.size
    val sheet = background(labelWidth + columns * (size + gap) + gap, names.size * (size + gap) + gap)
    names.forEachIndexed { row, name ->
        val p = PALETTES.getValue(name)
        val folder = stylesFolder.resolve("$name/textures/game/skin")
        val y = gap + row * (size + gap)
        val cell = Raster(columns * (size + gap) + labelWidth, size)
        val c = cell.canvas
        c.drawRect(rectF(labelWidth.toDouble(), 0.0, (labelWidth + columns * (size + gap)).toDouble(), size.toDouble()), fill(col(p.ground)))
        c.outlinedText(p.label.replace(" (other style)", ""), 8.0, size / 2.0 + 10, 26.0, PALETTES.getValue("desert"))
        kotlinPalette(p).fruits.forEachIndexed { column, (kind, color) ->
            val base = Pixels.read(folder.resolve("fruit${kind.title}_0.png")).toImage()
            val detail = Pixels.read(folder.resolve("fruit${kind.title}Detail_0.png")).toImage()
            val left = (labelWidth + column * (size + gap)).toDouble()
            val rect = rectF(left, 0.0, left + size, size.toDouble())
            c.drawImageRect(base, rect, LINEAR_MIPMAP, multiplyPaint(color))
            c.drawImageRect(detail, rect, LINEAR_MIPMAP)
        }
        sheet.composite(cell.pixels(), 0, y)
    }
    return sheet
}

// Every ground of every style, each shown as 2 by 2 tiles. Pillow drew the labels with its own default font.
fun groundsSheet(folder: File, names: List<String>, cell: Int = 128): Pixels {
    val files = listOf("ground") + GROUNDS.keys
    val labelH = 22
    val gap = 8
    val width = files.size * (cell + gap) + 120
    val height = labelH + names.size * (cell + gap)
    val sheet = Pixels.filled(width, height, 32, 32, 32, 255)
    val labels = Raster(width, height)
    val font = Font(TYPEFACE, 12f)
    val text = fill(Color.makeRGB(230, 230, 230))
    files.forEachIndexed { i, name ->
        val label = if (name == "ground") "checker" else name.removePrefix("ground").lowercase()
        labels.canvas.drawString(label, 120f + i * (cell + gap), 16f, font, text)
    }
    names.forEachIndexed { j, style ->
        val y = labelH + j * (cell + gap)
        labels.canvas.drawString(style, 6f, y + cell / 2f + 12, font, text)
        files.forEachIndexed { i, name ->
            val tile = Pixels.read(folder.resolve("$style/textures/game/skin/$name.png")).resized(cell / 2, cell / 2, Resample.LANCZOS)
            val x = 120 + i * (cell + gap)
            for (dx in listOf(0, cell / 2)) for (dy in listOf(0, cell / 2)) sheet.paste(tile, x + dx, y + dy)
        }
    }
    sheet.composite(labels.pixels())
    return sheet
}
