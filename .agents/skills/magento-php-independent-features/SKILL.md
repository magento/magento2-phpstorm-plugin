---
name: magento-php-independent-features
description: Assess or implement PHP-plugin-independent features in the Magento IDE plugin, including WebStorm portability, optional plugin registrations, and separation of XML or JavaScript intelligence from PHP PSI. Use for this plugin repository, not Magento application development.
---

# PHP-independent Magento plugin features

Read the repository [AGENTS.md](../../../AGENTS.md) and follow the existing project roadmap. Prioritize existing-code intelligence, inspections, navigation, and analysis. Manual IDE code generators are deprecated and maintenance-only; exclude code creation from portability recommendations.

## Trace the actual dependency boundary

Start from the feature registration:

- `src/main/resources/META-INF/plugin.xml` contains shared features and optional dependency declarations; `withPhp.xml` contains PHP-dependent features, and `withMcp.xml` registers shared MCP tools.
- `modules/php-mcp/src/main/resources/com.magento.idea.magento2plugin.php.mcp.xml` registers PHP-backed MCP tools and providers only when PHP and MCP are available.
- `modules/php-graphql/src/main/resources/com.magento.idea.magento2plugin.php.graphql.xml` registers resolver features only when PHP and GraphQL are available. `src/main/resources/META-INF/withJsGraphQl.xml` only makes GraphQL classes available to the main implementation JAR.

The optional content modules contain descriptors; implementation classes remain in the main plugin JAR. Preserve each module's explicit dependencies on the main plugin and both required plugins, and its separate packaging under `lib/modules/`.

Follow the reachable providers, helpers, indexes, inheritance, field/method types, static initializers, serializers, and reflective loading. Search for `com.jetbrains.php`, `PhpIndex`, PHP PSI, and mixed-purpose helpers. No direct PHP imports is insufficient evidence of independence.

Classify each dependency as XML/JS/project functionality, a replaceable PHP utility or inspection base, or genuine PHP semantic analysis. `DiIndex` still includes PHP semantics and delegates XML lookups to `XmlVirtualTypeIndex`. `EventNameIndex` and MCP method queries use optional PHP providers; inspect their registrations and current helpers before applying earlier findings.

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

Keep README, changelog, and bundled user-skill updates focused on available features, setup, usage, and limitations that affect the user's task. Leave internal reasoning, implementation details, migration mechanics, and verification procedures in developer-facing documentation or the implementation report; do not add them to user-facing documentation.
