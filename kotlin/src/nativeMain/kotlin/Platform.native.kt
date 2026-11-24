@file:OptIn(ExperimentalForeignApi::class, NativeRuntimeApi::class)

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.convert
import kotlinx.cinterop.toKString
import kotlin.native.runtime.GC
import kotlin.native.runtime.NativeRuntimeApi

actual fun getEnv(name: String): String? = platform.posix.getenv(name)?.toKString()

actual fun printErr(message: String) {
    platform.posix.fputs(message + "\n", platform.posix.stderr)
}

actual fun collectGarbage() = GC.collect()

actual fun sleepMillis(millis: Long) {
    platform.posix.usleep((millis * 1000).convert())
}

actual fun exit(status: Int): Nothing = kotlin.system.exitProcess(status)
