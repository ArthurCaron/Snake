package fr.mwet.tools.stylegenerator

import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ColorType
import org.jetbrains.skia.Image
import org.jetbrains.skia.ImageInfo
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.File
import javax.imageio.ImageIO

// An RGBA image with straight (not premultiplied) alpha, one byte per channel, row by row.
// Raster is where shapes are drawn with Skia; Pixels is where the generator works on the pixels themselves: the
// post-effects, the Pillow operations, the nine-patch borders and the PNG files.
class Pixels(val width: Int, val height: Int, val rgba: ByteArray = ByteArray(width * height * 4)) {
    // channel: 0 red, 1 green, 2 blue, 3 alpha. Values are 0 to 255 (the byte is read unsigned).
    operator fun get(x: Int, y: Int, channel: Int) = rgba[(y * width + x) * 4 + channel].toInt() and 0xff

    operator fun set(x: Int, y: Int, channel: Int, value: Int) {
        rgba[(y * width + x) * 4 + channel] = value.toByte()
    }

    fun copy() = Pixels(width, height, rgba.copyOf())

    fun toImage(): Image =
        Image.makeRaster(ImageInfo(width, height, ColorType.RGBA_8888, ColorAlphaType.UNPREMUL), rgba, width * 4)

    // Pixels outside this image come out transparent, as in Pillow.
    fun crop(left: Int, top: Int, right: Int, bottom: Int): Pixels {
        val out = Pixels(right - left, bottom - top)
        for (y in 0 until out.height) {
            val sy = y + top
            if (sy !in 0 until height) continue
            for (x in 0 until out.width) {
                val sx = x + left
                if (sx !in 0 until width) continue
                System.arraycopy(rgba, (sy * width + sx) * 4, out.rgba, (y * out.width + x) * 4, 4)
            }
        }
        return out
    }

    // Replaces the pixels under src, alpha included.
    fun paste(src: Pixels, left: Int, top: Int) {
        for (y in 0 until src.height) {
            val dy = y + top
            if (dy !in 0 until height) continue
            for (x in 0 until src.width) {
                val dx = x + left
                if (dx !in 0 until width) continue
                System.arraycopy(src.rgba, (y * src.width + x) * 4, rgba, (dy * width + dx) * 4, 4)
            }
        }
    }

    // Draws src over this image at (left, top).
    fun composite(src: Pixels, left: Int = 0, top: Int = 0) {
        val under = crop(left, top, left + src.width, top + src.height)
        paste(alphaComposite(under, src), left, top)
    }

    // withAlpha = false writes an RGB file without the alpha channel.
    fun write(file: File, withAlpha: Boolean = true) {
        file.parentFile.mkdirs()
        file.writeBytes(png(withAlpha))
    }

    // The PNG encoding of ImageIO. The bytes are stable from run to run, so two outputs compare with diff.
    fun png(withAlpha: Boolean = true): ByteArray {
        val image = BufferedImage(width, height, if (withAlpha) BufferedImage.TYPE_INT_ARGB else BufferedImage.TYPE_INT_RGB)
        val argb = IntArray(width * height) { i ->
            val o = i * 4
            ((rgba[o + 3].toInt() and 0xff) shl 24) or ((rgba[o].toInt() and 0xff) shl 16) or
                ((rgba[o + 1].toInt() and 0xff) shl 8) or (rgba[o + 2].toInt() and 0xff)
        }
        image.setRGB(0, 0, width, height, argb, 0, width)
        return ByteArrayOutputStream().also { ImageIO.write(image, "png", it) }.toByteArray()
    }

    companion object {
        // An image of a single color.
        fun filled(width: Int, height: Int, r: Int, g: Int, b: Int, a: Int) = Pixels(width, height).apply {
            for (i in 0 until width * height) {
                rgba[i * 4] = r.toByte()
                rgba[i * 4 + 1] = g.toByte()
                rgba[i * 4 + 2] = b.toByte()
                rgba[i * 4 + 3] = a.toByte()
            }
        }

        fun read(file: File): Pixels {
            val image = ImageIO.read(file)
            val argb = image.getRGB(0, 0, image.width, image.height, null, 0, image.width)
            val out = Pixels(image.width, image.height)
            for (i in argb.indices) {
                val v = argb[i]
                out.rgba[i * 4] = (v shr 16).toByte()
                out.rgba[i * 4 + 1] = (v shr 8).toByte()
                out.rgba[i * 4 + 2] = v.toByte()
                out.rgba[i * 4 + 3] = (v ushr 24).toByte()
            }
            return out
        }
    }
}
