/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.mcp

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.IndexNotReadyException
import com.intellij.openapi.project.Project
import kotlinx.coroutines.currentCoroutineContext
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.CoroutineContext

internal object MagentoMcpToolsetSupport {
    /**
     * Resolves the active IDE project from MCP call context, validates it, and executes the tool body.
     */
    suspend fun withProjectAction(
        validateProject: Boolean = true,
        requireSmartMode: Boolean = true,
        query: (Project) -> String
    ): String {
        val project = resolveProject(currentCoroutineContext()) ?: return "MCP project context is unavailable."

        if (validateProject) {
            val validationMessage = validateProject(
                project,
                requireSmartMode = requireSmartMode
            )
            if (validationMessage != null) {
                return validationMessage
            }
        }

        return query(project)
    }

    /**
     * Executes a tool body inside a read action after project validation succeeds.
     */
    suspend fun withProjectReadAction(
        validateProject: Boolean = true,
        requireSmartMode: Boolean = true,
        query: (Project) -> String
    ): String = withProjectAction(validateProject, requireSmartMode) { project ->
        try {
            MagentoMcpReadActionSupport.retryOnCancellation {
                ApplicationManager.getApplication().runReadAction<String> {
                    query(project)
                }
            }
        } catch (throwable: Throwable) {
            when {
                throwable is IndexNotReadyException -> MagentoMcpSupport.INDEXES_NOT_READY_MESSAGE
                MagentoMcpReadActionSupport.isCancellation(throwable) ->
                    MagentoMcpReadActionSupport.cancellationMessage()
                else -> throw throwable
            }
        }
    }

    /**
     * Executes a tool body on the EDT after project validation succeeds.
     */
    suspend fun withProjectEdtAction(
        validateProject: Boolean = true,
        query: (Project) -> String
    ): String = withProjectAction(validateProject) { project ->
        val application = ApplicationManager.getApplication()
        if (application.isDispatchThread) {
            return@withProjectAction query(project)
        }

        val result = AtomicReference<String>()
        val failure = AtomicReference<Throwable>()
        application.invokeAndWait {
            try {
                result.set(query(project))
            } catch (throwable: Throwable) {
                failure.set(throwable)
            }
        }

        failure.get()?.let { throw it }
        result.get() ?: ""
    }

    private fun validateProject(project: Project, requireSmartMode: Boolean = true): String? =
        MagentoMcpSupport.validateProject(project, requireSmartMode)

    /**
     * Uses the MCP runtime helper when available without hard-linking to a specific helper binary signature.
     */
    private fun resolveProject(context: CoroutineContext): Project? {
        return try {
            val helperClass = Class.forName("com.intellij.mcpserver.McpCallInfoKt")
            val helperMethod = helperClass.getMethod(
                "getProjectOrNull",
                CoroutineContext::class.java
            )
            helperMethod.invoke(null, context) as? Project
        } catch (_: ReflectiveOperationException) {
            null
        }
    }
}
