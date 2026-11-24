package benchmarks

import Benchmark
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.listSerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeCollection
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import util.toFixed

/** The `[1, true]` tuple of the "opts" field, serialized as a JSON array like a Rust `(i32, bool)`. */
@Serializable(with = JsonOptSerializer::class)
class JsonOpt(
    val number: Int,
    val flag: Boolean,
)

object JsonOptSerializer : KSerializer<JsonOpt> {
    @OptIn(ExperimentalSerializationApi::class)
    override val descriptor: SerialDescriptor = listSerialDescriptor(JsonPrimitive.serializer().descriptor)

    override fun serialize(
        encoder: Encoder,
        value: JsonOpt,
    ) = encoder.encodeCollection(descriptor, 2) {
        encodeIntElement(descriptor, 0, value.number)
        encodeBooleanElement(descriptor, 1, value.flag)
    }

    override fun deserialize(decoder: Decoder): JsonOpt =
        decoder.decodeStructure(descriptor) {
            var number = 0
            var flag = false
            while (true) {
                when (val index = decodeElementIndex(descriptor)) {
                    0 -> number = decodeIntElement(descriptor, 0)
                    1 -> flag = decodeBooleanElement(descriptor, 1)
                    CompositeDecoder.DECODE_DONE -> break
                    else -> throw IllegalArgumentException("Unexpected element index: $index")
                }
            }
            JsonOpt(number, flag)
        }
}

@Serializable
class JsonCoordinate(
    val x: Double,
    val y: Double,
    val z: Double,
    val name: String,
    val opts: Map<String, JsonOpt>,
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
                    opts = mapOf("1" to JsonOpt(1, true)),
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
