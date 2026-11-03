package fr.mwet.tools.stylegenerator

import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.sqrt

// The image operations the assets went through in Pillow, ported with the same integer arithmetic, so the resized
// icons, the blurred halos and the composited previews keep the pixel values of the Python generator.
// Skia has its own resize, blur and blend, but their rounding differs: the images can change by a few levels, and the
// port could no longer be checked file by file against the Python output. This file follows Pillow's C code
// (libImaging/Resample.c, BoxBlur.c, AlphaComposite.c) and keeps most of its variable names.

// The resampling filters: support is the radius, in source pixels, that the filter reads around each output pixel
// (at scale 1). BOX averages, BICUBIC and LANCZOS keep edges sharper when an image is shrunk.
enum class Resample(val support: Double, val filter: (Double) -> Double) {
    BOX(0.5, { x -> if (x > -0.5 && x <= 0.5) 1.0 else 0.0 }),
    BICUBIC(2.0, ::bicubic),
    LANCZOS(3.0, { x -> if (-3.0 <= x && x < 3.0) sinc(x) * sinc(x / 3) else 0.0 }),
}

private fun sinc(x: Double): Double {
    if (x == 0.0) return 1.0
    val px = x * Math.PI
    return StrictMath.sin(px) / px
}

// The cubic convolution kernel of Pillow, with a = -0.5.
private fun bicubic(value: Double): Double {
    val a = -0.5
    val x = if (value < 0.0) -value else value
    if (x < 1.0) return ((a + 2.0) * x - (a + 3.0)) * x * x + 1
    if (x < 2.0) return (((x - 5) * x + 8) * x - 4) * a
    return 0.0
}

// Pillow resamples in fixed point: a weight of 1.0 is 1 shl 22. A sum of 8-bit values times such weights fits in
// a 32-bit Int with 2 bits to spare.
private const val PRECISION_BITS = 32 - 8 - 2

// For each output pixel: the first source pixel and the number of source pixels it reads (bounds), and their
// weights, size per output pixel.
private class Coefficients(val size: Int, val bounds: IntArray, val weights: IntArray)

// precompute_coeffs of Pillow. When the image shrinks, the filter is stretched by the scale, so each output pixel
// averages all the source pixels it covers.
private fun coefficients(inSize: Int, outSize: Int, resample: Resample): Coefficients {
    val scale = inSize.toDouble() / outSize
    val filterScale = if (scale < 1.0) 1.0 else scale
    val support = resample.support * filterScale
    val size = ceil(support).toInt() * 2 + 1
    val bounds = IntArray(outSize * 2)
    val weights = IntArray(outSize * size)
    val k = DoubleArray(size)
    for (xx in 0 until outSize) {
        val center = (xx + 0.5) * scale
        val ss = 1.0 / filterScale
        val xmin = (center - support + 0.5).toInt().coerceAtLeast(0)
        val xmax = (center + support + 0.5).toInt().coerceAtMost(inSize) - xmin
        var ww = 0.0
        k.fill(0.0)
        for (x in 0 until xmax) {
            k[x] = resample.filter((x + xmin - center + 0.5) * ss)
            ww += k[x]
        }
        for (x in 0 until xmax) {
            if (ww != 0.0) k[x] /= ww
        }
        // The weights go to fixed point, rounded half away from zero.
        for (x in 0 until size) {
            val w = k[x] * (1 shl PRECISION_BITS)
            weights[xx * size + x] = if (k[x] < 0) (-0.5 + w).toInt() else (0.5 + w).toInt()
        }
        bounds[xx * 2] = xmin
        bounds[xx * 2 + 1] = xmax
    }
    return Coefficients(size, bounds, weights)
}

// Back from fixed point to a byte.
private fun clip8(value: Int) = (value shr PRECISION_BITS).coerceIn(0, 255)

// Resamples the rows firstRow until firstRow + rows to outWidth. ss starts at one half, so that the shift rounds.
private fun horizontal(src: Pixels, outWidth: Int, firstRow: Int, rows: Int, c: Coefficients): Pixels {
    val out = Pixels(outWidth, rows)
    for (y in 0 until rows) {
        for (xx in 0 until outWidth) {
            val xmin = c.bounds[xx * 2]
            val xmax = c.bounds[xx * 2 + 1]
            for (channel in 0 until 4) {
                var ss = 1 shl (PRECISION_BITS - 1)
                for (x in 0 until xmax) ss += src[x + xmin, y + firstRow, channel] * c.weights[xx * c.size + x]
                out[xx, y, channel] = clip8(ss)
            }
        }
    }
    return out
}

// Resamples the columns to outHeight. rowShift is the first source row that src holds.
private fun vertical(src: Pixels, outHeight: Int, rowShift: Int, c: Coefficients): Pixels {
    val out = Pixels(src.width, outHeight)
    for (yy in 0 until outHeight) {
        val ymin = c.bounds[yy * 2] - rowShift
        val ymax = c.bounds[yy * 2 + 1]
        for (x in 0 until src.width) {
            for (channel in 0 until 4) {
                var ss = 1 shl (PRECISION_BITS - 1)
                for (y in 0 until ymax) ss += src[x, y + ymin, channel] * c.weights[yy * c.size + y]
                out[x, yy, channel] = clip8(ss)
            }
        }
    }
    return out
}

// a * b / 255, rounded, without a division: the MULDIV255 macro of Pillow.
private fun muldiv255(a: Int, b: Int): Int {
    val tmp = a * b + 128
    return ((tmp shr 8) + tmp) shr 8
}

private fun Pixels.premultiplied() = copy().apply {
    for (i in 0 until width * height) {
        val alpha = rgba[i * 4 + 3].toInt() and 0xff
        for (c in 0 until 3) rgba[i * 4 + c] = muldiv255(rgba[i * 4 + c].toInt() and 0xff, alpha).toByte()
    }
}

// The integer division truncates, as in Pillow.
private fun Pixels.unpremultiplied() = copy().apply {
    for (i in 0 until width * height) {
        val alpha = rgba[i * 4 + 3].toInt() and 0xff
        if (alpha == 255 || alpha == 0) continue
        for (c in 0 until 3) rgba[i * 4 + c] = (255 * (rgba[i * 4 + c].toInt() and 0xff) / alpha).coerceAtMost(255).toByte()
    }
}

// Image.resize: the RGBA image is premultiplied first, then resampled horizontally and vertically.
// Premultiplying keeps the color of transparent pixels from bleeding into the edges of the shapes.
fun Pixels.resized(outWidth: Int, outHeight: Int, resample: Resample): Pixels {
    if (outWidth == width && outHeight == height) return copy()
    val horizontal = coefficients(width, outWidth, resample)
    val vertical = coefficients(height, outHeight, resample)
    var image = premultiplied()
    var rowShift = 0
    if (outWidth != width) {
        // Only the source rows that the vertical pass reads.
        val first = vertical.bounds[0]
        val last = vertical.bounds[outHeight * 2 - 2] + vertical.bounds[outHeight * 2 - 1]
        image = horizontal(image, outWidth, first, last - first, horizontal)
        rowShift = first
    }
    if (outHeight != height) image = vertical(image, outHeight, rowShift, vertical)
    return image.unpremultiplied()
}

// Image.resize with NEAREST for a whole factor: each pixel becomes a block.
fun Pixels.enlarged(factor: Int): Pixels {
    val out = Pixels(width * factor, height * factor)
    for (y in 0 until out.height) {
        for (x in 0 until out.width) System.arraycopy(rgba, ((y / factor) * width + x / factor) * 4, out.rgba, (y * out.width + x) * 4, 4)
    }
    return out
}

// ImageFilter.GaussianBlur on one channel: three box blurs whose size gives the variance of the Gaussian.
// radius is the standard deviation of the Gaussian, in pixels, as in Pillow. The rows are blurred, then the image is
// transposed so that the same row blur does the columns.
fun gaussianBlur(channel: IntArray, width: Int, height: Int, radius: Float): IntArray {
    val boxRadius = boxRadius(radius, 3)
    var out = channel.copyOf()
    repeat(3) { out = boxBlurRows(out, width, height, boxRadius) }
    var transposed = transpose(out, width, height)
    repeat(3) { transposed = boxBlurRows(transposed, height, width, boxRadius) }
    return transpose(transposed, height, width)
}

// Pillow computes the box radius in single precision.
private fun boxRadius(radius: Float, passes: Int): Float {
    val sigma2 = radius * radius / passes
    val length = sqrt(12.0 * sigma2 + 1.0).toFloat()
    val l = floor((length - 1.0) / 2.0).toFloat()
    var a = (2 * l + 1) * (l * (l + 1) - 3 * sigma2)
    a /= 6 * (sigma2 - (l + 1) * (l + 1))
    return l + a
}

private fun transpose(values: IntArray, width: Int, height: Int) = IntArray(values.size) { i ->
    val x = i / height
    val y = i % height
    values[y * width + x]
}

// The box has a whole radius and a fractional weight on its two far pixels; values past the edges repeat the edge.
// Fixed point with 24 fraction bits: ww is the weight of each whole pixel, fw the weight of each far pixel. acc is a
// running sum, updated by one pixel in and one out at each step. The masks with 0xffffffff copy the 32-bit unsigned
// overflow of the C code.
private fun boxBlurRows(values: IntArray, width: Int, height: Int, floatRadius: Float): IntArray {
    val radius = floatRadius.toInt()
    val ww = ((1 shl 24).toFloat() / (floatRadius * 2 + 1)).toLong()
    val fw = ((1L shl 24) - (radius * 2 + 1) * ww) / 2
    val edgeA = minOf(radius + 1, width)
    val edgeB = maxOf(width - radius - 1, 0)
    val lastX = width - 1
    val out = IntArray(values.size)
    for (y in 0 until height) {
        val row = y * width
        fun at(x: Int) = values[row + x].toLong()
        var acc = at(0) * (radius + 1)
        for (x in 0 until edgeA - 1) acc += at(x)
        acc += at(lastX) * (radius - edgeA + 1)
        fun save(x: Int, subtract: Int, add: Int, left: Int, right: Int) {
            acc += at(add) - at(subtract)
            val bulk = (acc * ww + (at(left) + at(right)) * fw) and 0xffffffffL
            out[row + x] = (((bulk + (1L shl 23)) and 0xffffffffL) shr 24).toInt() and 0xff
        }
        // The left edge, the middle and the right edge of the row; the else branch is a row too short for the box.
        if (edgeA <= edgeB) {
            for (x in 0 until edgeA) save(x, 0, x + radius, 0, x + radius + 1)
            for (x in edgeA until edgeB) save(x, x - radius - 1, x + radius, x - radius - 1, x + radius + 1)
            for (x in edgeB..lastX) save(x, x - radius - 1, lastX, x - radius - 1, lastX)
        } else {
            for (x in 0 until edgeB) save(x, 0, x + radius, 0, x + radius + 1)
            for (x in edgeB until edgeA) save(x, 0, lastX, 0, lastX)
            for (x in edgeA..lastX) save(x, x - radius - 1, lastX, x - radius - 1, lastX)
        }
    }
    return out
}

// Image.alpha_composite: src over dst, in integers with 7 extra bits of precision.
// The usual "over" operator on straight alpha: out alpha = sa + da * (1 - sa), and each color is the mix of the two
// colors weighted by coef1 (src) and coef2 (dst).
fun alphaComposite(dst: Pixels, src: Pixels): Pixels {
    val out = Pixels(dst.width, dst.height)
    val precision = 7
    fun shiftForDiv255(a: Long) = ((a shr 8) + a) shr 8
    for (i in 0 until dst.width * dst.height) {
        val o = i * 4
        val sa = src.rgba[o + 3].toInt() and 0xff
        if (sa == 0) {
            System.arraycopy(dst.rgba, o, out.rgba, o, 4)
            continue
        }
        val da = dst.rgba[o + 3].toInt() and 0xff
        val blend = da.toLong() * (255 - sa)
        val outA255 = sa.toLong() * 255 + blend
        val coef1 = sa.toLong() * 255 * 255 * (1L shl precision) / outA255
        val coef2 = 255 * (1L shl precision) - coef1
        for (c in 0 until 3) {
            val tmp = (src.rgba[o + c].toInt() and 0xff) * coef1 + (dst.rgba[o + c].toInt() and 0xff) * coef2
            out.rgba[o + c] = (shiftForDiv255(tmp + (0x80L shl precision)) shr precision).toByte()
        }
        out.rgba[o + 3] = shiftForDiv255(outA255 + 0x80).toByte()
    }
    return out
}
