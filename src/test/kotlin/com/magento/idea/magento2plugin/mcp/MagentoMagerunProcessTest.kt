package com.magento.idea.magento2plugin.mcp

import org.junit.Assert.*
import org.junit.Test
import java.nio.file.Path
import java.util.concurrent.CancellationException

class MagentoMagerunProcessTest {
    @Test
    fun testArgumentsAreNotInterpretedAsShellSyntax() {
        val literal = "spaces ; \$(echo injected) & 'quotes'"
        val result = MagentoMagerunProcess().run(listOf("sh", "-c", "printf '%s' \"\$1\"", "probe", literal), Path.of(".")) {}
        assertEquals(0, result.exitCode)
        assertEquals(literal, result.stdout)
    }

    @Test
    fun testTimesOutAndTerminatesChildProcess() {
        val result = MagentoMagerunProcess(timeoutMillis = 150).run(listOf("sh", "-c", "sleep 30"), Path.of(".")) {}
        assertTrue(result.timedOut)
    }

    @Test
    fun testBoundsStdoutAndStderrWithoutDeadlocking() {
        val result = MagentoMagerunProcess(maxOutputBytes = 128).run(
            listOf("sh", "-c", "while :; do printf 'long output data'; printf 'long error data' >&2; done"), Path.of(".")) {}
        assertTrue(result.outputLimited)
        assertTrue(result.stdout.length <= 128)
        assertTrue(result.stderr.length <= 128)
    }

    @Test
    fun testClosesStdinForNoninteractiveProbes() {
        val result = MagentoMagerunProcess().run(listOf("sh", "-c", "cat; printf done"), Path.of(".")) {}
        assertEquals("done", result.stdout)
    }

    @Test
    fun testCancellationStopsRunningProbe() {
        var checks = 0
        try {
            MagentoMagerunProcess().run(listOf("sh", "-c", "sleep 30"), Path.of(".")) {
                if (++checks > 1) throw CancellationException("test cancellation")
            }
            fail("Expected cancellation")
        } catch (_: CancellationException) {
            assertTrue(checks > 1)
        }
    }
}
