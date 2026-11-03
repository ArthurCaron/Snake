package fr.mwet.tools.stylegenerator

// CPython's random.Random: a Mersenne Twister (MT19937) seeded through init_by_array, and 53-bit floats.
// The layouts drawn at random are the ones of the Python generator.
// The fruit seeds and dots, the grains of groundGrain and the tones of the groundTiles flagstones come from fixed
// seeds (7 and 3), so every run draws the same images. kotlin.random would give other positions: this copy of
// CPython's generator keeps the positions, and so the pixels, of the Python generator. Only random() and uniform() are
// ported, the two calls the generator makes. Kotlin Int arithmetic wraps like the uint32_t of the C code, and ushr is
// its unsigned shift.
class PythonRandom(seed: Int) {
    private val mt = IntArray(N)
    private var index = N

    // init_by_array of the reference MT19937 code; 19650218, 1664525 and 1566083941 are its constants.
    init {
        initGenrand(19650218)
        // The seed is a key of one 32-bit word.
        val key = intArrayOf(seed)
        var i = 1
        var j = 0
        repeat(maxOf(N, key.size)) {
            mt[i] = (mt[i] xor ((mt[i - 1] xor (mt[i - 1] ushr 30)) * 1664525)) + key[j] + j
            i++
            j++
            if (i >= N) {
                mt[0] = mt[N - 1]
                i = 1
            }
            if (j >= key.size) j = 0
        }
        repeat(N - 1) {
            mt[i] = (mt[i] xor ((mt[i - 1] xor (mt[i - 1] ushr 30)) * 1566083941)) - i
            i++
            if (i >= N) {
                mt[0] = mt[N - 1]
                i = 1
            }
        }
        // The reference code sets the top bit so that the state is never all zeros.
        mt[0] = UPPER_MASK
    }

    private fun initGenrand(s: Int) {
        mt[0] = s
        for (i in 1 until N) mt[i] = 1812433253 * (mt[i - 1] xor (mt[i - 1] ushr 30)) + i
    }

    // Regenerates the 624 words of state once they are all used.
    private fun twist() {
        for (k in 0 until N) {
            val y = (mt[k] and UPPER_MASK) or (mt[(k + 1) % N] and LOWER_MASK)
            mt[k] = mt[(k + M) % N] xor (y ushr 1) xor (if (y and 1 != 0) MATRIX_A else 0)
        }
        index = 0
    }

    // The next 32-bit output, tempered (the shifts and masks spread the bits of the state word).
    private fun next(): Int {
        if (index >= N) twist()
        var y = mt[index++]
        y = y xor (y ushr 11)
        y = y xor ((y shl 7) and 0x9d2c5680.toInt())
        y = y xor ((y shl 15) and 0xefc60000.toInt())
        return y xor (y ushr 18)
    }

    // A Double in [0, 1) from 53 random bits: 27 bits of a (a * 2^26) followed by 26 bits of b, divided by 2^53.
    fun random(): Double {
        val a = next() ushr 5
        val b = next() ushr 6
        return (a * 67108864.0 + b) * (1.0 / 9007199254740992.0)
    }

    // A Double between a and b, computed as CPython does.
    fun uniform(a: Double, b: Double) = a + (b - a) * random()

    // The MT19937 parameters: 624 words of state, the middle offset 397 and the twist matrix.
    private companion object {
        const val N = 624
        const val M = 397
        const val MATRIX_A = 0x9908b0df.toInt()
        const val UPPER_MASK = 0x80000000.toInt()
        const val LOWER_MASK = 0x7fffffff
    }
}
