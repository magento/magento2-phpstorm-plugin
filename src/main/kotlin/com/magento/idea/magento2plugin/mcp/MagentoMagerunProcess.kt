/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.mcp

import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.nio.file.Path
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

internal fun interface MagerunRunner {
    fun run(argv: List<String>, cwd: Path, checkCancelled: () -> Unit): MagerunProcessResult
}

internal data class MagerunProcessResult(
    val exitCode: Int,
    val stdout: String = "",
    val stderr: String = "",
    val timedOut: Boolean = false,
    val outputLimited: Boolean = false
)

/** Argument-vector execution, closed stdin, concurrent bounded output, and bounded process lifetime. */
internal class MagentoMagerunProcess(
    private val timeoutMillis: Long = 20_000,
    private val maxOutputBytes: Int = 4 * 1024 * 1024
) : MagerunRunner {
    override fun run(argv: List<String>, cwd: Path, checkCancelled: () -> Unit): MagerunProcessResult {
        checkCancelled()
        val process = ProcessBuilder(argv).directory(cwd.toFile()).apply {
            environment()["NO_COLOR"] = "1"
        }.start()
        val exceeded = AtomicBoolean(false)
        val stdout = Collector(process.inputStream, maxOutputBytes, exceeded)
        val stderr = Collector(process.errorStream, minOf(maxOutputBytes, 65_536), exceeded)
        val readers = listOf(stdout, stderr).map { collector ->
            thread(isDaemon = true, name = "magento-magerun-output") { collector.read() }
        }
        val deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(timeoutMillis)
        var timedOut = false
        try {
            process.outputStream.close()
            while (!process.waitFor(50, TimeUnit.MILLISECONDS)) {
                checkCancelled()
                if (exceeded.get() || System.nanoTime() >= deadline) {
                    timedOut = !exceeded.get()
                    break
                }
            }
            if (process.isAlive) terminate(process)
            readers.forEach { it.join(500) }
            return MagerunProcessResult(if (process.isAlive) -1 else process.exitValue(), stdout.text(), stderr.text(),
                timedOut, exceeded.get())
        } finally {
            if (process.isAlive) terminate(process)
            process.inputStream.close()
            process.errorStream.close()
        }
    }

    private fun terminate(process: Process) {
        process.descendants().use { descendants -> descendants.forEach { it.destroyForcibly() } }
        process.destroyForcibly()
        process.waitFor(500, TimeUnit.MILLISECONDS)
    }

    private class Collector(private val stream: InputStream, private val limit: Int, private val exceeded: AtomicBoolean) {
        private val buffer = ByteArrayOutputStream()
        fun read() {
            try {
                val chunk = ByteArray(8192)
                while (true) {
                    val count = stream.read(chunk)
                    if (count < 0) return
                    synchronized(buffer) {
                        val remaining = limit - buffer.size()
                        buffer.write(chunk, 0, minOf(count, remaining))
                        if (count > remaining) exceeded.set(true)
                    }
                    if (exceeded.get()) return
                }
            } catch (_: java.io.IOException) {
                // The owning process is being cancelled or terminated.
            }
        }
        fun text(): String = synchronized(buffer) { buffer.toString(Charsets.UTF_8) }
    }
}
