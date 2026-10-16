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
   - If CLI validation is needed, follow the project-runtime workflow below. Source generation alone does not require runtime probes.

## Validate in the Project Runtime

Call `describe_magento_cli_environment` before shell validation. Use the returned **preferred** script and its working directory. Scripts beside the nearest Docker Compose configuration inside the project take precedence: with Compose at the project root and Magento in `src/`, prefer `./bin/magento`; `./src/bin/magento` is a fallback. Without an in-project Compose directory, use the Magento directory. Do not search outside the project.

Use the returned invocation example from its stated working directory. A fallback displayed as `./src/bin/magento` can be invoked as `./bin/magento` from `src/`; do not combine the two paths. Keep generated files under the configured Magento root. Examples describe syntax; run the validation needed for the task, not an illustrative cache flush or another unrelated operation.

Use a fallback only when fresh discovery shows the preferred script is absent or no longer eligible. A command error or stopped container is a blocker in the selected runtime, not a reason to switch to a host PHP/global binary or another installation. Report what was generated, which validation ran, and any runtime blocker. Creating a scaffold does not by itself request a Magento upgrade, reindex, service start, or tool installation; perform follow-up changes when they are part of the user's task.

### When validation needs n98-magerun2

- Call `magento_magerun` with `mode: "status"` to discover the selected candidate and `invocation` without starting a process. Check project/root identity. `detected` and `cached` do not prove current runtime availability.
- Use `mode: "commands"` with a focused `search`, then `mode: "command_help"` for an exact returned command or alias. These are typed arguments, not `parametersJson`. Read installed arguments/options instead of guessing commands; a command created by the `cli_command` scaffold belongs to Magento's CLI and is not automatically a magerun command.
- Keep `search` and `limit` unchanged when following `nextOffset`; stop when it is null or enough information is available. Reuse metadata during validation. It expires after five minutes; set `refresh: true` when runtime contents changed or a fresh check is requested. Refreshing `status` only clears the cache; refreshing `commands` probes again.
- Inspect the JSON `status` and `message` even if MCP does not set `isError`. Metadata success is `ok`. Missing, unavailable, incompatible, or unsupported installations should be reported without switching runtimes or triggering bootstrap downloads. Correct invalid arguments before retrying.
- Metadata probes do not execute application commands. If validation calls for an actual magerun command, append the verified command/options to returned `invocation.argv` and preserve its `cwd` and argument boundaries. Use that invocation rather than running `sourceWrapper`, which can contain download steps, or adding a host `--root-dir` to a container command.
- `mode: "mcp_config"` is for a separate native MCP setup request. It generates settings with optional narrow `include`/`exclude` filters; it neither starts a server nor writes client settings. Preserve returned command, arguments, working directory, and environment if applying the configuration is requested.

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
- Do not change the generated scaffold structure unless the user explicitly asks for that refactor or architectural rewrite.
- Most scaffold types require `moduleName` in Magento `Vendor_Module` format.
- PHP class parameters must be fully qualified names under the target module namespace, for example `Foo\\Bar\\Block\\Product\\BadgeBlock`.
- JSON backslashes must be escaped in `parametersJson`.
- EAV `options` are only valid for `select` or `multiselect` frontend inputs.
- For CRUD properties, use `field_name:type` strings and do not include the primary ID field.
- `entity_crud` intentionally generates CQRS-style read/write separation, such as query and command classes, instead of the standard Magento repository-only structure.

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
