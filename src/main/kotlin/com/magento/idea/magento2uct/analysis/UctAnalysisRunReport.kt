/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2uct.analysis

import com.magento.idea.magento2uct.packages.IssueSeverityLevel
import com.magento.idea.magento2uct.packages.SupportedIssue
import org.json.JSONArray
import org.json.JSONObject

/** Full, unpaginated report; reloads the exact saved findings without rerunning inspections. */
internal object UctAnalysisRunReport {
    fun write(view: UctAnalysisRuns.View): JSONObject = JSONObject().put("operation", "analyze")
        .put("state", view.state).put("error", view.error ?: JSONObject.NULL)
        .put("processedFiles", view.progress.processedFiles).put("totalFiles", view.progress.totalFiles)
        .put("request", JSONObject().put("paths", JSONArray(view.request.paths))
            .put("currentVersion", view.request.currentVersion ?: JSONObject.NULL).put("targetVersion", view.request.targetVersion)
            .put("minimumSeverity", view.request.minimumSeverity.name).put("ignoreCurrentVersion", view.request.ignoreCurrentVersion)
            .put("explainSuppressed", view.request.explainSuppressed))
        .put("result", view.result?.let { result -> JSONObject()
            .put("processedFiles", result.processedFiles).put("modules", result.modules).put("themes", result.themes)
            .put("analysisIdentity", result.analysisIdentity?.let(::JSONObject) ?: JSONObject.NULL)
            .put("fileTypeCounts", JSONObject(result.fileTypeCounts)).put("testFiles", result.testFiles)
            .put("suppressedByRule", result.suppressedByRule?.let { JSONObject(it.mapKeys { entry -> entry.key.toString() }) } ?: JSONObject.NULL)
            .put("suppressedBySeverity", result.suppressedBySeverity?.let(::JSONObject) ?: JSONObject.NULL)
            .put("findings", JSONArray(result.findings.map { finding -> JSONObject()
                .put("filePath", finding.filePath).put("line", finding.line).put("column", finding.column)
                .put("message", finding.message).put("issue", finding.issue.name)
                .put("code", finding.issue.code).put("severity", finding.issue.level.name.lowercase()) }))
        } ?: JSONObject.NULL)

    fun read(json: JSONObject): UctAnalysisRuns.View {
        require(json.getString("operation") == "analyze") { "Not an analysis report." }
        val request = json.getJSONObject("request")
        val result = json.optJSONObject("result")?.let { saved ->
            val findings = saved.getJSONArray("findings")
            UctAnalysisResult((0 until findings.length()).map { i -> findings.getJSONObject(i).let {
                UctFinding(it.getString("filePath"), it.getInt("line"), it.getInt("column"), it.getString("message"), SupportedIssue.valueOf(it.getString("issue")))
            } }, saved.getInt("processedFiles"), saved.getInt("modules"), saved.getInt("themes"),
                saved.optJSONObject("analysisIdentity")?.toString(), counts(saved.getJSONObject("fileTypeCounts")), saved.getInt("testFiles"),
                saved.optJSONObject("suppressedByRule")?.let { counts(it).mapKeys { entry -> entry.key.toInt() } },
                saved.optJSONObject("suppressedBySeverity")?.let(::counts))
        }
        return UctAnalysisRuns.View(json.getString("runId"), json.getString("state"),
            UctAnalysisRequest(request.getJSONArray("paths").toList().map { it as String },
                if (request.isNull("currentVersion")) null else request.getString("currentVersion"), request.getString("targetVersion"),
                IssueSeverityLevel.valueOf(request.getString("minimumSeverity")), request.getBoolean("ignoreCurrentVersion"), request.getBoolean("explainSuppressed")),
            UctAnalysisProgress(json.getInt("processedFiles"), json.getInt("totalFiles")), result,
            if (json.isNull("error")) null else json.getString("error"))
    }

    private fun counts(json: JSONObject): Map<String, Int> = json.keySet().associateWith { json.getInt(it) }
}
