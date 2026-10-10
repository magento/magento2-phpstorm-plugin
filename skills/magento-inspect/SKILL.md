---
name: magento-inspect
description: Inspect Magento 2 or Adobe Commerce project configuration and analyze backward or upgrade compatibility through the JetBrains Magento MCP tools. Use magento_inspect for configuration lookups and magento_compatibility for the built-in UCT PHP/XML analysis, including PHTML and HTML templates.
---

# Magento Inspect

Use this skill when the user asks to inspect, find, locate, or summarize Magento or Adobe Commerce project configuration in an opened JetBrains IDE project.

For backward compatibility or upgrade analysis, use the compatibility workflow below.

## Compatibility Analysis

1. Call `magento_compatibility` with `mode: "status"`, the intended `projectPath`, and the user's requested `targetVersion`. Include explicit `currentVersion` and `ignoreCurrentVersion` if the analysis requires them. Verify returned project identity. `readiness` separates `ideReady`, `releaseDataReady`, and overall `ready`; `missingVersions` lists required release data. `supportedVersions`/`latestSupportedVersion` describe local index coverage, not published releases. If the target is relative (such as “next minor”), use `mode: "releases"` with the intended `projectPath` and optional `currentVersion` first. It uses **Published releases URL** in Magento settings (official GitHub catalog by default) and returns published stable releases, dates, source links, cache age, and `nextMinorRelease`; follow its status continuation for the selected target. Do not replace an explicitly requested target with that recommendation. Effective version/filter options appear in `defaults`. A connection mismatch requires correcting MCP configuration and refreshing discovery. Do not create another IDE instance or a client script.
2. If release data is missing, follow `nextCall` to `mode: "prepare"`. It fetches the exact official Magento Open Source tag and builds compatibility snapshots in the IDE cache. Missing coverage does not prove the tag exists: preparation can fail with HTTP 404, so inspect the failure instead of repeatedly retrying or substituting a different release. Preserve the returned `analysisTargetVersion`, `currentVersion`, and `ignoreCurrentVersion` arguments through polling; preparation completion returns a status `nextCall` with the original analysis target/baseline options. Preparation reports `operation: "prepare"`, phase, downloaded bytes, and processed files. A completed preparation covers that release only; do not substitute a prepared baseline for the requested upgrade target. Poll running preparation using its returned `nextCall` and respect `retryAfterMs`. Cached releases return `complete: true`, `runId: null`, and a status `nextCall`; no polling or new download is needed. Do not invoke Composer or write client scripts.
3. When `readiness.ready` is true, call `mode: "analyze"` with exactly one of `moduleName` (exact `Vendor_Module`) or `path` (relative to returned `pathBase`, or absolute; `pathBase` can differ from `magentoRoot`), plus the original `targetVersion` and baseline options. `currentVersion` defaults to the installed version in the configured Magento root's `composer.lock`; there are no configured-version or JSON-string fallbacks. Optional `minimumSeverity` is warning/error/critical. A prepared target requires covered baseline data when a baseline is provided or detected.
4. Follow `nextCall.tool` and `nextCall.arguments` to poll results and retrieve remaining pages. Respect `retryAfterMs` while running. Continue until terminal and `hasMore` is false. File locations are project-relative with 1-based lines/columns. `mode: "cancel"` with `runId` stops either preparation or analysis; preserve preparation continuation options when available. Failed requests/jobs set MCP `isError: true`. Parse JSON text when `structuredContent` is absent: JetBrains omits structured output for errors and may disable it globally. Host argument-type and project-routing errors can be plain text; correct them before retrying. Send `offset`/`limit` as JSON integers; the host also coerces numeric strings, while fractions and overflow fail. Validation errors identify rejected parameters and accepted values, with `nextCall: null`; correct and retry the original request. A failed, cancelled, or incomplete run is not a clean result; inspect its state-specific guidance. Failed preparation never publishes ready data. Default findings include existing baseline issues; use `ignoreCurrentVersion: true` to suppress them when both releases are covered. Add `explainSuppressed: true` when baseline suppression counts by rule/severity would help explain the result; counts are taken before per-element severity filtering.
5. Report versions, source coverage, scope, completion state, severity totals, and relevant findings. Use `scope.fileTypeCounts` and `scope.testFiles` to describe coverage. Save `analysisIdentity` alongside findings: its engine revision, index fingerprint and snapshot checksums distinguish data changes from inconsistent scans. Prepared snapshots cover Magento Open Source core; proprietary Commerce extensions, third-party dependencies, generated code, and runtime behavior are excluded. Prepared change labels describe the observed release, not the first release introducing a change. Zero findings covers only the existing UCT rules and available data.

Analysis runs the plugin's built-in PHP/XML inspections, including PHTML and HTML templates, without saving files or changing UCT settings. It can run when editor UCT inspections are disabled, provided Magento support is enabled and IDE indexing is ready. Preparation can run while IDE indexing continues. The tool does not install or invoke the external Adobe UCT CLI. Directory scans discover custom modules/themes and exclude bundled components, but include test/fixture files inside those components; use a PHP, PHTML, XML, or HTML file path for a focused check. Scope support and processed-file counts follow IDE PHP/XML PSI types. Unsupported single files are rejected before a job starts; unsupported/empty directory scopes fail during background discovery. A running response is not proof that the scope can be analyzed. Retrieve and save completed findings before starting more analyses. Only the latest five jobs of each operation belong to the current project until it closes; prepared data persists in the IDE system cache across restarts. Rejected cache entries are reported in `cacheWarnings`; prepare the affected release again.

Use the user's requested release as `targetVersion`. Replace `<requested-version>` with that exact release in these examples:

```json
{"mode":"status","targetVersion":"<requested-version>","ignoreCurrentVersion":true}
```

Follow preparation calls, repeat this status request until ready, then analyze the requested scope:

```json
{"mode":"analyze","moduleName":"Foo_Bar","targetVersion":"<requested-version>","ignoreCurrentVersion":true}
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
