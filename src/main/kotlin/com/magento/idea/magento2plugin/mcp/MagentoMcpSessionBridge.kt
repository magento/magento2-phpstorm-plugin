/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.mcp

import com.intellij.mcpserver.McpSessionInvocationMode
import com.intellij.mcpserver.McpToolFilter
import com.intellij.mcpserver.impl.McpServerService
import com.intellij.openapi.application.ApplicationManager
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

/**
 * Exposes an authorized MCP HTTP session for driver/UI tests.
 */
object MagentoMcpSessionBridge {
    private var sessionDeferred: CompletableDeferred<Unit>? = null
    private var sessionInfo: Triple<Int, String, String>? = null

    @Synchronized
    fun startSession(): String {
        sessionInfo?.let { (port, tokenName, token) ->
            return "$port:$tokenName:$token"
        }
        val deferred = CompletableDeferred<Unit>()
        sessionDeferred = deferred
        val endpoint = CompletableFuture<Triple<Int, String, String>>()
        ApplicationManager.getApplication().executeOnPooledThread {
            try {
                runBlocking {
                    McpServerService.getInstance().authorizedSession(
                        McpServerService.McpSessionOptions(
                            McpServerService.AskCommandExecutionMode.DONT_ASK,
                            McpToolFilter.MaskBased("-*,+com.magento.idea.magento2plugin.mcp.*"),
                            invocationMode = McpSessionInvocationMode.DIRECT,
                        )
                    ) { port, tokenName, token ->
                        val info = Triple(port, tokenName, token)
                        sessionInfo = info
                        endpoint.complete(info)
                        deferred.await()
                    }
                }
            } catch (t: Throwable) {
                endpoint.completeExceptionally(t)
            }
        }
        val (port, tokenName, token) = endpoint.get(30, TimeUnit.SECONDS)
        return "$port:$tokenName:$token"
    }

    @Synchronized
    fun stopSession() {
        sessionDeferred?.complete(Unit)
        sessionDeferred = null
        sessionInfo = null
    }

    fun closeAndDispose(project: com.intellij.openapi.project.Project): Boolean {
        var closed = false
        ApplicationManager.getApplication().invokeAndWait {
            closed = com.intellij.openapi.project.ProjectManager.getInstance().closeAndDispose(project)
        }
        return closed
    }

    fun openProject(path: String): com.intellij.openapi.project.Project? {
        var opened: com.intellij.openapi.project.Project? = null
        ApplicationManager.getApplication().invokeAndWait {
            opened = com.intellij.ide.impl.ProjectUtil.openOrImport(path, null, false)
        }
        return opened
    }
}
