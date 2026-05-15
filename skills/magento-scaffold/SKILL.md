---
name: magento-scaffold
description: Use when generating Magento 2 or Adobe Commerce project scaffolding through the JetBrains Magento MCP toolset, including modules, plugins, observers, CRUD entities, controllers, CLI commands, blocks, view models, and product/category/customer EAV attributes. Prefer the single `magento_scaffold` tool with `help`, `detailed_schema`, and `render` modes instead of searching for many generator-specific tools.
---

# Magento Scaffold

Use this skill when the user asks to create Magento or Adobe Commerce code scaffolding in an opened JetBrains IDE project.

## Preferred Flow

1. Use `magento_scaffold` as the creation entry point:
   - First call with `mode: "help"` to get only scaffold names and short descriptions.
   - Then call with `mode: "detailed_schema"` and one `scaffoldType` to load only that scaffold's parameters, defaults, constraints, and example JSON.
   - Then call with `mode: "render"`, the chosen `scaffoldType`, and `parametersJson` as a JSON object string.

2. Use the generated result:
   - Read the returned paths and module name.
   - For a newly created module, pass the returned combined `moduleName` such as `Foo_Bar` to follow-up scaffolds.
   - If CLI validation is needed, call `describe_magento_cli_environment` before running shell commands and use returned project-local wrappers.

## Scaffold Types

Supported `scaffoldType` values:

- `module`
- `plugin`
- `observer`
- `entity_crud`
- `controller`
- `cli_command`
- `block`
- `view_model`
- `product_eav_attribute`
- `category_eav_attribute`
- `customer_eav_attribute`

## Rules

- Prefer `magento_scaffold` over any generator-specific creation tool. Its staged modes exist to avoid loading the whole scaffold library into context.
- Most scaffold types require `moduleName` in Magento `Vendor_Module` format.
- PHP class parameters must be fully qualified names under the target module namespace, for example `Foo\\Bar\\Block\\Product\\BadgeBlock`.
- JSON backslashes must be escaped in `parametersJson`.
- EAV `options` are only valid for `select` or `multiselect` frontend inputs.
- For CRUD properties, use `field_name:type` strings and do not include the primary ID field.

## Examples

Get the catalog:

```json
{"mode":"help","scaffoldType":"","parametersJson":""}
```

Get module schema:

```json
{"mode":"detailed_schema","scaffoldType":"module","parametersJson":""}
```

Render a module:

```json
{
  "mode": "render",
  "scaffoldType": "module",
  "parametersJson": "{\"packageName\":\"Foo\",\"moduleName\":\"Bar\"}"
}
```
