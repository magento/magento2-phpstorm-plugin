/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.mcp

import com.intellij.openapi.project.Project
import com.magento.idea.magento2plugin.project.MagentoSkillInstaller
import com.magento.idea.magento2plugin.project.MagentoSkillInstaller.AgentTarget
import com.magento.idea.magento2plugin.project.MagentoSkillInstaller.Skill
import java.io.IOException
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path

/** Bundled application skills only; never edits project instruction files or downloads content. */
internal object MagentoSkillCommands {
    const val WIKI_URL = "https://github.com/magento/magento2-phpstorm-plugin/wiki/MCP#suggested-agentsmd-instructions"
    private val targets = linkedMapOf(
        "project" to AgentTarget.PROJECT_SKILLS,
        "agents" to AgentTarget.AGENTS,
        "codex" to AgentTarget.CODEX,
        "claude" to AgentTarget.CLAUDE_CODE
    )

    data class Installation(val skill: Skill, val target: AgentTarget, val path: Path, val previous: String)

    fun help(): String = """
        Magento and Adobe Commerce bundled agent skills
        Use magento_intelligence for the full Magento MCP tool catalog.
        For status via execute_tool, pass magento_skills --mode status --agentTarget agents.
        mode=help: Show this documentation without reading or writing project files.
        mode=status (default): Read-only catalog and installation status for the selected agentTarget.
        mode=install: Install the explicitly selected skills; requires a non-empty skills array.
        skills: ["magento-inspect", "magento-scaffold", "magento-magerun"] or a subset. No automatic installation.
        agentTarget: project (default, skills/), agents (.agents/skills/), codex (.codex/skills/), claude (.claude/skills/).
        overwrite: false by default. Review customized files before explicitly replacing them with overwrite=true.
        projectPath: Optional intended IDE project directory, verified before reading or writing skills.
        Folders are relative to the IDE project root, not the configured Magento root or the user's home.
        Installation works before Magento integration is enabled and during indexing, without the PHP plugin.
        magento-inspect: Configuration inspection, project setup, CLI discovery and upgrade compatibility where available.
        magento-scaffold: Explicitly requested code creation; usage requires the JetBrains PHP plugin.
        magento-magerun: Everyday Magento investigation with an installed n98-magerun2 and project runtime.
        Returns per-skill paths and results, and suggested Markdown links for an agent to merge into existing AGENTS.md.
        Never creates or edits AGENTS.md, downloads skills, or changes Magento settings. Existing identical files are unchanged.
        Suggested project guidance: $WIKI_URL
        Example: {"mode":"install","skills":["magento-inspect"],"agentTarget":"agents"}
    """.trimIndent()

    fun execute(project: Project, mode: String, skills: List<String>, agentTarget: String,
                overwrite: Boolean, projectPath: String): String {
        if (mode == "help") return help()
        return try {
            require(mode in setOf("status", "install")) { "Unknown mode '$mode'. Use help, status, or install." }
            if (projectPath.isNotBlank()) {
                val base = requireNotNull(project.basePath) { "Unable to resolve project directory." }
                require(Path.of(projectPath).toAbsolutePath().normalize() == Path.of(base).toAbsolutePath().normalize()) {
                    "projectPath does not match the active project (expected: $base, received: $projectPath)"
                }
            }
            if (mode == "status") {
                val catalog = prepare(project, Skill.values().map { it.directoryName }, agentTarget, true)
                val lines = catalog.map { "${it.skill.directoryName}: ${it.previous} — ${it.path}" }
                "Magento agent skills status (read-only)\n" + lines.joinToString("\n") + "\n" +
                    guidance(project, catalog.filter { it.previous != "missing" })
            } else {
                require(skills.isNotEmpty()) { "Provide a non-empty skills array. Use mode=status for the catalog." }
                install(project, prepare(project, skills, agentTarget, overwrite), overwrite)
            }
        } catch (failure: IllegalArgumentException) {
            "Result: error\nMessage: ${failure.message}\nNo skills were installed."
        } catch (failure: IOException) {
            "Result: error\nMessage: ${failure.message}\nNo skills were installed."
        }
    }

    /** Validate all requested destinations before any skill files are changed. */
    fun prepare(project: Project, names: List<String>, targetName: String, overwrite: Boolean): List<Installation> {
        val target = targets[targetName]
            ?: throw IllegalArgumentException("Unknown agentTarget '$targetName'. Use ${targets.keys.joinToString()}.")
        val skills = names.distinct().map { name ->
            Skill.values().find { it.directoryName == name }
                ?: throw IllegalArgumentException("Unknown skill '$name'. Use ${Skill.values().joinToString { it.directoryName }}.")
        }
        return skills.map { skill ->
            val path = MagentoSkillInstaller.getTargetPath(project, skill, target)
            val exists = Files.exists(path, LinkOption.NOFOLLOW_LINKS)
            val different = MagentoSkillInstaller.hasDifferentExistingSkill(project, skill, target)
            if (different && !overwrite) {
                throw IllegalArgumentException(
                    "Customized skill preserved: $path. Review it before explicitly setting overwrite=true."
                )
            }
            Installation(skill, target, path, if (!exists) "missing" else if (different) "different" else "identical")
        }
    }

    fun install(project: Project, plan: List<Installation>, overwrite: Boolean): String {
        val lines = mutableListOf("Agent skill installation")
        val installed = mutableListOf<Installation>()
        var failed = false
        for (entry in plan) {
            try {
                MagentoSkillInstaller.install(project, entry.skill, entry.target, overwrite)
                installed += entry
                val state = when (entry.previous) {
                    "identical" -> "unchanged"
                    "different" -> "updated"
                    else -> "installed"
                }
                lines += "${entry.skill.directoryName}: $state — ${entry.path}"
            } catch (failure: IOException) {
                failed = true
                lines += "${entry.skill.directoryName}: error — ${failure.message}"
            }
        }
        lines += "Skill installation result: ${if (failed) "incomplete; successful writes above were retained" else "success"}"
        lines += guidance(project, installed)
        return lines.joinToString("\n")
    }

    fun guidance(project: Project, installed: List<Installation> = emptyList()): String = buildString {
        appendLine("Optional agent skills: magento-inspect (inspection, CLI, setup and compatibility); magento-scaffold (explicit code creation; requires the JetBrains PHP plugin); magento-magerun (everyday Magento investigation using n98-magerun2).")
        appendLine("Use magento_skills with mode=status to inspect installed skills, or mode=install, skills=[\"magento-inspect\"], agentTarget=project|agents|codex|claude to install selected skills.")
        appendLine("Skill folders are relative to the IDE project root, even when Magento is under src/: project=skills, agents=.agents/skills, codex=.codex/skills, claude=.claude/skills.")
        appendLine("Suggested AGENTS.md guidance: $WIKI_URL")
        appendLine("AGENTS.md was not created or modified. Read the existing project instructions and merge only relevant guidance, preserving project rules and avoiding duplicate links. Adjust relative links if using a nested instructions file.")
        project.basePath?.let { base ->
            val agents = Path.of(base).resolve("AGENTS.md")
            appendLine("Project instructions path: $agents (${if (Files.exists(agents, LinkOption.NOFOLLOW_LINKS)) "exists; read before editing" else "absent; create only as part of the requested setup"}).")
        }
        if (installed.isNotEmpty()) {
            appendLine("Suggested Markdown for the agent to adapt into the existing AGENTS.md:")
            appendLine("## Magento MCP skills")
            for (entry in installed) {
                val purpose = when (entry.skill) {
                    Skill.MAGENTO_INSPECT -> "Inspect Magento configuration, discover project CLI tools, and check upgrade compatibility where supported."
                    Skill.MAGENTO_SCAFFOLD -> "Use for explicitly requested Magento code creation when magento_scaffold is available (requires the JetBrains PHP plugin)."
                    Skill.MAGENTO_MAGERUN -> "Investigate Magento applications using the project's n98-magerun2 runtime."
                }
                appendLine("- [${entry.skill.directoryName}](${entry.target.getProjectRelativePath(entry.skill)}): $purpose")
            }
            appendLine("- [Magento MCP project guidance]($WIKI_URL)")
        }
    }.trimEnd()
}
