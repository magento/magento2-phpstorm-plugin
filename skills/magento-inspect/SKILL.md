---
name: magento-inspect
description: Use when inspecting Magento 2 or Adobe Commerce project configuration through the JetBrains Magento MCP toolset, including modules, DI configuration, plugin interceptions, event observers, layout entities, UI components, ACL resources, and admin menu entries. Prefer the single `magento_inspect` tool with `help`, `detailed_schema`, and `query` modes instead of searching for many finder-specific tools.
---

# Magento Inspect

Use this skill when the user asks to inspect, find, locate, or summarize Magento or Adobe Commerce project configuration in an opened JetBrains IDE project.

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
