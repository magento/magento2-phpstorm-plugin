/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.mcp

import com.intellij.mcpserver.McpToolCallResult
import com.intellij.mcpserver.McpToolCallResultContent
import org.json.JSONObject

/** JSON text is the canonical wire format, including errors and documentation. */
internal object MagentoCompatibilityResponse {
    fun envelope(body: JSONObject, operation: String): JSONObject {
        val nextMode = body.optJSONObject("nextCall")?.optJSONObject("arguments")?.optString("mode")
        val waitingStatus = operation in setOf("status", "upgrade") && nextMode in setOf("status", "results")
        val state = body.optString("state").ifEmpty { if (waitingStatus) "running" else "completed" }
        val polling = state == "running"
        return body.put("schemaVersion", 1).put("operation", body.optString("operation").ifEmpty { operation })
            .put("state", state).put("terminal", state != "running" && state != "blocked")
            .put("successful", state == "completed" && body.optJSONObject("error") == null)
            .put("pollRequired", polling)
            .put("nextStep", nextStep(body, operation, state, polling))
            // complete is retained as a legacy success flag. terminal governs job termination.
            .apply {
                optJSONObject("error")?.let { if (!it.has("recoverable")) it.put("recoverable", false) }
                if (!has("complete")) put("complete", state == "completed")
                if (!has("error")) put("error", JSONObject.NULL)
                if (!has("nextCall")) put("nextCall", JSONObject.NULL)
                if (!has("retryAfterMs")) put("retryAfterMs", JSONObject.NULL)
            }
    }

    /** One decision for clients; success/termination do not imply workflow completion. */
    private fun nextStep(body: JSONObject, operation: String, state: String, polling: Boolean): String = when {
        body.optJSONObject("nextCall") != null -> if (polling) "poll" else "call"
        body.optJSONObject("error") != null || state == "failed" -> "resolve_error"
        operation in setOf("status", "upgrade") && body.has("pluginEnabled") && !body.getBoolean("pluginEnabled") -> "enable_support"
        operation in setOf("status", "upgrade") && body.optJSONObject("readiness")?.optBoolean("ready") == true -> "analyze"
        operation in setOf("status", "upgrade") && body.optJSONObject("readiness")?.isNull("targetVersion") == true -> "select_target"
        else -> "done"
    }

    fun failure(operation: String, code: String, message: String, parameter: String? = null): JSONObject =
        envelope(JSONObject().put("state", "failed").put("complete", false)
            .put("error", JSONObject().put("code", code).put("message", message)
                .put("recoverable", false).apply { if (parameter != null) put("parameter", parameter) }), operation)

    fun wire(body: JSONObject): McpToolCallResult = McpToolCallResult(
        content = arrayOf(McpToolCallResultContent.Text(body.toString())),
        // JetBrains strips this field on errors. Omitting it consistently avoids two wire shapes.
        structuredContent = null,
        isError = body.optJSONObject("error") != null || body.optString("state") == "failed"
    )
}
