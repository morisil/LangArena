// The few operations common Kotlin has no API for, implemented once per platform.

/** Value of the environment variable [name], or null if it is not set. */
expect fun getEnv(name: String): String?

/** Prints [message] followed by a newline to the standard error stream. */
expect fun printErr(message: String)

/** Hints the runtime to run a garbage collection, if it supports doing so. */
expect fun collectGarbage()

expect fun sleepMillis(millis: Long)

/** Terminates the process with [status]. */
expect fun exit(status: Int): Nothing
