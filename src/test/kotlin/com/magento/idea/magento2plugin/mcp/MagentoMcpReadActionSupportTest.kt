package com.magento.idea.magento2plugin.mcp

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

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
}
