# Magento 2 and Adobe Commerce for HCE

The HCE integration has its own plugin identity, build, tests, and CI. Project
settings, CLI environment discovery, bundled skill delivery, Magerun metadata, and
shared XML analysis are its initial toolsets. The plugin can add
Magento analysis toolsets without changing its identity or loading desktop UI,
PHP, or JavaScript dependencies into this module unconditionally.

## Install on a development host

Run setup as the developer who uses the Magento checkout. The pilot targets Linux x86-64 and Apple Silicon macOS with Python 3.12+ and a JetBrains license valid for the pinned engine. For Adobe Commerce Cloud, use a development VM or workstation with the checkout and its Composer dependencies installed. Run the command from the directory containing Magento’s `composer.json`.

**Alpha prerelease quick start (`2026.3.7-alpha-2`).** After the GitHub release is published and its assets are ready, run this **single shell command from the Magento checkout**. Choose `--client codex` (default), `--client claude`, or `--client stdio` to print configuration for another MCP client.

```sh
installer=$(mktemp) && curl -fLsS 'https://github.com/magento/magento2-phpstorm-plugin/releases/download/2026.3.7-alpha-2/magento-hce.pyz' -o "$installer" && python3 "$installer" setup --client codex
```

This command downloads the installer from **GitHub Releases**. The installer then verifies and downloads the separately packaged HCE plugin from that same GitHub release. **HCE downloads are not counted as JetBrains Marketplace downloads.** The desktop IDE plugin is distributed via [JetBrains Marketplace](https://plugins.jetbrains.com/plugin/8024-magento-2-and-adobe-commerce) (alpha channel: `https://plugins.jetbrains.com/plugins/alpha/8024`); it is a different plugin and cannot replace the HCE archive. Marketplace download counting for HCE requires a separately approved Marketplace listing for plugin ID `com.magento.idea.magento2plugin.hce` and changing release packaging to use the installer's supported `--marketplace` download source.

**Branch preview.** The HCE host-package job produces the two files as separate artifacts. Extract both to the same directory and run this from the Magento checkout. A maintainer can also build the same files locally using the commands below.

```sh
python3 /path/to/magento-hce.pyz setup --client codex --plugin-file /path/to/magento-hce.zip
```

Setup verifies the JetBrains runtime and plugin archive against the installer's SHA-256 pins. It presents the bundled JetBrains agreement and offers terminal sign-in if needed. A provisioned host can specify `--runtime /path/to/intellij-server-2027.1`, `--license-dir /path/to/store`, and, after reviewing that build’s `EULA.txt`, `--accept-eula`. With an existing runtime, setup checks its product and build. See `setup --help` for all options.

Setup imports and indexes the checkout, then resolves a real module through the plugin before registering the client. Use `--check-module Vendor_Module` to choose the verification target. Open or restart the client in this checkout and trust the project or approve its MCP entry when prompted. Try **“Which modules depend on Magento_Catalog? Show the declaration paths.”**

Setup adds the `magento-hce` entry to `.codex/config.toml` or `.mcp.json`. Its launcher path belongs to your machine. Keep it local when sharing project configuration. Existing client entries and JPS module definitions are preserved. For checkouts without `.idea/modules.xml`, setup creates that file and `.idea/magento-hce.iml` to index the project root.

Downloads are shared per OS user under `${XDG_DATA_HOME:-~/.local/share}/magento-hce`. Each canonical checkout has its own settings, index and log directory. Tool discovery uses the catalog verified during setup. The first tool call opens HCE and subsequent calls reuse that process. One active engine can use a checkout’s index at a time. Close that client session before using another client or repeating setup. Repeating the command reuses verified packages and the index. To uninstall, remove the project’s `magento-hce` client entry and its project directory under the data directory. Remove the shared data directory only after retiring all its checkouts.

## Current tools

| Tool | Behavior |
| --- | --- |
| `configure_magento_project` | `action=status` (default) reads settings; `action=help` describes supported settings; `action=configure` explicitly persists root, enabled state, and/or CLI wrapper candidates. |
| `get_magento_root_path` | Reads the configured absolute root. Reports an unconfigured project explicitly. |
| `describe_magento_cli_environment` | Uses the shared Magento discovery service for wrappers, Compose priority, working directories, and DDEV configuration/overrides. Requires enabled integration and a configured root. |
| `magento_maintenance` | File evidence and explicitly requested runtime verification for Composer, ECE patches, database setup, and cache types. Returns affected items, reasons, commands and working directories. |
| `magento_diagnostics` | Explicit file-scoped checks using selected existing XML inspections; reports rules, severity, explanations, and source ranges. |
| `magento_inspect` | Shared Magento XML analysis: `help`, `detailed_schema`, and `query`; available when the host has XML support. |
| `magento_intelligence` | Lists the tools actually available in this host. |
| `magento_skills` | Read-only status/help and explicit bundled skill installation, without requiring Magento setup. |
| `magento_magerun` | Read-only candidate discovery and bounded version/list/help metadata probes after Magento setup. |

## Skills and Magerun

`magento_skills` reuses the desktop installer and packaged `magento-inspect`,
`magento-magerun`, and `magento-scaffold` instructions. Status never writes files.
Installation requires explicit skill names and supports `project`, `agents`,
`codex`, and `claude` targets under the opened project root. Customized files are
preserved unless `overwrite=true`; identical files remain unchanged. Symlink
boundaries and the complete installation plan are validated before writing.
The tool returns suggested instruction links and never edits `AGENTS.md`.
Installing instructions does not add unavailable host capabilities: PHP analysis,
upgrade compatibility, and scaffolding remain unavailable in HCE.

```json
{"mode":"install","skills":["magento-inspect","magento-magerun"],"agentTarget":"agents"}
```

`magento_magerun` reuses the existing candidate/wrapper/DDEV discovery, invocation
planning, process runner, JSON metadata parser, pagination, and cache. `help` and
`status` do not launch processes. `commands`, `command_help`, and `mcp_config` may
run bounded version/list/help probes against the selected installed runtime.
They never run application commands, start containers/services, install or update
Magerun, or fall back to another runtime. `mcp_config` returns configuration only;
it neither writes settings nor starts an MCP server. Unsupported wrappers and
unavailable/invalid runtime output have explicit statuses.

```json
{"mode":"commands","search":"sys","limit":10}
```

Metadata caches are separate by canonical project root, bounded to 16 projects
per engine process, expire after five minutes, and invalidate on relevant local
launcher/configuration/Composer changes. `refresh=true` clears cached metadata.
The adapter reloads saved integration settings on every call.

## XML analysis

`magento_inspect` reuses the existing inspection catalog, argument validation,
module/layout indexes, DI/event snapshots, UI component and ACL/menu queries,
XML PSI, cache invalidation,
bounded result formatting, and cancellation/index-readiness handling. The HCE
adapter loads saved settings into each analysis context and supplies the actual
host query capabilities. No alternate XML parser or duplicated Magento index is
introduced. Results use project-relative declaration paths and cover the opened
project's indexed scope, not an effective Magento configuration merge.

Supported query types: `module`, `layout_entities`, `di_config`,
`observers_for_event`, `ui_component`, `ui_template`, `acl_or_menu`, and (when native JSON PSI is loaded) `module_dependencies`. Discover one schema before calling the query, for example:

```json
{"mode":"detailed_schema","queryType":"di_config"}
```

```json
{"mode":"query","queryType":"di_config","parametersJson":"{\"className\":\"Magento\\\\Theme\\\\Block\\\\Html\\\\Topmenu\"}"}
```

`module_dependencies` answers which modules/packages a module declares and which
modules refer to it, using the existing module index, native XML/JSON PSI, and
PSI/root modification tracking for cached snapshots. It requires the runtime's
optional `com.intellij.modules.json` plugin; without it the query is omitted from
the HCE catalog. JSON PSI comes from the runtime; no IDE classes are bundled.

```json
{"mode":"query","queryType":"module_dependencies","parametersJson":"{\"moduleName\":\"Vendor_Module\",\"direction\":\"both\",\"limit\":20}"}
```

The JSON response separates `sequence_after` ordering from `composer_require`
and `composer_require_dev` package declarations. It preserves duplicate module
declarations and returns native source ranges, raw version constraints, counts,
limits, and metadata problems. Package-to-module candidates require an explicit
`composer.json` directly at a module root. Parent/multi-module package ownership,
Composer solving, enabled/installed state, transitive closure, effective load
order, and PHP validation are outside coverage. Missing module-root manifests
are reported as `absent`; external/platform requirements remain package edges.
`partial`/`complete=false` means a scan/result limit or malformed metadata prevents
a complete answer; `module_not_found` can still carry inbound sequence references.
Indexing readiness and unavailable capabilities remain distinct host responses.

The runtime contract suite consumes the same
`src/test/resources/mcp/module-dependencies-query.json` as the desktop HTTP tests.
It checks both directions, declaration/package ambiguity, source ranges,
validation/truncation, missing declarations/manifests, and XML/JSON live updates
through the runtime's native high-watermark barrier. Run it with the build and
runtime-contract commands above; it needs no PHP runtime or external service.

`ui_template` resolves a literal template alias through the same shared resolver
as desktop XML navigation:

```json
{"mode":"query","queryType":"ui_template","parametersJson":"{\"templatePath\":\"Vendor_Module/widget\"}"}
```

Discover its schema before querying. `text!` prefixes and `.html` suffixes are
normalized. Results contain sorted file paths, `normalizedPath`, candidate counts,
and explicit truncation after 20 files. Missing modules/templates return a distinct
no-match result; incomplete indexing and cancellation remain separate states.
The search covers indexed scope and returns file candidates, without selecting
the effective theme, active area, RequireJS maps, or runtime configuration. No
JavaScript/PHP plugin is required, and queries do not change project files.

The HTTP and HCE transport contracts use the same
`src/test/resources/mcp/ui-template-query.json` fixture for module, vendor, theme,
library, alias and missing-file cases. They also verify bounded results and live
creation/rename/deletion. Index readiness and cancellation are exercised under
controlled conditions in shared desktop tests; HCE live updates use the engine's
indexing barrier.

Module lookup returns exact/fuzzy declaration names, module directories, editable
status, and `etc`/`view` paths. Layout lookup covers handles, block declarations
(including class/template attributes), and container declarations (including HTML
attributes), with project-relative file paths. These are indexed declarations;
references, effective layout merging, and PHP class validity are not inferred.

HCE registers the existing `ModuleXmlIndex`, `BlockNameIndex`, and
`ContainerNameIndex`. Its global key enumeration does not include arbitrary
custom indexes in the tested build. The adapter uses public `FilenameIndex` and
`FileBasedIndex.getFileData` APIs to enumerate the existing per-file index data;
query matching, scope filtering, declaration lookup, and output remain shared.
No analyzer internals, private allowlist mutations, or replacement indexes are
used. The host's internal analysis settings keep these static indexes populated;
query access still checks the durable project configuration. Enumeration visits
matching XML files, so cost grows with indexed project size; production Magento
project performance has not yet been benchmarked.

After on-disk updates, use the engine's `wait-for-highwatermark` command before
querying to wait for indexing. Live contracts verify renamed modules, blocks,
and containers, alongside handles, missing matches, and clean shutdown.
PHP queries remain unavailable.
Unavailable queries are omitted from the catalog and explicitly rejected rather
than returning no matches. Declaration results do not validate PHP classes,
inheritance, Magento merge order, or application runtime behavior. Live-update
contracts run in the actual HCE process. Controlled dumb-mode behavior is checked
in the shared PhpStorm tests; it is not claimed as an observed HCE transition.

Configure with, for example:

```json
{"action":"configure","magentoPath":"src","enabled":true,"cliToolCandidates":["bin/magento","bin/php","bin/composer"]}
```

Paths resolve inside the opened project, including symlink checks. The module
does not claim a directory is a verified Magento installation. Responses are JSON
text with `status` and `successful`; inspect them even when the transport's
`isError` is false. Disabled support, missing settings, invalid configuration,
storage errors, and unsupported capabilities are distinct states.

CLI environment discovery reads files only. It does not execute wrappers, PHP, Docker, DDEV, or
Magerun, start services, probe container state, or install skills. DDEV settings
are a bounded static view; secret environment entries and hooks are not exported.
Indexing exclusions are not implemented here: passing `excludeGeneratedFiles`
returns `unsupported_capability` without changing settings.

## Maintenance checks

After a setup patch edit, pass its path relative to the configured Magento root.

```json
{"mode":"check","checkIds":["setup_upgrade"],"changedFiles":["app/code/Acme/News/Setup/Patch/Data/AddFeed.php"]}
```

When runtime verification is requested, repeat with `mode="verify"`. With no `checkIds`,
verification follows Composer → ECE patches → setup → cache. Resolve an upstream finding
and verify again to obtain a fresh downstream observation. Use `devMode="no-dev"` to compare
a production installation. Commands in findings are suggestions and include the selected working directory.

The configured checkout supplies file evidence. Verification supports local tools, DDEV and
recognized Mark Shust wrappers using `bin/clinotty`. An unavailable runtime or unsupported ECE
status format produces an incomplete observation. On the first observation of a changed
`app/etc/config.php`, use `newlyEnabledModules` to supply additions from the deployment diff.

## XML diagnostics

Discover `magento_diagnostics` with `mode=help`, then check one project-relative
XML file. This runs the existing local inspection visitors, including their
messages and XML suppression, without applying quick fixes or changing files.

```json
{"mode":"check","filePath":"src/app/code/Foo/Bar/view/frontend/layout/default.xml","limit":25}
```

The initial rules use the desktop inspection IDs:

- `ModuleDeclarationInModuleXmlInspection` (error): declaration name disagrees
  with the editable module directory. Vendor modules and dependency sequence
  declarations retain the existing inspection's exclusions.
- `CacheableFalseInDefaultLayoutInspection` (warning): `cacheable="false"` on
  blocks/reference blocks in `layout/default.xml` affects full-page caching.

An optional `ruleIds` array selects rules; unknown rules are rejected. Responses
include applicable `ruleIds`, `skippedRules`, messages, explanations, project-relative
paths, and ranges with 1-based line/column and UTF-16 offsets (exclusive end).
Severities are fixed defaults; IDE profiles and quick fixes are not exposed.
Coverage is limited to these rules, without schema, PHP, merge-order or runtime
validation. A clean result applies only to the reported rule/file scope.

`status=ok` and `complete=true` describe a completed check. Truncated results use
`status=partial`, `complete=false`, and `truncated=true`. Indexing, cancellation,
malformed XML, unavailable/out-of-scope files, and invalid arguments have explicit
incomplete states. The check is bounded to 1 MiB, 50,000 PSI elements, and 1,000
collected findings; `limit` controls the returned findings (1..100, default 25).
There is no automatic whole-project scan. After on-disk changes, wait for the
engine's `wait-for-highwatermark` barrier as with other XML queries.

Runtime contracts verify both rules, positive/negative fixtures, exact ranges,
read-only behavior, bounds, suppression, and live updates. Shared
PhpStorm tests additionally inject cancellation and controlled dumb mode;
these states are not claimed as forced transitions in the real HCE process.

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

One GitHub release job verifies the desktop plugin, packages HCE with the same version, attaches the desktop ZIP, HCE ZIP and installer to the same tag, and submits the desktop plugin to Marketplace. To reproduce the installer for a particular release, pass its actual tag.

```sh
python3 modules/hce/package_launcher.py --plugin modules/hce/build/distributions/magento-hce.zip --github-repository OWNER/REPO --release-tag TAG --output modules/hce/build/distributions/magento-hce.pyz
```

For an approved HCE Marketplace listing, use `--marketplace` in place of `--github-repository OWNER/REPO --release-tag TAG` and supply the exact ZIP served by Marketplace. The installer then fetches that version through JetBrains’ download API on a cache miss. The current plugin XML ID is `com.magento.idea.magento2plugin.hce`. The pilot uses GitHub release assets. Attribution to desktop listing 8024 would require a publishing arrangement with JetBrains.

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
clean shutdown. XML contracts exercise the shared catalog/schemas, DI, observers,
UI component and ACL/menu queries, module/layout lookups and live index updates,
unavailable capabilities, invalid arguments,
source paths, and an on-disk observer update. Skill contracts cover discovery,
installation, customized-file preservation, explicit overwrite, and unchanged
project instructions. Magerun contracts execute a deterministic Python container
launcher fixture through the real shared process runner, using existing captured
10.1.0 JSON metadata. They check read-only status, command/help/config responses,
cache reuse/refresh/file invalidation, rejected arguments, and unsupported-wrapper
nonexecution. This is not a test of a live PHP/Magerun installation or Docker/DDEV
service. The settings/environment discovery fixtures never execute their wrappers.

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

General IDE inspections beyond the two scoped XML rules, PHP semantics,
JavaScript semantics, GraphQL, upgrade compatibility, and command execution are
separate capabilities; this implementation does not advertise them. The historical
exploratory probes and report are kept outside the repository at the user's request.
