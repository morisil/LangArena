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

    fun toByteArray(): ByteArray = buffer.copyOf(size)
}
