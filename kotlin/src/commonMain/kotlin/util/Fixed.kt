package util

/**
 * Formats this double in fixed-point notation with exactly [decimals] digits after
 * the decimal point, without exponent, using `.` as decimal separator.
 *
 * The exact binary value of the double is rounded to [decimals] places, ties to even.
 * This is what C's `printf("%.Nf")`, Rust's `format!("{:.N}")` and Go's
 * `strconv.FormatFloat(v, 'f', N, 64)` produce. Pure Kotlin, so it gives identical
 * results on every platform (the JVM-only `String.format` is not available in common code).
 */
fun Double.toFixed(decimals: Int): String {
    require(decimals >= 0) { "decimals must be non-negative: $decimals" }
    if (isNaN()) return "NaN"
    if (isInfinite()) return if (this > 0) "Infinity" else "-Infinity"

    val bits = toRawBits()
    val negative = bits < 0
    val biasedExponent = ((bits ushr 52) and 0x7FF).toInt()
    val fraction = bits and 0xFFFFFFFFFFFFFL
    // value = mantissa * 2^exponent
    val mantissa = if (biasedExponent == 0) fraction else fraction or (1L shl 52)
    val exponent = if (biasedExponent == 0) -1074 else biasedExponent - 1075

    // Integer part as little-endian decimal digits.
    val intDigits: MutableList<Int>
    // Fractional part as fracNumerator / 2^k, with fracNumerator < 2^k.
    val k: Int
    val fracNumerator: Long
    if (exponent >= 0) {
        intDigits = integerDigits(mantissa, exponent)
        k = 0
        fracNumerator = 0
    } else {
        k = -exponent
        if (k < 64) {
            intDigits = integerDigits(mantissa ushr k, 0)
            fracNumerator = mantissa and ((1L shl k) - 1)
        } else {
            intDigits = mutableListOf(0)
            fracNumerator = mantissa
        }
    }

    val fracDigits = IntArray(decimals)
    var roundUp = false
    if (fracNumerator != 0L) {
        // r holds the remaining fraction scaled by 2^k, as 32-bit limbs (little-endian),
        // with headroom for the digit that multiplying by 10 shifts above bit k.
        val limbCount = (k + 4) / 32 + 1
        val r = LongArray(limbCount)
        r[0] = fracNumerator and 0xFFFFFFFFL
        if (limbCount > 1) r[1] = fracNumerator ushr 32
        val digitLimb = k / 32
        val digitShift = k % 32
        val lowMask = (1L shl digitShift) - 1
        for (i in 0 until decimals) {
            var carry = 0L
            for (j in 0 until limbCount) {
                val x = r[j] * 10 + carry
                r[j] = x and 0xFFFFFFFFL
                carry = x ushr 32
            }
            // digit = r >> k, then clear everything from bit k upwards
            var digit = r[digitLimb] ushr digitShift
            if (digitLimb + 1 < limbCount) digit = digit or (r[digitLimb + 1] shl (32 - digitShift))
            fracDigits[i] = digit.toInt()
            r[digitLimb] = r[digitLimb] and lowMask
            for (j in digitLimb + 1 until limbCount) r[j] = 0
        }
        // Compare the remainder r with half = 2^(k-1).
        val halfLimb = (k - 1) / 32
        val halfBit = 1L shl ((k - 1) % 32)
        if (r[halfLimb] and halfBit != 0L) {
            var restNonZero = r[halfLimb] and (halfBit - 1) != 0L
            for (j in 0 until halfLimb) if (r[j] != 0L) restNonZero = true
            roundUp =
                if (restNonZero) {
                    true
                } else {
                    val lastDigit = if (decimals > 0) fracDigits[decimals - 1] else intDigits[0]
                    lastDigit % 2 == 1
                }
        }
    }

    if (roundUp) {
        var i = decimals - 1
        var carry = true
        while (carry && i >= 0) {
            if (fracDigits[i] == 9) {
                fracDigits[i] = 0
            } else {
                fracDigits[i]++
                carry = false
            }
            i--
        }
        var j = 0
        while (carry) {
            if (j == intDigits.size) intDigits.add(0)
            if (intDigits[j] == 9) {
                intDigits[j] = 0
                j++
            } else {
                intDigits[j]++
                carry = false
            }
        }
    }

    val sb = StringBuilder(intDigits.size + decimals + 2)
    if (negative) sb.append('-')
    for (i in intDigits.indices.reversed()) sb.append('0' + intDigits[i])
    if (decimals > 0) {
        sb.append('.')
        for (d in fracDigits) sb.append('0' + d)
    }
    return sb.toString()
}

/** Little-endian decimal digits of `value * 2^shift`, for non-negative [value]. */
private fun integerDigits(
    value: Long,
    shift: Int,
): MutableList<Int> {
    val digits = mutableListOf<Int>()
    var v = value
    do {
        digits.add((v % 10).toInt())
        v /= 10
    } while (v != 0L)
    repeat(shift) {
        var carry = 0
        for (i in digits.indices) {
            val x = digits[i] * 2 + carry
            digits[i] = x % 10
            carry = x / 10
        }
        if (carry != 0) digits.add(carry)
    }
    return digits
}
