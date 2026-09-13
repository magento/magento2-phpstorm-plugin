---
name: magento-inspect
description: Inspect Magento 2 or Adobe Commerce project configuration and analyze backward or upgrade compatibility through the JetBrains Magento MCP tools. Use magento_inspect for configuration lookups and magento_compatibility for the built-in UCT PHP/XML analysis.
---

# Magento Inspect

Use this skill when the user asks to inspect, find, locate, or summarize Magento or Adobe Commerce project configuration in an opened JetBrains IDE project.

For backward compatibility or upgrade analysis, use the compatibility workflow below.

## Compatibility Analysis

1. Call `magento_compatibility` with `mode: "detailed_schema"` and `parametersJson: "{}"`, then `mode: "status"` with the same empty parameters. Status reports indexing readiness, configured versions, active runs, and the targets covered by the bundled compatibility indexes.
2. Select the requested module or path and target version. Use `mode: "analyze"` with `parametersJson` containing exactly one of `moduleName` (exact `Vendor_Module`) or `path` (project-relative or absolute), and `targetVersion`. Optional parameters are `currentVersion`, `minimumSeverity`, and `ignoreCurrentVersion`; consult the schema for defaults.
3. Keep the returned `runId` and call `mode: "results"` with it. Allow time between checks while the run is `running`. When `completed`, use `nextOffset` until `hasMore` is false to retrieve all findings. File locations are project-relative with 1-based lines and columns. Use `mode: "cancel"` with the runId when the user stops the analysis or the run is no longer needed.
4. Report the versions, scope, completion state, severity totals, and relevant findings. A failed, cancelled, or incomplete run is not a clean compatibility result. An unsupported target or missing index data requires a coverage explanation; do not silently choose an older target. Zero findings covers only the shipped UCT rules and data, not every possible upgrade issue.

The tool runs the plugin's built-in PHP/XML inspections without saving files or changing UCT settings. It can run when editor UCT inspections are disabled, provided Magento project support is enabled and indexing is ready. It does not install or invoke the external Adobe UCT CLI. Directory scans discover custom modules/themes and exclude bundled components; use a PHP/XML file path for a focused check. Runs belong to the current IDE project and the latest five are retained until it closes.

For example, after confirming the target is covered:

```json
{"mode":"analyze","parametersJson":"{\"moduleName\":\"Foo_Bar\",\"targetVersion\":\"2.4.3\",\"minimumSeverity\":\"warning\"}"}
```

The version in this example is illustrative; use the user's target and the coverage returned by `status`.

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
