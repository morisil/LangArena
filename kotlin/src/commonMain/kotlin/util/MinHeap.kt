package util

/**
 * Binary min-heap, a minimal common-Kotlin replacement for `java.util.PriorityQueue`
 * (natural ordering, `add` / `poll`).
 */
class MinHeap<T : Comparable<T>>(
    initialCapacity: Int = 16,
) {
    private var items = arrayOfNulls<Any?>(maxOf(initialCapacity, 1))

    var size = 0
        private set

    fun isEmpty(): Boolean = size == 0

    fun isNotEmpty(): Boolean = size != 0

    fun add(item: T) {
        if (size == items.size) items = items.copyOf(size * 2)
        var i = size++
        while (i > 0) {
            val parent = (i - 1) ushr 1
            val p = get(parent)
            if (item >= p) break
            items[i] = p
            i = parent
        }
        items[i] = item
    }

    /** Removes and returns the smallest item. */
    fun poll(): T {
        if (size == 0) throw NoSuchElementException("heap is empty")
        val result = get(0)
        val last = get(--size)
        items[size] = null
        if (size > 0) {
            var i = 0
            val half = size ushr 1
            while (i < half) {
                var child = 2 * i + 1
                var c = get(child)
                val right = child + 1
                if (right < size) {
                    val r = get(right)
                    if (r < c) {
                        child = right
                        c = r
                    }
                }
                if (last <= c) break
                items[i] = c
                i = child
            }
            items[i] = last
        }
        return result
    }

    @Suppress("UNCHECKED_CAST")
    private fun get(index: Int): T = items[index] as T
}
