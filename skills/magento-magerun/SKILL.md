---
name: magento-magerun
description: Use n98-magerun2 to investigate or operate an installed Magento 2, Adobe Commerce, or Mage-OS project, including installed command discovery, Docker/DDEV wrappers, scoped configuration, database, cron, indexers, and native Magerun MCP settings.
---

# Magento runtime with n98-magerun2

Call `magento_intelligence` in the intended project and follow the advertised invocation for each tool. For a question about Composer installation, Cloud patches, database setup or cache invalidation, use `magento_maintenance` first when available. Its `check` mode takes changed paths relative to the Magento root. Use `verify` for an explicitly requested runtime check. Report the finding's item, evidence and environment. Magerun discovery is only needed for a separate runtime question, such as inspecting a store-scoped setting.

## Select the runtime

Call `describe_magento_cli_environment` and read the configured Magento root, preferred script, invocation and working directory. For a project with Compose at its root and Magento in `src/`, a preferred wrapper may run from the project root. A fallback displayed as `./src/bin/magento` may instead run as `./bin/magento` with `cwd` in `src/`. Use each invocation from its own working directory.

For DDEV, review the parsed configuration and ordered override files. Append the intended container command and its arguments to the returned `execPrefix` from its host `cwd`. Read `configuration.status` and coverage before using the prefix. Respect the configured project name and `working_dir.web`. The `docroot` serves web requests and `composer_root` controls `ddev composer`. If DDEV needs an interactive permission prompt, use the returned `terminalGuidance.command` from `terminalGuidance.cwd` in the indicated terminal. If a launcher or container fails, report that environment's blocker. Use a fallback only when fresh discovery selects it. Use the discovered Magento CLI wrapper for Magento CLI commands.

## Discover installed Magerun commands

1. Call `magento_magerun` with typed arguments and `mode: "status"`. Inspect `selected` and `invocation`. If there is no project-local candidate, a configured DDEV project can supply `magerun2` inside the container. File detection and cached metadata alone cannot confirm that the runtime is currently available.
2. Call `mode: "commands"` with a focused `search` and `limit` (1–100). To page, pass `nextOffset` as `offset` and keep the search and limit unchanged. Stop when the command is found or `nextOffset` is null.
3. Call `mode: "command_help"` with an exact returned command or alias in `command`. Check its installed arguments and options. Read the JSON `status` and `message` even for a successful MCP transport. `ok` indicates successful metadata retrieval. Distinguish missing launchers, incompatible installations, unsupported wrappers, and runtime failures from a missing command.
4. Reuse metadata while paging or reading help. It expires after five minutes. Use `refresh: true` when the installed runtime changes or fresh discovery is requested. Refreshing `status` clears the cache. Refreshing `commands` probes the runtime.
5. When the task calls for executing a discovered command, append its verified arguments to the returned `invocation.argv` and preserve `invocation.cwd`. Keep arguments separate. Running `sourceWrapper` directly can trigger installation. Adding a host `--root-dir` can point a container command at the wrong path. Report any failure in the selected runtime.

For DDEV, an uncached metadata probe checks the project with `ddev describe --skip-hooks` before it runs `ddev exec`. If the tool returns `environment_not_running`, follow its message. Wait while the project is starting, or ask the developer to run `ddev start` from the DDEV project root. Running the returned `ddev exec` invocation directly would start a stopped project. If the status probe returns `runtime_unavailable` or `timeout`, report the blocker in the selected runtime.

```json
{"mode":"commands","search":"sys:info","limit":10}
```

## Native Magerun MCP settings

For a native MCP setup request, use `magento_magerun` with `mode: "mcp_config"`. Discover the installed commands first. Omit `include` for the tool's supported defaults, or pass narrow, space-separated `include` and optional `exclude` patterns. Preserve the returned command, arguments, working directory and environment. Check `serverStarted` and `configurationWritten` when reporting the result. Apply settings or start the server only if that work was requested. Launching a DDEV-backed configuration uses `ddev exec`, which starts a project that is stopped.

## Application and configuration

- **Application PHP:** When runtime evidence is needed, discover `dev:console` and read its help. It bootstraps Magento; `dev:console --auto-exit` can run a supplied command and exit. For example, an invocation followed by `dev:console --auto-exit 'echo 1 + 1;'` checks the console without changing data. Use focused PHP to call the relevant repository or service and print only needed results. Do not assume a repository call or arbitrary PHP is read-only: review the code and effects first. Area-dependent behavior requires the appropriate application area; report bootstrap or area errors rather than guessing.
- **DI and events:** Search `dev:di:preferences:list`, `config:data:di`, and `dev:module:observer:list` for the relevant type, event, and area. Compare runtime merged configuration with source declarations and distinguish configured observers from evidence that an event actually ran.
- **Store settings:** Use `config:search` to find a configuration path, `sys:store:list` for store context, then `config:store:get` with the relevant `--scope` and `--scope-id`. Check the installed help and account for scope inheritance and environment overrides when interpreting a value.
- **Installation:** Use `sys:info`, `sys:check`, and `dev:module:list` as relevant to identify the installed application, module state, and diagnostic issues. Report their scope and limitations rather than assuming a successful check proves the application is healthy.

## Database, EAV and secrets

- `db:query` uses the Magento database configuration, normally from `app/etc/env.php`, without manually copying credentials into a command. Consult help for `--connection` when multiple connections exist. Database access may require the container's database client and network; do not print the whole configuration, credentials, connection strings, or crypt key. Avoid `--only-command` because generated connection commands can reveal credentials.
- For inspection, use focused `SELECT` queries with explicit columns, relevant predicates, and a limit. Avoid returning sensitive customer or payment data unless needed for the request. `eav:attribute:list` and `eav:attribute:view` help identify entity types, attributes, and metadata; use the installed help for their arguments before correlating metadata with persisted rows. Respect table prefixes and store scope.
- Encrypted configuration can be read with `config:store:get --decrypt` for a specific path and scope; `dev:decrypt` handles a supplied encrypted value if the installed version supports it. Decrypt only when needed for the user's task. Treat decrypted values, encrypted values, and the Magento crypt key as secrets: minimize exposure in logs, command history, process arguments, MCP responses, and files. Prefer checking the needed property (for example, whether a value is present or matches an expected value) without displaying the value. Do not include secrets in the answer unless the user explicitly needs the value. If the available command would expose a secret through an inappropriate channel, explain the limitation and let the user perform the sensitive step locally.

## Cron and indexers

- Use `sys:cron:list` and `sys:cron:history` to inspect the job definition and recorded executions. `sys:cron:run` executes real application code and may send email, call external services, or modify data; use it only when running that job is within the requested task and its effects are understood. Review the job code, environment, and installed command help first. A successful invocation is not proof that the scheduler itself works.
- Inspect indexers with `indexer:list` or the discovered Magento CLI `indexer:status`. To run a requested reindex, use the discovered Magento CLI `indexer:reindex` with the relevant indexer IDs after checking its help; monitor the result and verify status. Reindexing writes data and can be expensive. Do not use `indexer:trigger:recreate` as a substitute for reindexing.

Report the chosen runtime, relevant scope and evidence, and any incomplete checks without including secrets or unrelated data.
