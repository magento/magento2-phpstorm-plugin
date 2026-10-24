---
name: magento-ai-analysis
description: Design, implement, or review inspections, configuration and dependency analysis, and agent-facing MCP capabilities in the Magento IDE plugin. Use for shared analysis services, diagnostic quality, capability discovery, and analysis contracts in this repository, not for running Magento application scaffolding.
---

# Magento inspections and AI-assisted analysis

Read the repository [AGENTS.md](../../../AGENTS.md) and follow the existing project roadmap. Prioritize accurate project knowledge available to both developers and AI agents. Manual menu/dialog/wizard code creation is deprecated and maintenance-only; code creation is outside the analysis roadmap.

## Choose the analysis boundary

Identify the question the feature answers and the evidence needed: XML declarations, module metadata, JavaScript references, PHP semantics, or runtime observations. Reuse existing indexes and analysis services where they provide that evidence. Separate analysis from Swing actions/dialogs so editor inspections and MCP tools can share the implementation.

Default to the shared, PHP-independent layer for XML/JS/HTML/JSON and project metadata. Keep genuine PHP semantic analysis behind optional PHP providers. Use [magento-php-independent-features](../magento-php-independent-features/SKILL.md) when changing that boundary.

Current starting points:

- `MagentoInspectionCommands` defines the `magento_inspect` catalog and dispatcher.
- `MagentoModuleQueries`, `MagentoViewQueries`, `MagentoEventQueries`, and `MagentoMcpSnapshots` contain module/configuration lookup logic.
- `MagentoDiQueries` and `MagentoMcpSupport` provide shared XML lookup and utilities. PHP method interception is contributed through `MagentoMethodPluginQueryProvider`; its implementation and PHP helpers remain optional.
- `MagentoMcpProjectToolset` exposes shared inspection queries and CLI/project tools through `src/main/resources/META-INF/withMcp.xml`. PHP-backed MCP tools and providers are registered in `modules/php-mcp/src/main/resources/com.magento.idea.magento2plugin.php.mcp.xml`, which requires the main plugin, PHP, and MCP. Confirm current descriptors and query capabilities before claiming availability.
- `magento_compatibility` already has background jobs and result retrieval. Reuse relevant patterns when new analysis needs them, while respecting its PHP dependency and actual coverage.

## Define a useful agent contract

- Expose actual host capabilities in discovery/help/schema. Distinguish unsupported analysis from a supported query with zero matches. Avoid loading PHP-dependent classes merely to check capability availability.
- Provide explicit file/module/project scope, canonical identifiers, and paths that an agent can resolve. Avoid automatic whole-project scans for focused requests.
- For diagnostics, provide stable rule identifiers, severity, explanation, source locations, and related declarations when relevant. Explain the evidence and any uncertainty.
- Make ordering deterministic and bound large responses with pagination or scoped summaries. Preserve existing tool names, parameter contracts, and response compatibility when evolving formats.
- For long-running work, support cancellation and report completion, processed scope, indexing readiness, and partial/unsupported coverage. A failed or incomplete scan is not a clean result.
- Keep analysis read-only. Treat applying fixes, generating files, changing settings, downloading dependencies, and executing application commands as distinct operations within the user's requested scope.
- Report static declarations as static evidence. Do not claim effective Magento merge order, PHP type validity, or runtime reachability unless the analysis actually establishes it.

## Validate observable behavior

Use fixtures that demonstrate the intended finding, a valid case that should not be flagged, and relevant area/module boundaries. Verify source locations and incomplete/unavailable states when affected. For shared PHP-independent behavior, exercise both WebStorm without PHP and the existing PhpStorm behavior.

When MCP contracts change, test discovery, schema/arguments, project routing, success, and relevant errors through the existing HTTP test setup. Add WebStorm capability coverage separately; the current `mcpE2eTest` task is not a WebStorm runtime test. Update affected bundled user skills under `skills/` to describe real capabilities rather than planned ones.

Report what an agent can now learn, the supported scope and host capabilities, evidence returned, validation performed, and remaining coverage limits.

Keep user-facing documentation focused on the questions users can answer, how to run the tools, and how to interpret their results. Include requirements and coverage limits that change usage; keep internal reasoning, implementation details, and verification procedures in developer-facing documentation or the implementation report.
