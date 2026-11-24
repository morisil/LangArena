package util

/**
 * Minimal RFC 4180 CSV reader, a common-Kotlin replacement for opencsv's `CSVReader`:
 * comma-separated fields, optionally enclosed in double quotes; inside quotes, commas and
 * line breaks are literal and `""` is an escaped quote. Records end with `\n` or `\r\n`.
 */
class CsvReader(
    private val text: String,
) {
    private var pos = 0
    private val field = StringBuilder()

    /** Returns the fields of the next record, or null at the end of input. */
    fun readNext(): List<String>? {
        if (pos >= text.length) return null
        val fields = ArrayList<String>()
        var inQuotes = false
        while (pos < text.length) {
            val c = text[pos++]
            if (inQuotes) {
                if (c == '"') {
                    if (pos < text.length && text[pos] == '"') {
                        field.append('"')
                        pos++
                    } else {
                        inQuotes = false
                    }
                } else {
                    field.append(c)
                }
            } else {
                when (c) {
                    '"' -> inQuotes = true
                    ',' -> {
                        fields.add(field.toString())
                        field.setLength(0)
                    }
                    '\n' -> break
                    '\r' -> if (pos < text.length && text[pos] == '\n') {
                        pos++
                        break
                    } else {
                        field.append(c)
                    }
                    else -> field.append(c)
                }
            }
        }
        fields.add(field.toString())
        field.setLength(0)
        return fields
    }
}
