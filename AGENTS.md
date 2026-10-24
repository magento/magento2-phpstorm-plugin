# Repository direction

This repository develops the Magento 2 and Adobe Commerce IDE plugin. Apply these priorities when planning, implementing, or reviewing plugin changes.

## 1. Manual code creation is deprecated

Manual code creation through IDE menus, dialogs, and wizards is maintenance-only. Do not propose new manual generators, expand their feature sets, or port them to WebStorm as roadmap work. Fix correctness, security, and compatibility problems in existing generators when needed. Deprecation does not authorize deleting existing actions, templates, or public APIs.

AI-assisted tools now handle boilerplate and project-specific code generation well enough that expanding dedicated generator dialogs is no longer a priority. Invest in Magento-specific context, inspections, and analysis that help developers and AI agents produce and validate correct changes.

Existing AI-invoked scaffolding remains available for explicit creation requests. It is distinct from manual generator investment. The current portability and analysis roadmap excludes all code-creation features.

## 2. Prioritize PHP-independent and AI-assisted development

Favor features that help developers and AI agents understand, inspect, navigate, validate, and analyze existing Magento projects:

- Shared XML, JavaScript, HTML, JSON, module, and theme intelligence that works without the JetBrains PHP plugin.
- Accurate inspections with actionable explanations and source locations.
- Configuration, dependency, reference, and impact analysis exposed through reusable services and MCP tools.
- Reliable capability discovery, scoped queries, bounded results, and explicit analysis coverage.

PHP independence means independence from the IDE's PHP plugin. It does not imply that running Magento or a PHP CLI command needs no PHP runtime. Keep PHP PSI, class hierarchy, method resolution, and other semantic PHP analysis optional; do not substitute text matching and claim equivalent coverage.

Use the existing project roadmap for feature priorities and sequencing. Keep repository policy here; do not create a separate development-direction document.

## Implementation boundaries

- Register shared functionality in `src/main/resources/META-INF/plugin.xml`, PHP-backed functionality in `withPhp.xml`, and shared MCP tools in `withMcp.xml`. Register integrations requiring both PHP and another plugin in the optional content-module descriptors under `modules/php-mcp/src/main/resources/` and `modules/php-graphql/src/main/resources/`; preserve their explicit dependencies on the main plugin and both required plugins.
- Audit reachable helpers, indexes, base classes, signatures, static initialization, and serializers before moving registrations. A class without a direct PHP import can still load PHP-dependent code.
- Keep analysis logic independent of Swing dialogs and actions so editor inspections and MCP can reuse it.
- Preserve existing PHP behavior while adding useful behavior without PHP. Distinguish unavailable analysis, incomplete indexing, cancellation, and partial coverage from a successful empty result.
- Keep query and analysis operations read-only unless a change is part of the user's request. Include enough source evidence for an agent to verify findings.

## Repository skills

- Use [magento-php-independent-features](.agents/skills/magento-php-independent-features/SKILL.md) for plugin dependency separation and WebStorm feature portability.
- Use [magento-ai-analysis](.agents/skills/magento-ai-analysis/SKILL.md) for inspections, analysis services, and agent-facing MCP capabilities.
- `skills/magento-inspect` and `skills/magento-scaffold` are bundled user skills for working on Magento applications. Repository development skills belong in `.agents/skills` and are not packaged as plugin resources.

## Verification

Use focused existing tests and add behavior coverage for changed analysis or dependency boundaries. A passing PhpStorm test does not establish WebStorm compatibility.

- `./gradlew test --tests '<relevant test class>'` runs focused tests in the configured PhpStorm test platform.
- `./gradlew uiTest --tests '<relevant UI test class>'` uses the configured WebStorm runtime; exercise moved features without PHP support.
- `./gradlew mcpE2eTest` checks real MCP HTTP contracts in the configured test platform; it is not a WebStorm runtime check.
- `./gradlew verifyPlugin -PverifyWebStorm=true` includes WebStorm in plugin verification.

Choose checks appropriate to the change. Documentation-only changes need link/content and skill validation, not a full IDE test run. See [CONTRIBUTING](.github/CONTRIBUTING.md) for environment requirements.
