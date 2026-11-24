import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import util.toFixed

object Helper {
    private const val IM = 139968
    private const val IA = 3877
    private const val IC = 29573
    private const val INIT = 42

    private var last = INIT

    var order: List<String> = emptyList()
        private set

    var config = JsonObject(emptyMap())
        private set

    fun reset() {
        last = INIT
    }

    fun nextInt(max: Int): Int {
        last = (last * IA + IC) % IM
        return (last / IM.toDouble() * max).toInt()
    }

    fun nextInt(
        from: Int,
        to: Int,
    ): Int = nextInt(to - from + 1) + from

    fun nextFloat(max: Double = 1.0): Double {
        last = (last * IA + IC) % IM
        return max * last / IM.toDouble()
    }

    fun debug(message: () -> String) {
        if ("1" == getEnv("DEBUG")) {
            println(message())
        }
    }

    fun checksum(v: String): UInt {
        var hash = 5381L
        for (c in v) {
            hash = ((hash shl 5) + hash) + c.code
        }
        return hash.toUInt()
    }

    fun checksum(v: ByteArray): UInt {
        var hash = 5381L
        for (b in v) {
            hash = ((hash shl 5) + hash) + (b.toInt() and 0xFF)
        }
        return hash.toUInt()
    }

    fun checksumF64(v: Double): UInt = checksum(v.toFixed(7))

    fun loadConfig(filename: String? = null) {
        val text = SystemFileSystem.source(Path(filename ?: "../run.js")).buffered().use { it.readString() }
        val dict = LinkedHashMap<String, JsonObject>()
        val orderList = mutableListOf<String>()

        for (element in Json.parseToJsonElement(text).jsonArray) {
            val item = element.jsonObject
            val name = item.getValue("name").jsonPrimitive.content
            dict[name] = item
            orderList.add(name)
        }

        config = JsonObject(dict)
        order = orderList
    }

    private fun configSection(
        className: String,
        fieldName: String,
    ): JsonObject? = (config[className] as? JsonObject)?.takeIf { it.containsKey(fieldName) }

    fun optConfigI64(
        className: String,
        fieldName: String,
    ): Long? = configSection(className, fieldName)?.getValue(fieldName)?.jsonPrimitive?.long

    fun configI64(
        className: String,
        fieldName: String,
    ): Long {
        val section = configSection(className, fieldName)
        if (section == null) {
            printErr("Config not found for $className, field: $fieldName")
            return 0
        }
        return section.getValue(fieldName).jsonPrimitive.long
    }

    fun configS(
        className: String,
        fieldName: String,
    ): String {
        val section = configSection(className, fieldName)
        if (section == null) {
            printErr("Config not found for $className, field: $fieldName")
            return ""
        }
        return section.getValue(fieldName).jsonPrimitive.content
    }
}
