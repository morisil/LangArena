package benchmarks

import Benchmark
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class Coord(
    val x: Double,
    val y: Double,
    val z: Double,
)

class JsonParseMapping : Benchmark() {
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
        val data = json.decodeFromString<CoordinatesData>(text)
        val coordinates = data.coordinates

        var x = 0.0
        var y = 0.0
        var z = 0.0

        for (coord in coordinates) {
            x += coord.x
            y += coord.y
            z += coord.z
        }

        val len = coordinates.size.toDouble()
        return Coord(x / len, y / len, z / len)
    }

    @Serializable
    class CoordinatesData(
        val coordinates: List<Coord>,
    )

    private val json = Json { ignoreUnknownKeys = true }

    override fun run(iterationId: Int) {
        val coord = calc(text)
        resultVal += Helper.checksumF64(coord.x) + Helper.checksumF64(coord.y) + Helper.checksumF64(coord.z)
    }

    override fun checksum(): UInt = resultVal

    override fun name(): String = "Json::ParseMapping"
}
