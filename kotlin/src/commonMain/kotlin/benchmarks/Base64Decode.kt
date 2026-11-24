package benchmarks

import Benchmark
import kotlin.io.encoding.Base64

class Base64Decode : Benchmark() {
    private val n = configInt("size")
    private lateinit var str2: String
    private lateinit var bytes: ByteArray
    private var resultVal: UInt = 0u

    override fun prepare() {
        val str = "a".repeat(n)
        str2 = Base64.encode(str.encodeToByteArray())
        bytes = Base64.decode(str2)
    }

    override fun run(iterationId: Int) {
        bytes = Base64.decode(str2)
        resultVal += bytes.size.toUInt()
    }

    override fun checksum(): UInt {
        val str3 = bytes.copyOfRange(0, 4).decodeToString()
        val message = "decode ${str2.take(4)}... to ${str3.take(4)}...: $resultVal"
        return Helper.checksum(message)
    }

    override fun name(): String = "Base64::Decode"
}
