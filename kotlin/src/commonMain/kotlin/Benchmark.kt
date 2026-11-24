import util.toFixed
import kotlin.time.TimeSource

abstract class Benchmark {
    abstract fun run(iterationId: Int)

    abstract fun checksum(): UInt

    open fun prepare() {}

    abstract fun name(): String

    open fun warmupIterations(): Long =
        Helper.optConfigI64(name(), "warmup_iterations")
            ?: maxOf((iterations() * 0.2).toLong(), 1L)

    open fun warmup() {
        repeat(warmupIterations().toInt()) { run(it) }
    }

    open fun runAll() {
        repeat(iterations().toInt()) { run(it) }
    }

    open fun configVal(fieldName: String): Long = Helper.configI64(name(), fieldName)

    fun configInt(fieldName: String): Int = configVal(fieldName).toInt()

    fun configStr(fieldName: String): String = Helper.configS(name(), fieldName)

    open fun iterations(): Long = configVal("iterations")

    open fun expectedChecksum(): Long = configVal("checksum")

    companion object {
        private val benchmarkMap = mutableMapOf<String, () -> Benchmark>()

        fun registerBenchmark(
            name: String,
            factory: () -> Benchmark,
        ) {
            if (benchmarkMap.containsKey(name)) {
                printErr("Warning: Benchmark with name '$name' already registered. Skipping.")
                return
            }
            benchmarkMap[name] = factory
        }

        fun all(singleBench: String? = null) {
            var summaryTime = 0.0
            var ok = 0
            var fails = 0

            for (benchName in Helper.order) {
                if (singleBench != null && !benchName.contains(singleBench, ignoreCase = true)) {
                    continue
                }

                val factory = benchmarkMap[benchName]
                if (factory == null) {
                    println("Warning: Benchmark '$benchName' defined in config but not found in code")
                    continue
                }

                val bench = factory()

                Helper.reset()

                bench.prepare()
                bench.warmup()
                collectGarbage()

                Helper.reset()

                val startTime = TimeSource.Monotonic.markNow()
                bench.runAll()
                val timeDelta2 = startTime.elapsedNow().inWholeNanoseconds / 1_000_000_000.0

                collectGarbage()
                sleepMillis(1)
                collectGarbage()

                print("$benchName: ")
                if (bench.checksum().toLong() == bench.expectedChecksum()) {
                    print("OK ")
                    ok++
                } else {
                    print("ERR[actual=${bench.checksum()}, expected=${bench.expectedChecksum()}] ")
                    fails++
                }

                println("in ${timeDelta2.toFixed(3)}s")
                summaryTime += timeDelta2
            }

            println("Summary: ${summaryTime.toFixed(4)}s, ${ok + fails}, $ok, $fails")

            if (fails > 0) {
                exit(1)
            }
        }
    }
}
