<p align="center">
    <a href="https://magento.com">
        <img src="https://static.magento.com/sites/all/themes/magento/logo.svg" width="300px" alt="Magento Commerce" />
    </a>
</p>

<!-- Plugin description -->
# Magento 2 and Adobe Commerce

<table align="center" style="border-collapse: collapse; width: 100%; text-align: center;">
  <caption style="font-size: 1.2em; margin-bottom: 10px;">
    <strong>PhpStorm IDE Plugin</strong> for a better Magento 2 development workflow.
  </caption>
  <thead>
  </thead>
  <tbody>
  </tbody>
  <tfoot>
    <tr style="background-color: #f9f9f9;">
      <td colspan="3" style="padding: 20px;">
        <h3 style="margin: 10px 0;">Support the Project</h3>
        <p>If you find this plugin helpful and want to support its development, consider buying the contributors a coffee:</p>
        <a href="https://buymeacoffee.com/vitalii_b" style="text-decoration: none; color: inherit;">
<pre style="display: inline-block; margin: 10px 0; font-family: monospace;">
    ( (
     ) )
  ........
  |      |]
  \      /
   `----'
 Buy Me a Coffee
</pre>
        </a>
        <h3 style="margin: 10px 0;">Sponsors</h3>
        <p style="margin: 10px 0;">Thank you to our sponsors for supporting the plugin:</p>
        <p><strong>Lucas van Staden</strong></p>
        <p><strong>Ivan Chepurnyi</strong></p>
        <p><strong>Michael Ryvlin</strong></p>
      </td>
    </tr>
  </tfoot>
</table>

## Features

* Configuration smart completion and references for XML/JavaScript files
* `Navigate to configuration` reference in scope of class/interface
* `Go to plugin` reference in scope of class/interface and method
* `Navigate to Web API configuration` reference in scope of class/interface and method
* Plugin class methods generation
* Plugin declaration inspection
* Magento-specific MCP tools for AI agents
* RequireJS reference navigation and completion
* MFTF reference navigation and completion
* GraphQL navigation line markers
* Code generation
* Inspections for XML configuration

<!-- Plugin description end -->

[![Version](http://phpstorm.espend.de/badge/8024/version)](https://plugins.jetbrains.com/plugin/8024)
[![Downloads](http://phpstorm.espend.de/badge/8024/downloads)](https://plugins.jetbrains.com/plugin/8024)
[![Made With Love](https://img.shields.io/badge/Made%20With-Love-orange.svg)](https://magento.com)

## Installation

1. Go to `Settings > Preferences` in the PhpStorm IDE
2. Navigate to `Plugins`
3. Click the `Browse repositories...` button and search for "Magento 2 and Adobe Commerce"
4. Install the plugin and restart PhpStorm
5. Go to `Settings > Preferences > Languages & Frameworks > PHP > Frameworks > Magento` in the PhpStorm IDE
6. Check `Enable` and click the `OK` button

## Works with

* PhpStorm 2026+

## MCP tools

The plugin exposes a Magento-specific MCP toolset for AI agents inside JetBrains IDEs with MCP support enabled.

Available project and creation tools:

* `get_magento_root_path`: returns the resolved Magento root directory for the current IDE project. Use this when an agent or shell command needs an absolute project path.
* `magento_scaffold`: a three-step Magento scaffold library and renderer designed to keep agent context small. First call it with `mode=help` to get only scaffold names and short descriptions. Then call it with `mode=detailed_schema` and one `scaffoldType` to load detailed parameters, defaults, constraints, and example JSON only for that scaffold. Finally call it with `mode=render`, the selected `scaffoldType`, and `parametersJson` as a JSON object to create files. Supported scaffold types are `module`, `plugin`, `observer`, `entity_crud`, `controller`, `cli_command`, `block`, `view_model`, `product_eav_attribute`, `category_eav_attribute`, and `customer_eav_attribute`.

Available query and inspection tools:

* `magento_inspect`: a three-step Magento inspection library designed to keep agent context small. First call it with `mode=help` to get only query names and short descriptions. Then call it with `mode=detailed_schema` and one `queryType` to load detailed parameters and example JSON only for that inspection. Finally call it with `mode=query`, the selected `queryType`, and `parametersJson` as a JSON object to inspect the project. Supported query types are `module`, `di_config`, `plugins_for_method`, `observers_for_event`, `layout_entities`, `ui_component`, and `acl_or_menu`.
* `describe_magento_cli_environment`: detects project-local CLI wrappers such as Mark Shust Docker scripts under project-level or Magento-root `bin/`, returns the exact command paths an agent should use, and includes example invocations for Magento CLI, PHP, Composer, `n98-magerun`, and stack lifecycle commands such as `./bin/start`.

Notes:

* The IDE MCP server must be enabled in the JetBrains IDE.
* Only one JetBrains IDE instance should have the MCP server enabled at a time so the configured MCP port stays free; if another IDE is already using that port, the MCP server will not start correctly.
* The IDE MCP server entry must be added to the agent MCP configuration.
* MCP tools work against the currently opened IDE project.
* Magento plugin support must be enabled for the project.
* Indexing must be finished before MCP queries and generators can run.
* Category EAV attribute generation creates both the data patch and `view/adminhtml/ui_component/category_form.xml`.
* `describe_magento_cli_environment` detects project-local wrappers such as Mark Shust Docker scripts under project-level or Magento-root `bin/` and returns the exact command paths an agent should use, including stack lifecycle wrappers such as `bin/start`, `bin/stop`, and `bin/restart`.

### MCP CLI wrapper configuration

If your Magento project uses local wrapper scripts such as Mark Shust Docker commands, configure them in:

`Settings > Languages & Frameworks > PHP > Frameworks > Magento > MCP CLI wrapper candidates`

Use a comma-separated list of relative paths, for example:

`bin/magento, bin/n98-magerun2, bin/cli, bin/start`

Agent usage pattern:

1. Call `describe_magento_cli_environment`.
2. Use the returned wrapper path exactly, for example `./bin/magento cache:flush`, `./bin/n98-magerun2 sys:info`, or `./bin/start`.
3. Keep Magento code edits and generators under the configured Magento root.
4. If the tool reports a wrapper outside that root, still run it from the returned project-relative path; that is valid for nested Magento roots.
5. Prefer the wrapper over global binaries because these scripts often enter Docker containers or a project-specific runtime.

## Setting up development environment

1. Check out this repository.
1. Open the project in IntelliJ IDEA.
1. Make sure that you are on the latest develop branch (for example `5.4.0-develop`).
1. Import the Gradle project from `build.gradle.kts`.
1. Use **JDK 21** for both the project SDK and the Gradle JVM:
    - `Right click on the project root > Open Module Settings > Project > Project SDK`
    - `IntelliJ IDEA > Preferences > Build, Execution, Deployment > Build Tools > Gradle > Gradle JVM`
1. In the Gradle tool window, run `Tasks > Intellij platform > runIde`.
1. The task launches a PhpStorm sandbox with the plugin installed. Make sure the plugin is enabled and indexing is finished before testing features.

## How to contribute
1) Start with looking into [Community Backlog](https://github.com/magento/magento2-phpstorm-plugin/issues?q=is%3Aissue%20state%3Aopen%20label%3A%22good%20first%20issue%22). Any ticket in `Ready for Development` and `Good First Issue` columns are a good candidates to start.
2) Didn't satisfy your requirements? [Create a new issue](https://github.com/magento/magento2-phpstorm-plugin/issues/new). It can be for example:
    - **Bug report** - Found a bug in the code? Let us know!
    - **Enhancement** - Know how to improve existing functionality? Open an issue describe how to enhance the plugin.
    - **New feature proposal** - Know how to make a killer feature? Do not hesitate to submit your proposal.
3) The issue will appear in the `Ready for Grooming` column of the [Community Backlog](https://github.com/magento/magento2-phpstorm-plugin/issues?q=is%3Aissue%20state%3Aopen%20label%3A%22good%20first%20issue%22). Once it will be discussed and approved the issue will be ready for development.
4) Refer to the [Contributing Guide](https://github.com/magento/magento2-phpstorm-plugin/blob/5.4.0-develop/.github/CONTRIBUTING.md) for more information on how to contribute.

## Learn to contribute
1) SDK [Developing a Plugin](https://plugins.jetbrains.com/docs/intellij/developing-plugins.html)
2) Good Presentation about platform [How We Built Comma, the Raku IDE, on the IntelliJ Platform](https://www.youtube.com/watch?v=zDP9uUMYrvs)
3) Plugin example [idea-php-symfony2-plugin](https://github.com/Haehnchen/idea-php-symfony2-plugin)

## How to create SandBox for development
1. Create sandbox folder
2. Copy to sandbox folder `composer.json` and `composer.lock`
3. In sandbox folder create `app/code` and `vendor/magento`
4. Copy any of the magento modules (as for example: `framework`, `module-catalog`, `module-checkout`, `module-customer`, `module-sales`) into the `vendor/magento` folder. It is better to add as few modules as possible to reduce reindexing time during application running
5. (Nice to have) Open IDE and go to `Preferences > Editor > File and Code Templates > Includes tab` and add default headers for `PHP File Header` and `XML File Header`

**PHP File Header:**
```php
/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
declare(strict_types=1);
```

**XML File Header:**
```xml
<!--
/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
-->
```

### <img src="https://upload.wikimedia.org/wikipedia/commons/7/76/Slack_Icon.png" width="20"> Join the [#phpstorm-plugin](https://magentocommeng.slack.com/archives/C010C2LUCEA) Slack channel to get more involved

## License

Each Magento source file included in this distribution is licensed under OSL-3.0 license.

Please read the [LICENSE.txt](https://github.com/magento/magento2-phpstorm-plugin/blob/5.4.0-develop/LICENSE.txt) for the full text of the [Open Software License v. 3.0 (OSL-3.0)](http://opensource.org/licenses/osl-3.0.php).
