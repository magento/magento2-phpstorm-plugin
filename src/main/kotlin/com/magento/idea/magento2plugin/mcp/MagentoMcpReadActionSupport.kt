/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.mcp

import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.progress.ProcessCanceledException
import java.util.concurrent.CancellationException

internal object MagentoMcpReadActionSupport {
    private const val MAX_ATTEMPTS = 3
    private const val CANCELLATION_MESSAGE =
        "The request was cancelled before the IDE could read project state. Retry."

    fun cancellationMessage(): String = CANCELLATION_MESSAGE

    fun <T> retryOnCancellation(action: () -> T): T {
        var lastCancellation: Throwable? = null

        repeat(MAX_ATTEMPTS) { attempt ->
            try {
                return action()
            } catch (throwable: Throwable) {
                if (!isCancellation(throwable)) {
                    throw throwable
                }
                lastCancellation = throwable
                if (attempt == MAX_ATTEMPTS - 1) {
                    throw throwable
                }
            }
        }

        throw lastCancellation ?: IllegalStateException("Cancellation retry completed without result.")
    }

    fun isCancellation(throwable: Throwable): Boolean {
        return throwable is ReadAction.CannotReadException
            || throwable is ProcessCanceledException
            || throwable is CancellationException
    }
}
