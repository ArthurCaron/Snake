package fr.mwet.tools.stylegenerator

import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

// A rounded button with the snake head looking up, drawn at 128 units and scaled to the size.
// The head is the one-layer snakeHead of Main.kt, in its real colors, with pupils drawn in.
// Main.kt resizes this 512 px image to every icon size of the desktop, Android and web launchers.
fun appIcon(p: Palette, head: Raster, size: Int = 512) = Raster(size, size).apply {
    val c = canvas
    val k = size / 128.0
    c.scale(k, k)
    // A square of 116 units with a 28 unit corner radius; the 10 unit stroke leaves a 5 unit outline outside it.
    val shape = rect(6.0, 6.0, 122.0, 122.0, 28.0)
    c.drawPath(shape, stroke(col(p.outline), 10.0))
    c.clipped(shape) {
        // The button look: the dark bevel shows in the bottom 12 units, a light strip near the top.
        drawPath(shape, fill(col(p.buttonDark)))
        drawPath(rect(6.0, 6.0, 122.0, 110.0, 26.0), fill(col(p.button)))
        drawPath(rect(18.0, 14.0, 110.0, 20.0, 3.0), fill(col(p.buttonLight, 0.9)))
    }
    c.save()
    // Read from the last line up: the 64 px sprite is centred on (0, 0), enlarged 1.7 times (109 units), turned a
    // quarter clockwise so that its front (x = 0) points up, and placed 2 units below the centre of the button.
    c.translate(64.0, 66.0)
    c.rotate(90.0)
    c.scale(1.7, 1.7)
    c.translate(-MID, -MID)
    // Nearest sampling, as in the Python generator.
    c.drawImage(head.image(), 0f, 0f)
    c.restore()
}

// Windows icon: one PNG entry per size, smallest first. 256 is written as 0 in the directory.
// The layout of the file that Pillow wrote: a 6-byte header, a 16-byte entry per size, then the PNG data.
fun writeIco(icon: Pixels, sizes: List<Int>, file: File) {
    val frames = sizes.sorted().distinct().filter { it <= minOf(icon.width, 256) }
        .map { it to icon.resized(it, it, Resample.LANCZOS).png() }
    val header = ByteBuffer.allocate(6 + 16 * frames.size).order(ByteOrder.LITTLE_ENDIAN)
    // Reserved 0, type 1 (icon), the number of images.
    header.putShort(0).putShort(1).putShort(frames.size.toShort())
    var offset = 6 + 16 * frames.size
    for ((size, png) in frames) {
        val side = (if (size < 256) size else 0).toByte()
        // Width, height, no color table, reserved, 0 planes, 32 bits per pixel, data size, data offset.
        header.put(side).put(side).put(0).put(0).putShort(0).putShort(32).putInt(png.size).putInt(offset)
        offset += png.size
    }
    val out = ByteArrayOutputStream()
    out.write(header.array())
    for ((_, png) in frames) out.write(png)
    file.parentFile.mkdirs()
    file.writeBytes(out.toByteArray())
}

// macOS icon: a table of contents, then one PNG chunk per type. Pillow resizes with BICUBIC here.
// Each type is an icon size in pixels: ic11 to ic14 are the Retina (2x) versions of 16, 32, 128 and 256. ic10 (1024)
// is enlarged from the 512 px icon.
fun writeIcns(icon: Pixels, file: File) {
    val types = listOf("ic07" to 128, "ic08" to 256, "ic09" to 512, "ic10" to 1024, "ic11" to 32, "ic12" to 64, "ic13" to 256, "ic14" to 512)
    val streams = types.map { it.second }.distinct().associateWith { icon.resized(it, it, Resample.BICUBIC).png() }
    // Every block starts with a 4-letter type and a 4-byte length that counts these 8 bytes.
    val headerSize = 8
    val tocSize = headerSize + types.size * headerSize
    val dataSize = types.sumOf { headerSize + streams.getValue(it.second).size }
    val buffer = ByteBuffer.allocate(headerSize + tocSize + dataSize).order(ByteOrder.BIG_ENDIAN)
    buffer.put("icns".toByteArray()).putInt(headerSize + tocSize + dataSize)
    buffer.put("TOC ".toByteArray()).putInt(tocSize)
    for ((type, size) in types) buffer.put(type.toByteArray()).putInt(headerSize + streams.getValue(size).size)
    for ((type, size) in types) {
        val stream = streams.getValue(size)
        buffer.put(type.toByteArray()).putInt(headerSize + stream.size).put(stream)
    }
    file.parentFile.mkdirs()
    file.writeBytes(buffer.array())
}
