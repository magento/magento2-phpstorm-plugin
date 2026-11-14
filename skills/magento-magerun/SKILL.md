---
name: magento-magerun
description: Use n98-magerun2 to investigate or operate an installed Magento 2, Adobe Commerce, or Mage-OS project, including runtime, configuration, database, cron, and indexer workflows.
---

# Magento runtime with n98-magerun2

Use this skill when a task needs the running Magento application or its database. Source configuration can establish declarations, but runtime results depend on the selected installation, area, scope, and deployed state.

Call `magento_intelligence` to discover available Magento MCP tools. To check project skill copies via the router, pass `magento_skills --mode status --agentTarget agents` to `execute_tool`.

## Select and verify the runtime

1. Call `describe_magento_cli_environment` for the intended IDE project and check the Magento root, working directory, wrappers, and container configuration. Do not assume the host and container share paths or executables.
2. Call `magento_magerun` with `mode: "status"`; detection alone does not verify a wrapper or running container. Use `mode: "commands"` with a focused `search`, followed by `mode: "command_help"` for an exact returned command. The installed version and custom modules determine what exists and which options it accepts.
3. These MCP modes inspect metadata, not application state. Execute only commands needed for the user's task using the returned `invocation.argv` and `invocation.cwd`, preserving argument boundaries. Do not execute `sourceWrapper` in place of the returned invocation, switch runtimes after a failure, or assume native Magerun MCP is connected. If discovery or execution fails, report the actual blocker.

For commands belonging to Magento rather than Magerun, use the discovered Magento CLI wrapper and its reported working directory. Confirm the environment and intended scope before operations that write data, send messages, run jobs, or rebuild indexes; run only the operation requested or necessary for the task. Explain meaningful side effects and report failures.

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
