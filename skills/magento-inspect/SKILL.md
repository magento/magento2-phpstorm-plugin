---
name: magento-inspect
description: Inspect Magento 2 and Adobe Commerce projects through desktop or HCE MCP. Use for source lookups, module dependencies, XML diagnostics, project setup, and deciding whether Composer install, Cloud patches, setup upgrade, or targeted cache cleaning is warranted after edits. Includes upgrade compatibility analysis on supported desktop hosts.
---

# Magento Inspect

Start with `magento_intelligence` in the intended project. Request one tool's schema with `tool` set to its exact name. Use the invocation it advertises. On desktop, a router entry takes `execute_tool` with the tool name and `--argument value` pairs. Pass arrays and objects as JSON. HCE uses the opened project. Only pass `projectPath` where the host advertises it.

## Maintenance after edits

Use `magento_maintenance` on HCE, IntelliJ IDEA, WebStorm, or PhpStorm when a change may affect the installed application. Its arguments are typed fields. For file evidence use `mode: "check"` and supply the actual diff paths relative to the **Magento root**. Include deleted paths and both paths of a rename. With Magento in `src/`, a project file at `src/app/code/Foo/Bar/etc/db_schema.xml` is passed as follows.

```json
{"mode":"check","changedFiles":["app/code/Foo/Bar/etc/db_schema.xml"]}
```

Use `newlyEnabledModules` for names enabled in the `app/etc/config.php` diff, especially on the first observation. The service can also compare successive observations of the module list. Include a `checkIds` array to limit the scope to the question. Omit it to evaluate all four checks in dependency order. Select `devMode: "no-dev"` for a Composer installation intended to exclude development packages. The default is `"dev"`.

For an explicit request to verify a data patch in the selected runtime, keep the relevant changed paths and narrow the probe.

```json
{"mode":"verify","checkIds":["setup_upgrade"],"changedFiles":["app/code/Foo/Bar/Setup/Patch/Data/AddColor.php"]}
```

| Check ID | How to interpret the evidence |
| --- | --- |
| `composer_install` | Compare a current lock with installed package names, versions, and references in the selected runtime's effective vendor directory. A metapackage can have no install path and a container can own the entire vendor tree. A missing or stale lock calls for resolving the intended dependencies first. |
| `ece_patches` | Read `patchStatus` for each expected required Cloud patch, selected quality patch or dependency, and project hotfix. Identify a custom patch by its project-relative path when ECE gives it an `N/A` ID. A missing match, conflicting rows, or an `N/A` status leaves the patch unknown. Unselected optional patches need no action. Report the checked environment. |
| `setup_upgrade` | A schema, setup patch, legacy setup version, or enabled-module change warrants checking. Report a pending patch or schema difference only when the validator identifies it. Exit 2 can also mean a validator threw. A DB connection failure leaves the result unknown. A DB ahead of code calls for deployment alignment. Report newly enabled modules separately from the modules the validators inspected. |
| `cache_clean` | Runtime evidence identifies enabled invalidated cache type IDs, including custom types. A layout XML, `etc/config.xml`, or translation CSV edit can still produce a source advisory for `layout`, `config`, or `translate` when runtime invalidations are empty. Storefront layout changes also advise cleaning `full_page` when the affected response is cached. For Admin-only layout changes, the source advisory is scoped to `layout`. `cache:status` reports enablement. Omit the clean command when there are no identified IDs. |

Read top-level `status` for request errors. For successful requests, report each finding's `checkId`, `reasonId`, affected `item` or `module`, `evidence`, `environment`, and `nextAction`. Read `status` and `complete` on each finding. `required` identifies confirmed work, `advisory` supplies a reason to investigate or a conditional action, `clear` applies to what was checked, and `unknown` marks inconclusive or blocked work. A file check returning `not_applicable` / `no_file_signal` only describes the supplied changes. An ordinary PHP edit alone gives no reason to recommend `setup:upgrade`.

Present a `suggestedCommand` using its exact `argv` and `cwd`. Run a remediation only when the user requests that action. If several checks fire, resolve Composer first, recheck patches against the installed code, then verify setup. After setup, reassess caches before suggesting a clean. For a runtime-only check, omit already handled paths so their old file advisories do not recur. In the desktop status bar, open **Magento Maintenance** to see findings grouped by check and item. **Verify runtime** requests fresh evidence. **Mark reviewed** dismisses the tracked changes.

## Source inspection and XML diagnostics

1. Call `magento_inspect` with `mode: "help"` for the current query list, then `mode: "detailed_schema"` and one `queryType`. Submit `mode: "query"`, that `queryType`, and `parametersJson` as a JSON **string**.
2. Choose from the queries the host lists. `module`, `di_config`, `observers_for_event`, `layout_entities`, `ui_component`, and `acl_or_menu` locate declarations. Use the component base name, such as `product_form`, and cite the returned source locations. A source match does not establish effective merged configuration.
3. For `module_dependencies`, use an exact `moduleName`, optional `direction` (`dependencies`, `dependents`, or `both`) and a `limit` of 1–100. Distinguish XML sequence ordering from Composer requirements and development requirements. Read completeness, problems, truncation, and edge source ranges. Module-root package manifests can be missing or ambiguous. Enabled state and version satisfaction require separate evidence. HCE also requires JSON PSI for this query.
4. For `ui_template`, pass a literal `templatePath` such as `Foo_Bar/widget`. Report candidate HTML files and coverage. Theme fallback and RequireJS mapping can affect runtime selection.
5. Use `plugins_for_method` only when advertised on a PHP-enabled desktop. Supply the class FQN with escaped backslashes and the bare method name such as `save`.

```json
{"mode":"query","queryType":"module_dependencies","parametersJson":"{\"moduleName\":\"Foo_Bar\",\"direction\":\"both\",\"limit\":20}"}
```

For area-specific observer configuration, pass `area` to `observers_for_event`. For merged method-plugin configuration, pass `effectiveArea` to `plugins_for_method`. On a method-plugin query, `area` alone filters declaration files. Both queries return declarations by default. Set `format` to `"json"` inside `parametersJson` for source records. Read `status` and `decidedBy` to distinguish applicable, replaced, and disabled entries. If the module list in `app/etc/config.php` is missing or nonliteral, report the entries as candidates.

For file diagnostics, use `magento_diagnostics` with `mode: "help"` to discover rule IDs, then `mode: "check"` with one **IDE-project-relative** `filePath`. With Magento under `src/`, retain that prefix here. The module-declaration and default-layout cacheability rules report source ranges. Read applicability, readiness, completeness and truncation before interpreting an empty list. After external disk edits on HCE, use the engine's `wait-for-highwatermark` command before an indexed query when the client exposes the LSP command.

```json
{"mode":"check","filePath":"src/app/code/Foo/Bar/view/frontend/layout/default.xml"}
```

## Project setup, skills and runtime

- Read existing project instructions. Use `configure_magento_project` with `action: "status"` to check integration and the configured root. For requested setup, configure `enabled: true` and `magentoPath` (e.g. `.` or `src`). Use `get_magento_root_path` for a root-only lookup. Read the host's readiness response before indexed queries.
- On desktop, discover `get_magento_indexing_exclusions` or preview changes using `configure_magento_indexing_exclusions` with `action: "preview"`. Use apply, update or reset only for requested changes. Desktop setup enables generated/runtime exclusions by default and accepts `excludeGeneratedFiles: false` to opt out. Omit indexing-exclusion arguments on HCE. An excluded path can still exist on disk.
- Use `magento_skills` with `mode: "status"` and the intended `agentTarget` to compare local copies with the bundle. For a requested installation use `mode: "install"` with an explicit `skills` array. Targets are relative to the IDE project root: `project` writes to `skills/`, `agents` to `.agents/skills/`, `codex` to `.codex/skills/`, and `claude` to `.claude/skills/`. Review differences before explicitly choosing `overwrite: true`. Read each installation result. If updating project instructions is part of the request, merge the returned links into the existing `AGENTS.md`.
- For manual CLI work, call `describe_magento_cli_environment` and use the preferred invocation from its reported working directory. For installed n98-magerun2 command discovery, use `magento_magerun` status, commands and command_help. See [magento-magerun](../magento-magerun/SKILL.md) when installed for pagination, wrapper failures and native MCP setup. Maintenance verification uses its own probes and needs no Magerun discovery.

- For DDEV, check `configuration.status`, then use the reported `execPrefix` and `cwd` for container commands. `projectCommands` lists project-defined commands from `.ddev/commands`, with the service, description, usage, examples, and invocation. Use a listed project command's `invocation` after the user authorizes running it.

## Upgrade compatibility on a PHP-enabled desktop

Use `magento_compatibility` for source compatibility with a Magento release. For a question about whether the current database needs an upgrade, use the `setup_upgrade` maintenance check. Pass the absolute IDE project directory as `projectPath` on every compatibility call, including help. Use typed arguments.

### Choose a target and scope

For a known scope, start with `mode: "upgrade"` and exactly one `moduleName` or `path`. Supply the exact release the user requested. Omit `targetVersion` to select the next published feature release. A response with `outcome: "no_newer_release"` means no scan started. The preset freezes the baseline and target, suppresses existing baseline issues and explains the suppression by default.

```json
{"projectPath":"<IDE-project-directory>","mode":"upgrade","moduleName":"Foo_Bar","targetVersion":"<requested-version>"}
```

Resolve relative paths from the returned `pathBase`. With Magento under `src/`, a scope can be `src/app/code/Foo/Bar`. For an invalid path, inspect `error.pathBase` and `error.suggestedPath`. Directory scans discover custom modules and themes and include their tests. Use a PHP, PHTML, XML, or HTML file for a focused scan. Scans inherit the project's **UCT Exclude Paths**. To override them for this scan, pass `excludedPaths` with absolute or `pathBase`-relative paths. An empty array includes paths skipped by that setting.

### Follow the returned workflow

- Verify project identity. `status` reports local index coverage, the detected baseline from the configured root's `composer.lock`, and IDE and release-data readiness. Read `readiness.ready` before analysis. For release discovery, `releases` uses the configured **Published releases URL** and reports the next feature release. Set `includeReleases: true` only when the full catalog is needed.
- Follow each non-null `nextCall` verbatim. It carries project, scope, target, baseline, exclusions and severity filters through preparation, analysis and pagination. For `nextStep: "poll"`, wait `retryAfterMs`. For `"call"`, continue immediately, including after a completed job that still has a next page. `successful`, `complete` and `terminal` describe the response or job. An unscoped ready status needs a scope. For `select_target`, `resolve_error` or `enable_support`, address the returned reason.
- `prepare` fetches the exact Magento Open Source tag into the IDE cache. Check its phase, progress or failure. A cached preparation can return `runId: null` and a continuation. Retain the original `analysisTargetVersion` even if a baseline release needs preparation. Check `cacheWarnings` for rejected entries. If the requested tag is unavailable, report it.
- `analyze` runs the built-in PHP/XML inspections, including templates, against committed IDE documents. Integration must be enabled and indexing ready. The editor's UCT inspections can be disabled. Check the final outcome and processed-file counts. Use `results` with the returned `runId` for pages and `cancel` for a requested cancellation.

### Report what the scan covered

Report the baseline, target, scope, severity totals and findings with project-relative locations and 1-based lines and columns. Use `scope.fileTypeCounts` and `scope.testFiles`. Read `findingsScope` for upgrade-only or target-including-baseline results. For an analysis outside the preset, `ignoreCurrentVersion: true` requests baseline suppression and `explainSuppressed: true` returns its counts. `minimumSeverity` accepts warning, error or critical. Suppression counts raw diagnostics before per-element severity filtering, so they can differ from displayed-finding counts.

Keep `analysisIdentity` with the report when comparing results. `releaseSnapshotSha256` identifies release state, `comparisonSnapshotSha256` includes baseline symbols, `archiveSha256` identifies source bytes, and `indexRevision` includes suppression options. Terminal reports include all findings. Check `report.saved` and `report.error` before relying on later retrieval by run ID.

Prepared coverage is Magento Open Source core. A clean scan applies to the inspected files, selected rules and available release data. Proprietary extensions, third-party dependencies, generated code and runtime behavior require separate evidence.
