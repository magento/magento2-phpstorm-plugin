# Development priorities

Read [AGENTS.md](../AGENTS.md) before proposing features, and follow the existing project roadmap. Manual IDE code generators are deprecated and maintenance-only. Prioritize PHP-independent capabilities, AI-assisted development, inspections, and analysis. The portability roadmap excludes code creation.

Repository development skills live in `.agents/skills`; the skills under `skills/` are bundled for users working on Magento applications.

# Setting up development environment

1. Check out this repository.
1. Open the project in IntelliJ IDEA.
1. Make sure that you are on the latest develop branch (for example `5.4.0-develop`).
1. Import the Gradle project from `build.gradle.kts`.
1. Use **JDK 25** for both the project SDK and the Gradle JVM.
1. In the Gradle tool window, run `Tasks > Intellij platform > runIde`.
1. The task launches a PhpStorm sandbox with the plugin installed. Make sure that the plugin is enabled and indexing is finished before testing features.

To use an extracted IDE distribution, pass `-PplatformLocalPath=/path/to/ide`. Windows CI uses the pinned PhpStorm Windows ZIP through this option.

To run `./gradlew test`, also install PHP CLI and add `php` (`php.exe` on Windows) to `PATH`. The run-configuration tests launch PHP to verify interpreter settings and command arguments. CI installs PHP 8.4 on Linux, Windows, and macOS.

# Optional plugin integrations

Shared, PHP, and shared MCP features are registered in `src/main/resources/META-INF/plugin.xml`, `withPhp.xml`, and `withMcp.xml`, respectively. Integrations that need both PHP and MCP or GraphQL use the optional content-module descriptors in `modules/php-mcp/src/main/resources/` and `modules/php-graphql/src/main/resources/`. Each descriptor declares dependencies on the main plugin and both required plugins. Implementation classes stay in the main plugin JAR; the descriptor modules are packaged separately in `lib/modules/`.

Run `./gradlew verifyPlugin -PverifyWebStorm=true --max-workers=1 --no-parallel` after changing these boundaries. This checks the configured PhpStorm and WebStorm versions; also exercise the affected behavior in both IDEs. CI runs the verifier and retains its reports, and publication requires verification to pass.

# MCP HTTP end-to-end tests

```sh
./gradlew mcpE2eTest
```

This starts the real bundled JetBrains MCP server in the headless IDE test application, opens an authenticated localhost HTTP session, and sends `initialize`, `notifications/initialized`, `tools/list`, and `tools/call` requests with the JDK HTTP client. It keeps the server's event stream open and deletes the session during teardown. No AI Assistant, model, API key, manually running IDE, browser, Docker installation, or Magento installation is required. JDK 25 and PHP CLI on `PATH` are required; Gradle downloads the IDE/test dependencies on the first run.

Each test owns a temporary project and an isolated server session. The suite covers published tool names/descriptions and typed schemas, project routing, actual JSON-RPC and tool errors, Docker-directory wrapper priority, Magento-directory fallback, magerun command paging/search/help, caching, and native MCP configuration. Magerun probes use the production process runner and a local PHP metadata fixture that records arguments, working directory, and environment. The Docker wrapper test uses a local `clinotty` shim on POSIX systems; it verifies the adapter and never contacts Docker. These fixtures test the plugin's API and runtime invocation, not a particular installed magerun release or container image.

Compatibility workflows now call `magento_compatibility` through the HTTP endpoint instead of injecting MCP call context or invoking the SDK transport adapter. Local release HTTP fixtures make preparation, progress, cancellation, persistence, and PHP/XML analysis repeatable without external downloads. Pure service tests remain for detailed validation and cache edge cases. Project routing can return plain-text tool errors, while invalid argument types can return JSON-RPC errors before the plugin runs; the tests verify both forms.

The HTTP suites also run with `./gradlew test`, so the existing Linux, Windows, and macOS CI jobs exercise them. To run only the CLI/magerun HTTP scenarios:

```sh
./gradlew mcpE2eTest --tests '*MagentoMcpHttpTest'
```

Reports are written to `build/reports/tests/mcpE2eTest/index.html` (or `build/reports/tests/test/index.html` when using `test`). The installed IDE's MCP settings and open projects are not used.

## External router discovery before setup

Run `./gradlew test --tests '*MagentoExternalDiscoveryMcpTest'` for the first-call discovery regression. Unlike the authorized-session fixtures, this test starts the sandbox's global MCP server, selects `VIA_ROUTER` in `McpToolFilterSettings` with its default filter, and uses an independent JDK HTTP client against `/stream` without a private-session token. It sends `initialize`, opens the event stream, sends `notifications/initialized`, and immediately requests `tools/list`. The project has disabled Magento integration, no configured root, and no installed Magento skills. The test restores the sandbox server settings afterward.

Observed on 2026-11-15 with PhpStorm **PS-263.7307.215** and plugin **2026.3.6-p1**:

- The first `tools/list` returned exactly `execute_tool`, `magento_intelligence`, and `magento_skills`.
- Direct `magento_intelligence` with `tool: "configure_magento_project"` and routed `execute_tool` with `command: "magento_intelligence --tool configure_magento_project"` and the current `projectPath` returned identical configuration descriptions and argument schemas.
- Direct and routed `magento_skills --mode status` returned the same three missing skills. Discovery and recovery responses left the project settings and instruction/skill files unchanged.
- Routed calls that encountered disabled support or an unconfigured root returned both setup-discovery invocation forms and the current `projectPath`.

The plugin registers its discovery toolsets through `META-INF/withMcp.xml`; their `McpToolset.alwaysIncluded()` overrides keep them directly visible in router mode. The global endpoint uses settings-based session options and filtering; the test does not force visibility through a Magento-only filter. No plugin-owned omission reproduced. A client that exposes only `execute_tool` to the model despite receiving this three-tool wire list is applying a later client/session exposure policy. The reported Codex omission was not reproduced in this external HTTP test; this test does not inspect Codex's internal model tool selection. Compare that client's own `tools/list` trace before attributing an omission to it.

The inspected JetBrains build provides `mcpToolsProvider`, `mcpToolset`, `mcpToolFilterProvider`, and `mcpManagedSessionSupport` extension points, but no extension point for appending plugin discovery hints to the router description. `UniversalToolset.execute_tool` declares the fixed description “Universal tool executor that can invoke specific IDE MCP tool dynamically.” Its `displayDescription(toolName)` belongs to `UniversalToolset` and does not aggregate other plugins' discovery hints. Keep discovery hints in Magento tool descriptions/responses; do not replace or mutate the platform-owned router descriptor. Recheck the SDK when targeting a newer IDE build.

To additionally test compatibility preparation against official release archives:

```sh
MAGENTO_MCP_REAL_RELEASE_TEST=true ./gradlew mcpE2eTest --tests '*MagentoCompatibilityCommandsTest.testHttpMcpWithActualMagentoReleases'
```

This opt-in test downloads release sources into a temporary IDE test cache; routine tests need no external network once Gradle dependencies are cached.

# Plugin publication

1. [Build a new version of the plugin](https://www.jetbrains.org/intellij/sdk/docs/basics/getting_started/deploying_plugin.html)
1. [Install the newly created archive/jar file from disk](https://www.jetbrains.com/help/idea/managing-plugins.html#installing-plugins-from-disk) for testing
1. [Publish](https://www.jetbrains.org/intellij/sdk/docs/basics/getting_started/publishing_plugin.html)

# Contribution process

If you are a new GitHub user, we recommend that you create your own [free github account](https://github.com/signup/free). This will allow you to collaborate with the Magento 2 development team, fork this project and send pull requests.

1. Search current [listed issues](https://github.com/magento/magento2-phpstorm-plugin/issues) (open or closed) for similar proposals of intended contribution before starting work on a new contribution.
2. Review and sign the [Contributor License Agreement (CLA)](https://opensource.adobe.com/cla.html) if this is your first time contributing. You only need to sign the CLA once.
3. Create and test your work.
4. Fork this repository and create a pull request.
5. Once your contribution is received the Magento 2 development team will review the contribution and collaborate with you as needed.
