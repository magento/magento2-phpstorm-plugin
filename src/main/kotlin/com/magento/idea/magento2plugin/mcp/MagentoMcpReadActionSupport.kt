/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.mcp

internal object MagentoMcpReadActionSupport {
    private const val CANCELLATION_MESSAGE =
        "The request was cancelled before the IDE could read project state. Retry."

    fun cancellationMessage(): String = CANCELLATION_MESSAGE
}
