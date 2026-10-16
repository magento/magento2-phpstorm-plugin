# Setting up development environment

1. Check out this repository.
1. Open the project in IntelliJ IDEA.
1. Make sure that you are on the latest develop branch (for example `5.4.0-develop`).
1. Import the Gradle project from `build.gradle.kts`.
1. Use **JDK 25** for both the project SDK and the Gradle JVM.
1. In the Gradle tool window, run `Tasks > Intellij platform > runIde`.
1. The task launches a PhpStorm sandbox with the plugin installed. Make sure that the plugin is enabled and indexing is finished before testing features.

To run `./gradlew test`, also install PHP CLI and add `php` (`php.exe` on Windows) to `PATH`. The run-configuration tests launch PHP to verify interpreter settings and command arguments. CI installs PHP 8.4 on Linux, Windows, and macOS.

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
