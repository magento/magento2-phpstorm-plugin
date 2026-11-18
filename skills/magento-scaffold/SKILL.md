---
name: magento-scaffold
description: Use when generating Magento 2 or Adobe Commerce project scaffolding through the JetBrains Magento MCP toolset, including modules, plugins, observers, CRUD entities, controllers, CLI commands, blocks, view models, and product/category/customer EAV attributes. Use the desktop magento_scaffold tool for an explicit creation request and check generated files for relevant maintenance evidence.
---

# Magento Scaffold

Call `magento_intelligence` in the intended project. Continue with `magento_scaffold` when the connected desktop advertises it. HCE and desktops without the JetBrains PHP plugin can install this skill, but code generation requires a PHP-enabled desktop. Use the invocation indicated by discovery.

## Generate the requested scaffold

1. Read the project instructions. Call `configure_magento_project` with `action: "status"` if the configured root or readiness is uncertain. Resolve configuration or pending indexing before rendering.
2. Use `magento_scaffold` with `mode: "help"` for the supported types. Select one `scaffoldType` and request `mode: "detailed_schema"` for its parameters, defaults and example.
3. Render with `mode: "render"`, the selected `scaffoldType`, and `parametersJson` as a JSON **string**. Review the generated paths and code. For a new module, pass its returned combined `moduleName`, such as `Foo_Bar`, to follow-up scaffolds.

## Check the generated files

Use the actual generated diff to decide what to validate. If `magento_maintenance` is advertised, use `mode: "check"` for relevant files. Pass typed `changedFiles` relative to the Magento root. For example, if a CRUD scaffold adds this schema, check its file evidence.

```json
{"mode":"check","checkIds":["setup_upgrade"],"changedFiles":["app/code/Foo/Bar/etc/db_schema.xml"]}
```

A generated data patch warrants the same check. For layout XML use `cache_clean`. Supply `newlyEnabledModules` only when the enabled-module configuration actually changed. A generated plugin or PHP class alone gives no reason to recommend an upgrade. For XML rule checks, `magento_diagnostics` takes an IDE-project-relative `filePath`, including any Magento subdirectory.

For an explicit runtime verification request, pass the relevant changes to `magento_maintenance` with `mode: "verify"`. Report the affected item, evidence, environment and next action. Leave suggested commands as suggestions unless the user requests the maintenance action. See [magento-inspect](../magento-inspect/SKILL.md) when installed for multi-check ordering and status interpretation.

For other runtime validation, discover the preferred script and working directory using `describe_magento_cli_environment`. A generated `cli_command` belongs to the Magento CLI. Use `magento_magerun` command discovery for n98-magerun2 operations. Report the selected runtime and any validation blocker.

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
- Review generated paths and code, then implement the requested behavior using the project's conventions. Do not treat successful generation as proof that the feature works or broaden the task into an unrelated architectural rewrite.
- Most scaffold types require `moduleName` in Magento `Vendor_Module` format.
- PHP class parameters must be fully qualified names under the target module namespace, for example `Foo\\Bar\\Block\\Product\\BadgeBlock`.
- JSON backslashes must be escaped in `parametersJson`.
- EAV `options` are only valid for `select` or `multiselect` frontend inputs.
- For CRUD properties, use `field_name:type` strings and do not include the primary ID field.
- `entity_crud` intentionally generates CQRS-style read/write separation, such as query and command classes, instead of the standard Magento repository-only structure.

## Examples

Get the catalog:

```json
{"mode":"help"}
```

Get module schema:

```json
{"mode":"detailed_schema","scaffoldType":"module"}
```

Render a module:

```json
{
  "mode": "render",
  "scaffoldType": "module",
  "parametersJson": "{\"packageName\":\"Foo\",\"moduleName\":\"Bar\"}"
}
```
