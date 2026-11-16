# Magento 2 and Adobe Commerce for HCE

The HCE integration has its own plugin identity, build, tests, and CI. Project
settings and CLI environment discovery are its first toolset. The plugin can add
Magento analysis toolsets without changing its identity or loading desktop UI,
PHP, or JavaScript dependencies into this module unconditionally.

## Current tools

| Tool | Behavior |
| --- | --- |
| `configure_magento_project` | `action=status` (default) reads settings; `action=help` describes supported settings; `action=configure` explicitly persists root, enabled state, and/or CLI wrapper candidates. |
| `get_magento_root_path` | Reads the configured absolute root. Reports an unconfigured project explicitly. |
| `describe_magento_cli_environment` | Uses the shared Magento discovery service for wrappers, Compose priority, working directories, and DDEV configuration/overrides. Requires enabled integration and a configured root. |

Configure with, for example:

```json
{"action":"configure","magentoPath":"src","enabled":true,"cliToolCandidates":["bin/magento","bin/php","bin/composer"]}
```

Paths resolve inside the opened project, including symlink checks. The module
does not claim a directory is a verified Magento installation. Responses are JSON
text with `status` and `successful`; inspect them even when the transport's
`isError` is false. Disabled support, missing settings, invalid configuration,
storage errors, and unsupported capabilities are distinct states.

Discovery reads files only. It does not execute wrappers, PHP, Docker, DDEV, or
Magerun, start services, probe container state, or install skills. DDEV settings
are a bounded static view; secret environment entries and hooks are not exported.
Indexing exclusions are not implemented here: passing `excludeGeneratedFiles`
returns `unsupported_capability` without changing settings.

## Configuration lifetime

HCE's analysis project settings did not survive tool calls. `SettingsStore`
therefore stores a versioned JSON document per canonical project path under the
host configuration directory, using a SHA-256 filename, atomic replacement, and
transaction locking. Every tool call reloads it. Settings survive analysis-context
replacement and process restarts and remain separate between projects.

The default directory is `PathManager.getConfigPath()/magento-hce`. Set the module's
own JVM property `-Dmagento.hce.settings.dir=/absolute/stable/directory` through
the launcher's `IJ_JAVA_OPTIONS` to choose another location. This is an option of
this integration, not an undocumented HCE flag. Changing HCE's configuration root
also changes the default store; use the explicit property when sharing settings
across isolated engine sessions. No project instruction file is modified.

## Build and installation

Requires JDK 25 and the repository's Gradle wrapper. The independently compiled
core uses the repository version catalog. The host is pinned to
**IntelliJ Language Server by JetBrains ILS-271.2085.0**, package `intellij-server`
2027.1. [runtime.json](runtime.json) records the official client manifest, download
URLs, and checksums. The small JSON library matches the main plugin; SnakeYAML
Engine 2.9 matches the tested engine's bundled version.

```sh
./gradlew -p modules/hce test
./gradlew -p modules/hce packageHcePlugin -PhceRuntime=/absolute/path/to/intellij-server-2027.1
python3 modules/hce/tests/check_archive.py modules/hce/build/distributions/magento-hce.zip
```

Extract `magento-hce.zip` into a dedicated plugins directory and pass that directory
with `-Didea.plugins.path=...` through `IJ_JAVA_OPTIONS`. The engine's bundled
`bin/idea.properties` documents this property. Launch using its documented
`intellij-server --stdio`, `--system-path`, EULA, and license mechanisms. The
module depends on the observed engine plugin ID `org.jetbrains.ls.plugin.mcp`.
Discover schemas through the engine's advertised `list-mcp-tools` command before
using `run-mcp-tool`. This module uses that native LSP bridge; it does not add an
HTTP server or imply compatibility with the full desktop plugin.

The module intentionally remains outside the main Gradle settings: ordinary
PhpStorm/WebStorm builds do not resolve or package its HCE-specific classes.

## Separate verification

`src/test` contains plain JVM settings and discovery tests, including concurrent
updates, project isolation, invalid/corrupt settings, root boundaries, wrapper
selection, DDEV overrides, and read-only behavior. They need neither an IDE nor
an HCE distribution or license.

`tests/runtime_contract_test.py` launches the real engine, opens copies of the
existing repository fixture, initializes LSP, discovers tool schemas, and calls
the actual tools. It verifies settings across calls and engine restart, project
isolation, Compose wrappers, DDEV updates/errors, unsupported exclusions, and
clean shutdown. It never executes the fixture wrappers.

```sh
python3 modules/hce/tests/runtime_contract_test.py \
  --runtime /absolute/path/to/intellij-server-2027.1 \
  --plugin modules/hce/build/distributions/magento-hce.zip \
  --license-dir /absolute/path/to/already-activated-store \
  --output build/hce/contracts-local \
  --accept-eula
```

Read the installed runtime's EULA first. Use a new output directory per run.
Python 3.9+ runs the contracts; the CI download helper uses Python 3.13 for safe
Linux archive extraction. The helper only obtains/verifies the distribution; it
does not launch or activate it. No distribution, cache, license, or generated log
belongs in version control.

`.github/workflows/hce.yml` provides separate jobs:

- Automatic PR core tests on Linux and macOS, without HCE.
- Automatic pinned host compilation and archive checks, without launching HCE.
- Manual actual-runtime contracts on a provisioned `[self-hosted, hce, linux, x64]`
  runner. Set repository variables `HCE_RUNTIME` and `HCE_LICENSE_DIR` to its
  installed runtime and valid activated license store. Select both
  `run_runtime_tests` and `accept_runtime_eula` when dispatching. CI does not
  create/reset trials or bypass activation. Provisioning and permitted unattended
  use remain the runner owner's responsibility.

## Extension boundaries

`src/main` holds host-independent settings/project services and compiles a small
allowlist of existing shared discovery sources. `src/host` holds HCE MCP
registration and project-context adaptation. Additional toolsets can reuse the
same settings store and shared analysis services, with their own contracts and
explicit capability gates. Keep optional language dependencies out of the core.

Module/layout index enumeration, general IDE inspections, PHP semantics,
JavaScript semantics, GraphQL, upgrade compatibility, and command execution are
separate capabilities; this implementation does not advertise them. The historical
exploratory probes and report are kept outside the repository at the user's request.
