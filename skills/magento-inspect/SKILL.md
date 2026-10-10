---
name: magento-inspect
description: Inspect Magento 2 or Adobe Commerce project configuration and analyze backward or upgrade compatibility through the JetBrains Magento MCP tools. Use magento_inspect for configuration lookups and magento_compatibility for the built-in UCT PHP/XML analysis, including PHTML and HTML templates.
---

# Magento Inspect

Use this skill when the user asks to inspect, find, locate, or summarize Magento or Adobe Commerce project configuration in an opened JetBrains IDE project.

For backward compatibility or upgrade analysis, use the compatibility workflow below.

## Compatibility Analysis

For an upgrade check with a known scope, prefer `mode: "upgrade"` with the intended `projectPath` and exactly one `moduleName` or `path`. Omit `targetVersion` only when the user wants the next published feature release; provide the exact version otherwise. This preset defaults `ignoreCurrentVersion` and `explainSuppressed` to true, freezes the detected baseline and selected target, and returns the full scope/filter options in each `nextCall`. Follow `nextStep`/`nextCall` through preparation, readiness and analysis without reconstructing requests. `outcome: "no_newer_release"` means discovery found no newer feature release and no scan started. Verify project identity before continuing. Use the individual modes below when inspecting readiness, catalogs, or all target findings.

1. Call `magento_compatibility` with `mode: "status"`, the intended `projectPath`, and the user's requested `targetVersion`. Include explicit `currentVersion` and `ignoreCurrentVersion` if the analysis requires them. Verify returned project identity. `readiness` separates `ideReady`, `releaseDataReady`, and overall `ready`; `missingVersions` lists required release data. `supportedVersions`/`latestSupportedVersion` describe local index coverage, not published releases. If the target is relative (such as “next minor”), use `mode: "releases"` with the intended `projectPath` and optional `currentVersion` first. It uses **Published releases URL** in Magento settings (official GitHub catalog by default) and returns compact source/cache metadata, `releaseCount`, and dated `nextMinorRelease`; pass `includeReleases: true` only when the full catalog is needed; follow its status continuation for the selected target. Do not replace an explicitly requested target with that recommendation. Effective version/filter options appear in `defaults`. A connection mismatch requires correcting MCP configuration and refreshing discovery. Do not create another IDE instance or a client script.
2. If release data is missing, follow `nextCall` to `mode: "prepare"`. It fetches the exact official Magento Open Source tag and builds compatibility snapshots in the IDE cache. Missing coverage does not prove the tag exists: preparation can fail with HTTP 404, so inspect the failure instead of repeatedly retrying or substituting a different release. Preserve every returned argument through polling, including `analysisTargetVersion`, `currentVersion`, `ignoreCurrentVersion`, `path`/`moduleName`, `minimumSeverity`, and `explainSuppressed`; preparation completion returns a status `nextCall` with the original analysis target/baseline options. Preparation reports `operation: "prepare"`, phase, downloaded bytes, and processed files. A completed preparation covers that release only; do not substitute a prepared baseline for the requested upgrade target. Poll running preparation using its returned `nextCall` and respect `retryAfterMs`. Cached releases return `complete: true`, `runId: null`, and a status `nextCall`; no polling or new download is needed. Do not invoke Composer or write client scripts.
3. When `readiness.ready` is true, call `mode: "analyze"` with exactly one of `moduleName` (exact `Vendor_Module`) or `path` (relative to returned `pathBase`, or absolute; `pathBase` can differ from `magentoRoot`), plus the original `targetVersion` and baseline options. With Magento under `src/`, use `src/app/code/Vendor/Module`; an invalid relative path returns `error.pathBase` and, when detected, `error.suggestedPath` for an explicit retry. `currentVersion` defaults to the installed version in the configured Magento root's `composer.lock`; there are no configured-version or JSON-string fallbacks. Optional `minimumSeverity` is warning/error/critical. A prepared target requires covered baseline data when a baseline is provided or detected.
4. Use `nextStep` for the workflow decision. For `poll`, wait `retryAfterMs` then follow `nextCall`; for `call`, follow it immediately. Always follow a non-null `nextCall`, including blocked coverage, completed/cached preparation, and pagination, even when `terminal: true` or `pollRequired: false`. For `analyze`, supply the requested scope to the ready status; for `select_target`, obtain the exact target; for `resolve_error`, correct the reported error; for `enable_support`, enable Magento project support; `done` has no continuation. `successful` and `complete` describe the response/job, not readiness or the whole upgrade workflow. File locations are project-relative with 1-based lines/columns. `mode: "cancel"` with `runId` stops either preparation or analysis; preserve preparation continuation options when available. Failed requests/jobs set MCP `isError: true`. Every plugin reply, including help and errors, is one JSON object in `content[0].text`; `structuredContent` is consistently omitted. Help/schema text is in `documentation`. Host project-routing errors occur before the plugin and may still be plain text. Send `offset`/`limit` as JSON integers; the plugin also accepts numeric strings and returns JSON validation errors for fractions/overflow. Validation errors identify rejected parameters and accepted values, with `nextCall: null`; correct and retry the original request. Missing coverage has a distinct `coverage_required` error, `state: "blocked"`, `error.recoverable: true`, and a preparation continuation. Poll only when `pollRequired: true`; `terminal: true` covers completed, failed and cancelled jobs. `successful` and legacy `complete` mean success, never a polling condition. A failed or cancelled run is not a clean result. Terminal reports are automatically saved outside the project; `report` gives the JSON path, save errors and retention (30 days / 100 runs per operation). `results` can retrieve archived run IDs after memory eviction or restart, and exports contain every finding without pagination. Failed preparation never publishes ready data. Default findings include existing baseline issues; use `ignoreCurrentVersion: true` to suppress them when both releases are covered. Add `explainSuppressed: true` when baseline suppression counts by rule/severity would help explain the result; `suppression.suppressedDiagnostics` (legacy `total`) counts raw diagnostics before per-element severity filtering. `summary.displayedFindings` (legacy `totalIssues`) counts displayed findings, so the counts need not subtract evenly. `findingsScope` identifies `upgrade_changes` or `target_including_baseline`.
5. Report versions, source coverage, scope, completion state, severity totals, and relevant findings. Use `scope.fileTypeCounts` and `scope.testFiles` to describe coverage. Save `analysisIdentity` alongside findings: `releaseSnapshotSha256` identifies a release independently of the baseline; `comparisonSnapshotSha256` (legacy `snapshotSha256`) includes baseline symbols needed to detect removals and can change with a different baseline. `archiveSha256` identifies source bytes; `indexRevision` also includes suppression options. Compare matching request identities. Prepared snapshots cover Magento Open Source core; proprietary Commerce extensions, third-party dependencies, generated code, and runtime behavior are excluded. Prepared change labels describe the observed release, not the first release introducing a change. Zero findings covers only the existing UCT rules and available data.

Analysis runs the plugin's built-in PHP/XML inspections, including PHTML and HTML templates, without saving project source files or changing UCT settings. It can run when editor UCT inspections are disabled, provided Magento support is enabled and IDE indexing is ready. Preparation can run while IDE indexing continues. The tool does not install or invoke the external Adobe UCT CLI. Directory scans discover custom modules/themes and exclude bundled components, but include test/fixture files inside those components; use a PHP, PHTML, XML, or HTML file path for a focused check. Scope support and processed-file counts follow IDE PHP/XML PSI types. Unsupported single files are rejected before a job starts; unsupported/empty directory scopes fail during background discovery. A running response is not proof that the scope can be analyzed. Terminal results are automatically exported to JSON in the IDE system cache. Five jobs per operation remain in memory; reports remain accessible through `results` after eviction/restart for 30 days or the latest 100 archived jobs per operation. Check `report.saved` and `report.error`; exports include all findings. Prepared release data also persists across restarts. Rejected cache entries are reported in `cacheWarnings`; prepare the affected release again.

Use the user's requested release as `targetVersion`. Replace `<requested-version>` with that exact release in this example:

```json
{"projectPath":"<IDE-project-directory>","mode":"upgrade","moduleName":"Foo_Bar","targetVersion":"<requested-version>"}
```

Follow every returned continuation verbatim, including scope, severity, suppression, and original analysis target through preparation. For the next published feature release:

```json
{"projectPath":"<IDE-project-directory>","mode":"upgrade","moduleName":"Foo_Bar"}
```

## Preferred Flow

1. Use `magento_inspect` as the lookup entry point:
   - First call with `mode: "help"` to get only query names and short descriptions.
   - Then call with `mode: "detailed_schema"` and one `queryType` to load only that query's parameters and example JSON.
   - Then call with `mode: "query"`, the chosen `queryType`, and `parametersJson` as a JSON object string.

2. Use the returned result:
   - Prefer canonical module names, paths, class names, event names, handles, component names, ACL IDs, and menu IDs from the result.
   - If a shell command is needed after inspection, call `describe_magento_cli_environment` before running it.

## Query Types

Supported `queryType` values:

- `module`
- `di_config`
- `plugins_for_method`
- `observers_for_event`
- `layout_entities`
- `ui_component`
- `acl_or_menu`

## Rules

- Prefer `magento_inspect` over any finder-specific tool. Its staged modes exist to avoid loading the whole inspection library into context.
- Use PHP FQNs with escaped backslashes inside `parametersJson`, for example `Magento\\Catalog\\Api\\ProductRepositoryInterface`.
- Use bare method names for `plugins_for_method`, for example `save`, not `beforeSave`, `aroundSave`, or `afterSave`.
- Use UI component base names without `.xml`, for example `product_form`.

## Examples

Get the catalog:

```json
{"mode":"help","queryType":"","parametersJson":""}
```

Get plugin lookup schema:

```json
{"mode":"detailed_schema","queryType":"plugins_for_method","parametersJson":""}
```

Run a plugin lookup:

```json
{
  "mode": "query",
  "queryType": "plugins_for_method",
  "parametersJson": "{\"className\":\"Magento\\\\Catalog\\\\Api\\\\ProductRepositoryInterface\",\"methodName\":\"save\"}"
}
```
