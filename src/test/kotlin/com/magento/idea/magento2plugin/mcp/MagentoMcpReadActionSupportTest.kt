package com.magento.idea.magento2plugin.mcp

import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.progress.ProcessCanceledException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.util.concurrent.CancellationException

class MagentoMcpReadActionSupportTest {
    @Test
    fun testCancellationMessageDoesNotAssumeWriteActionCause() {
        val message = MagentoMcpReadActionSupport.cancellationMessage()

        assertEquals(
            "The request was cancelled before the IDE could read project state. Retry.",
            message
        )
        assertFalse(message.contains("pending write action"))
    }

    @Test
    fun testIsCancellationRecognizesIdeAndCoroutineCancellationTypes() {
        assertTrue(
            MagentoMcpReadActionSupport.isCancellation(ProcessCanceledException())
        )
        assertTrue(
            MagentoMcpReadActionSupport.isCancellation(CancellationException("cancelled"))
        )
        assertTrue(
            MagentoMcpReadActionSupport.isCancellation(ReadAction.CannotReadException())
        )
        assertFalse(
            MagentoMcpReadActionSupport.isCancellation(IllegalStateException("not cancelled"))
        )
    }

    @Test
    fun testRetryOnCancellationRetriesTransientCancellationUntilSuccess() {
        var attempts = 0

        val result = MagentoMcpReadActionSupport.retryOnCancellation {
            attempts += 1
            if (attempts < 3) {
                throw ProcessCanceledException()
            }
            "ok"
        }

        assertEquals("ok", result)
        assertEquals(3, attempts)
    }

    @Test
    fun testRetryOnCancellationStopsAfterMaxAttempts() {
        var attempts = 0

        try {
            MagentoMcpReadActionSupport.retryOnCancellation {
                attempts += 1
                throw ProcessCanceledException()
            }
            fail("Expected cancellation to be rethrown after retries are exhausted.")
        } catch (_: ProcessCanceledException) {
            assertEquals(3, attempts)
        }
    }

    @Test
    fun testRetryOnCancellationDoesNotRetryNonCancellationFailures() {
        var attempts = 0

        try {
            MagentoMcpReadActionSupport.retryOnCancellation {
                attempts += 1
                throw IllegalStateException("boom")
            }
            fail("Expected non-cancellation failure to be rethrown immediately.")
        } catch (throwable: IllegalStateException) {
            assertEquals("boom", throwable.message)
            assertEquals(1, attempts)
        }
    }
}
