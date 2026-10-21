package com.magento.idea.magento2plugin.mcp

import com.intellij.openapi.application.PathManager
import org.junit.Assert.*
import org.junit.Test
import java.nio.file.Path
import java.util.concurrent.CancellationException

class MagentoMagerunProcessTest {
    @Test
    fun testArgumentsAreNotInterpretedAsShellSyntax() {
        for (literal in listOf("spaces ; \$(echo injected) & 'quotes'", "double \"quotes\" and trailing \\", "")) {
            val result = MagentoMagerunProcess().run(fixtureCommand("echo", literal), Path.of(".")) {}
            assertEquals(result.stderr, 0, result.exitCode)
            assertEquals(literal, result.stdout)
        }
    }

    @Test
    fun testTimesOutAndTerminatesChildProcess() {
        val result = MagentoMagerunProcess(timeoutMillis = 150).run(fixtureCommand("sleep"), Path.of(".")) {}
        assertTrue(result.timedOut)
    }

    @Test
    fun testBoundsStdoutAndStderrWithoutDeadlocking() {
        val result = MagentoMagerunProcess(maxOutputBytes = 128).run(
            fixtureCommand("output"), Path.of(".")) {}
        assertTrue(result.outputLimited)
        assertTrue(result.stdout.length <= 128)
        assertTrue(result.stderr.length <= 128)
    }

    @Test
    fun testClosesStdinForNoninteractiveProbes() {
        val result = MagentoMagerunProcess().run(fixtureCommand("stdin"), Path.of(".")) {}
        assertEquals("done", result.stdout)
    }

    @Test
    fun testCancellationStopsRunningProbe() {
        var checks = 0
        try {
            MagentoMagerunProcess().run(fixtureCommand("sleep"), Path.of(".")) {
                if (++checks > 1) throw CancellationException("test cancellation")
            }
            fail("Expected cancellation")
        } catch (_: CancellationException) {
            assertTrue(checks > 1)
        }
    }

    private fun fixtureCommand(mode: String, vararg arguments: String): List<String> {
        val executable = if (java.io.File.separatorChar == '\\') "java.exe" else "java"
        val java = Path.of(System.getProperty("java.home"), "bin", executable).toString()
        val fixture = MagerunProcessFixture::class.java
        val classpath = PathManager.getJarForClass(fixture).toString()
        return listOf(java, "-cp", classpath, fixture.name, mode) + arguments
    }
}
