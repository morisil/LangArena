package benchmarks

import Benchmark
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import util.toFixed

@Serializable
class JsonCoordinate(
    val x: Double,
    val y: Double,
    val z: Double,
    val name: String,
    val opts: Map<String, JsonArray>,
)

@Serializable
class JsonCoordinates(
    val coordinates: List<JsonCoordinate>,
    val info: String,
)

class JsonGenerate : Benchmark() {
    var n = configVal("coords")
    private lateinit var data: List<JsonCoordinate>
    lateinit var text: String
    private var resultVal: Long = 0

    override fun prepare() {
        data =
            List(n.toInt()) {
                JsonCoordinate(
                    x = Helper.nextFloat().toFixed(8).toDouble(),
                    y = Helper.nextFloat().toFixed(8).toDouble(),
                    z = Helper.nextFloat().toFixed(8).toDouble(),
                    name = "${Helper.nextFloat().toFixed(7)} ${Helper.nextInt(10000)}",
                    opts = mapOf("1" to JsonArray(listOf(JsonPrimitive(1), JsonPrimitive(true)))),
                )
            }
    }

    override fun run(iterationId: Int) {
        text = Json.encodeToString(JsonCoordinates(data, "some info"))
        if (text.startsWith("{\"coordinates\":")) resultVal += 1
    }

    override fun checksum(): UInt = resultVal.toUInt()

    override fun name(): String = "Json::Generate"
}
