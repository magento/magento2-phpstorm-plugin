---
name: magento-php-independent-features
description: Assess or implement PHP-plugin-independent features in the Magento IDE plugin, including WebStorm portability, optional plugin registrations, and separation of XML or JavaScript intelligence from PHP PSI. Use for this plugin repository, not Magento application development.
---

# PHP-independent Magento plugin features

Read the repository [AGENTS.md](../../../AGENTS.md) and follow the existing project roadmap. Prioritize existing-code intelligence, inspections, navigation, and analysis. Manual IDE code generators are deprecated and maintenance-only; exclude code creation from portability recommendations.

## Trace the actual dependency boundary

Start from the feature registration in `src/main/resources/META-INF/`:

- `plugin.xml` contains shared features and optional dependency declarations.
- `withPhp.xml` contains PHP-dependent features and nested optional dependencies.
- `withMcp.xml` registers shared MCP tools; `withPhpMcp.xml` registers PHP-backed tools.
- `withJsGraphQl.xml` is currently nested under PHP and contains PHP-backed resolver features. Preserve both plugin dependencies where needed.

Follow the reachable providers, helpers, indexes, inheritance, field/method types, static initializers, serializers, and reflective loading. Search for `com.jetbrains.php`, `PhpIndex`, PHP PSI, and mixed-purpose helpers. No direct PHP imports is insufficient evidence of independence.

Classify each dependency as XML/JS/project functionality, a replaceable PHP utility or inspection base, or genuine PHP semantic analysis. Examples of mixed helpers are `DiIndex`, `EventNameIndex`, `MagentoMcpSupport`, and `MagentoDiQueries`; inspect current code before applying earlier findings.

## Separate shared behavior

- Extract shared lookup/analysis services and retain optional PHP providers for class, method, inheritance, and dispatch-call analysis.
- Move registrations together with the indexes and services the feature needs. Remove duplicate registrations from PHP descriptors so PhpStorm does not produce duplicate results.
- Preserve other dependency boundaries: PHP independence does not make GraphQL or MCP APIs unconditional.
- Preserve existing normalization and index-key semantics when replacing PHP utilities. Check serializer compatibility and bump index versions when stored representations change.
- Use existing shared module/theme indexes rather than requiring PHP registration-file PSI.
- Keep unsupported PHP semantics explicit. XML declarations establish configuration facts; they do not establish PHP class existence, inheritance, or effective runtime behavior.

Moving `CronGroupIndexer`, `ProductTypeIndex`, or layout/block indexes without an applicable consumer is not a user-visible migration. Recommend concrete behavior and its dependency changes.

## Verify and report

Use focused behavior tests for the changed feature and a WebStorm runtime without the PHP plugin. Existing `test` and `mcpE2eTest` tasks use the configured PhpStorm test platform; they cannot prove PHP-independent loading. Use the WebStorm `uiTest` setup for relevant feature checks and `verifyPlugin -PverifyWebStorm=true` for compatibility verification. Also check the existing PHP behavior.

For an assessment, report the feature, user/agent benefit, actual blocker, relative effort, source evidence, and required validation. Separate proposed migrations from functionality that is already shared. For implementation, report the behavior delivered, retained optional capabilities, tests run, and coverage limitations.
