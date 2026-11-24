package util

import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

/** Checks [toFixed] against the exact `BigDecimal` reference (round half even). */
class FixedJvmTest {
    private fun reference(v: Double, decimals: Int): String {
        val s = BigDecimal(v).setScale(decimals, RoundingMode.HALF_EVEN).toPlainString()
        // BigDecimal drops the sign of results that round to zero; C/Java printf keep it
        return if (v.toRawBits() < 0 && !s.startsWith("-")) "-$s" else s
    }

    private fun check(v: Double, decimals: Int) =
        assertEquals(reference(v, decimals), v.toFixed(decimals), "value=$v decimals=$decimals")

    @Test
    fun specialValues() {
        val values = listOf(
            0.0, -0.0, 0.5, 1.5, 2.5, -2.5, 0.125, 0.375, 0.15, 0.25, 9.5, 99.995, 0.9999999999,
            1e-300, Double.MIN_VALUE, -Double.MIN_VALUE, Double.MAX_VALUE, 1e22, 2.0.pow(63), 2.0.pow(53) + 2,
            123456789.123456789, 4.35, 1.0 / 3, 2.0 / 3, -1.0 / 3,
        )
        for (v in values) for (d in 0..20) check(v, d)
        assertEquals("NaN", Double.NaN.toFixed(3))
        assertEquals("Infinity", Double.POSITIVE_INFINITY.toFixed(3))
        assertEquals("-Infinity", Double.NEGATIVE_INFINITY.toFixed(3))
    }

    @Test
    fun randomBits() {
        val random = Random(42)
        repeat(200_000) {
            val v = Double.fromBits(random.nextLong())
            if (!v.isNaN() && !v.isInfinite() && kotlin.math.abs(v) < 1e30) check(v, random.nextInt(0, 16))
        }
    }

    @Test
    fun randomBenchmarkLikeValues() {
        val random = Random(7)
        repeat(500_000) {
            val v = (random.nextDouble() - 0.5) * 10.0.pow(random.nextInt(-8, 10))
            check(v, random.nextInt(0, 12))
        }
    }

    @Test
    fun exactTies() {
        // n / 2^m values are exact ties at m-1 decimals in binary, exercising half-even
        for (m in 1..12) for (n in -5000..5000) {
            val v = n / 2.0.pow(m)
            for (d in 0..m) check(v, d)
        }
    }

    private fun Double.pow(n: Int) = Math.pow(this, n.toDouble())
}
