package benchmarks

import Benchmark
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class JsonParseDom : Benchmark() {
    private lateinit var text: String
    private var resultVal: UInt = 0u

    override fun prepare() {
        val generator = JsonGenerate()
        generator.n = configVal("coords")
        generator.prepare()
        generator.run(0)
        text = generator.text
    }

    private fun calc(text: String): Coord {
        val json = Json.parseToJsonElement(text).jsonObject
        val coordinates = json.getValue("coordinates").jsonArray

        var x = 0.0
        var y = 0.0
        var z = 0.0

        for (element in coordinates) {
            val coord = element.jsonObject
            x += coord.getValue("x").jsonPrimitive.double
            y += coord.getValue("y").jsonPrimitive.double
            z += coord.getValue("z").jsonPrimitive.double
        }

        val len = coordinates.size.toDouble()
        return Coord(x / len, y / len, z / len)
    }

    override fun run(iterationId: Int) {
        val coord = calc(text)
        resultVal += Helper.checksumF64(coord.x) + Helper.checksumF64(coord.y) + Helper.checksumF64(coord.z)
    }

    override fun checksum(): UInt = resultVal

    override fun name(): String = "Json::ParseDom"
}
