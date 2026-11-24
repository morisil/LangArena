actual fun getEnv(name: String): String? = System.getenv(name)

actual fun printErr(message: String) = System.err.println(message)

actual fun collectGarbage() = System.gc()

actual fun sleepMillis(millis: Long) = Thread.sleep(millis)

actual fun exit(status: Int): Nothing = kotlin.system.exitProcess(status)
