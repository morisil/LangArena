package util

/**
 * Growable byte buffer, a minimal common-Kotlin replacement for `java.io.ByteArrayOutputStream`.
 */
class ByteArrayBuilder(
    initialCapacity: Int = 32,
) {
    private var buffer = ByteArray(maxOf(initialCapacity, 1))

    var size = 0
        private set

    private fun ensureCapacity(extra: Int) {
        val required = size + extra
        if (required > buffer.size) {
            buffer = buffer.copyOf(maxOf(buffer.size * 2, required))
        }
    }

    /** Writes the low 8 bits of [byte], like `OutputStream.write(int)`. */
    fun write(byte: Int) {
        ensureCapacity(1)
        buffer[size++] = byte.toByte()
    }

    fun write(bytes: ByteArray) {
        ensureCapacity(bytes.size)
        bytes.copyInto(buffer, size)
        size += bytes.size
    }

    /** Writes each char of [s] as one byte (ISO-8859-1), for strings with chars in 0..255. */
    fun writeLatin1(s: String) {
        ensureCapacity(s.length)
        for (c in s) buffer[size++] = c.code.toByte()
    }

    fun toByteArray(): ByteArray = buffer.copyOf(size)
}
