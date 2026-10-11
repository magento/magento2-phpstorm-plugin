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

Connect your AI assistant to PhpStorm to inspect Magento configuration, generate modules and other components, and check upgrade compatibility.

1. Open your Magento project and enable the plugin in **Settings > Languages & Frameworks > PHP > Frameworks > Magento**. Check the Magento installation path if it differs from the project directory.
2. Enable the IDE's **MCP Server** and add its connection to your assistant using the configuration shown in the IDE.
3. Wait for indexing to finish, then ask your assistant to work with the open project. For example:
   * “Find the plugins and observers used by Application_Blog.”
   * “Create a Magento module named Acme_Example.”
   * “Check Application_Blog against the next published Magento minor release and explain the findings.”

Compatibility checks can download release data on first use. Ask for **only new upgrade issues** to exclude issues already present in the installed version. The checks cover Magento Open Source core APIs; review Commerce extensions, third-party code, and runtime behavior separately.

In the Magento settings page, you can also:

* Install the bundled **Magento inspect** and **Magento scaffold** skills for your assistant.
* Set **MCP CLI wrapper candidates** when your project uses Docker or other wrapper scripts, for example `bin/magento, bin/cli, bin/composer`.
* Change **Published releases URL** to use a GitHub-compatible release catalog mirror. Leave it blank to use the official Magento GitHub catalog.

Reconnect your assistant after updating the plugin so it discovers the latest tools. If it connects to the wrong project, check the IDE connection and project path. When running multiple IDEs, ensure their MCP servers use different ports.

## Setting up development environment

1. Check out this repository.
1. Open the project in IntelliJ IDEA.
1. Make sure that you are on the latest develop branch (for example `5.4.0-develop`).
1. Import the Gradle project from `build.gradle.kts`.
1. Use **JDK 25** for both the project SDK and the Gradle JVM:
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
