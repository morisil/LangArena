package benchmarks

import Benchmark
import kotlin.io.encoding.Base64

class Base64Encode : Benchmark() {
    private val n = configInt("size")
    private lateinit var bytes: ByteArray
    private lateinit var str2: String
    private var resultVal: UInt = 0u

    override fun prepare() {
        val str = "a".repeat(n)
        bytes = str.encodeToByteArray()
        str2 = Base64.encode(bytes)
    }

    override fun run(iterationId: Int) {
        val encoded = Base64.encode(bytes)
        resultVal += encoded.length.toUInt()
    }

    override fun checksum(): UInt {
        val str = bytes.copyOfRange(0, 4).decodeToString()
        val message = "encode ${str.take(4)}... to ${str2.take(4)}...: $resultVal"
        return Helper.checksum(message)
    }

    override fun name(): String = "Base64::Encode"
}
