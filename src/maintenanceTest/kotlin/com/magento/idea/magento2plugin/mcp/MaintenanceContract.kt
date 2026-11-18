/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.mcp

import org.json.JSONArray
import org.json.JSONObject
import java.nio.file.Files
import java.nio.file.Path

/** Identical launcher and cases for both desktop hosts and the HCE stdio suite. */
internal object MaintenanceContract {
    fun prepare(project: Path, magento: Path) {
        Files.createDirectories(project.resolve("bin"))
        Files.writeString(project.resolve("compose.yaml"), "services: {}")
        Files.writeString(project.resolve("bin/magento"), "#!/bin/sh\nbin/cli bin/magento \"\$@\"\n")
        check(project.resolve("bin/magento").toFile().setExecutable(true))
        Files.writeString(project.resolve("bin/clinotty"), resource("launcher.py"))
        check(project.resolve("bin/clinotty").toFile().setExecutable(true))
        Files.writeString(magento.resolve("composer.lock"), """{"packages":[],"packages-dev":[]}""")
    }

    fun run(project: Path, magento: Path = project.resolve("src"), call: (JSONObject) -> JSONObject) {
        val cases = JSONArray(resource("cases.json"))
        for (i in 0 until cases.length()) {
            val case = cases.getJSONObject(i)
            case.optJSONObject("files")?.let { files -> for (name in files.keySet()) {
                val file = magento.resolve(name)
                Files.createDirectories(file.parent)
                Files.writeString(file, files.getString(name))
            } }
            Files.writeString(project.resolve("observation.json"), case.getJSONObject("observation").toString())
            Files.writeString(project.resolve("probe-log"), "")
            val result = call(case.getJSONObject("args"))
            val actual = result.getJSONArray("findings").map { it as JSONObject }.toMutableList()
            for (entry in case.getJSONArray("expected")) {
                val expected = entry as JSONObject
                val index = actual.indexOfFirst { f -> (expected.keySet() - "argvTail").all { f.opt(it) == expected.get(it) } }
                check(index >= 0) { "${case.getString("name")}: expected $expected in $result" }
                val finding = actual.removeAt(index)
                for (key in listOf("checkId", "environment", "complete", "evidence", "nextAction", "item")) {
                    check(finding.has(key)) { finding }
                }
                expected.optJSONArray("argvTail")?.let { tail ->
                    check(finding.getJSONObject("suggestedCommand").getJSONArray("argv").toList().takeLast(tail.length()) == tail.toList())
                }
                if (finding.getString("status") in setOf("unknown", "clear")) check(!finding.has("suggestedCommand"))
                if (finding.getString("status") == "required") {
                    val command = finding.getJSONObject("suggestedCommand")
                    check(command.getString("cwd").isNotBlank() && command.getJSONArray("argv").length() > 0)
                }
            }
            check(actual.isEmpty()) { "Unexpected findings: $actual" }
            if (case.getJSONObject("args").getString("mode") == "check") {
                check(Files.readString(project.resolve("probe-log")).isEmpty())
            }
        }
    }

    private fun resource(name: String) = MaintenanceContract::class.java.getResource("/maintenance/$name")!!.readText()
}
