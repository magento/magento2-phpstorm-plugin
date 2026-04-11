<p align="center">
    <a href="https://magento.com">
        <img src="https://static.magento.com/sites/all/themes/magento/logo.svg" width="300px" alt="Magento Commerce" />
    </a>
</p>

<!-- Plugin description -->
# Magento 2 and Adobe Commerce Support

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
3. Click the `Browse repositories...` button and search for "Magento 2 and Adobe Commerce Support"
4. Install the plugin and restart PhpStorm
5. Go to `Settings > Preferences > Languages & Frameworks > PHP > Frameworks > Magento` in the PhpStorm IDE
6. Check `Enable` and click the `OK` button

## Works with

* PhpStorm 2026+

## MCP tools

The plugin exposes a Magento-specific MCP toolset for AI agents inside JetBrains IDEs with MCP support enabled.

Available project and creation tools:

* `get_magento_root_path`: returns the resolved Magento root directory for the current IDE project. Use this when an agent or shell command needs an absolute project path.
* `create_magento_module`: creates a new Magento module with `composer.json`, `registration.php`, and `etc/module.xml`. Pass Magento module parts such as `packageName=Foo` and `moduleName=Bar` to create `Foo_Bar`; the Composer package name is derived automatically as `foo/module-bar`.
* `create_magento_plugin`: creates a plugin class and the matching `di.xml` declaration. `moduleName` must be `Vendor_Module`, `targetClassName` must be an existing PHP FQN such as `Magento\\Catalog\\Api\\ProductRepositoryInterface`, `targetMethodName` is the intercepted method name, and `pluginType` must be `before`, `around`, or `after`.
* `create_magento_observer`: creates an observer class and `events.xml` declaration. `moduleName` must be `Vendor_Module`, `eventName` should be a Magento event such as `catalog_product_save_after`, and `observerClassFqn` should live inside the module namespace, usually under `Observer\\`.
* `create_magento_entity_crud`: scaffolds a Magento CRUD module area including DB schema, model, resource model, collection, repository-related classes, ACL/menu entries, and optional admin UI pieces. `properties` must be a list of `field_name:type` values such as `["title:string", "is_active:bool"]`; the primary ID field is generated automatically.
* `create_magento_controller`: creates a controller class in an editable module. `controllerClassFqn` must be under the module `Controller` namespace, for example `Foo\\Bar\\Controller\\Index\\Index` or `Foo\\Bar\\Controller\\Adminhtml\\Order\\Index`, and `httpMethod` must be `GET`, `POST`, `PUT`, or `DELETE`.
* `create_magento_cli_command`: creates a Symfony console command class and registers it in `etc/di.xml`. `commandClassFqn` usually belongs under `Console\\Command`, and `commandName` should be a Magento CLI command name such as `foo:bar:sync-data`.
* `create_magento_block`: creates a block class under the module `Block\\` namespace, for example `Foo\\Bar\\Block\\Product\\BadgeBlock`.
* `create_magento_view_model`: creates a view model class under the module `ViewModel\\` namespace, for example `Foo\\Bar\\ViewModel\\Product\\BadgeViewModel`.
* `create_magento_product_eav_attribute`: creates a product EAV attribute data patch and optional source model. `attributeCode` must be lower_snake_case, `backendType` and `frontendInput` must be valid Magento attribute types, and `options` may only be used for `select` or `multiselect`.
* `create_magento_category_eav_attribute`: creates a category EAV attribute data patch, `view/adminhtml/ui_component/category_form.xml`, and an optional source model using the same attribute format rules as the product tool.
* `create_magento_customer_eav_attribute`: creates a customer EAV attribute data patch and optional source model using Magento customer attribute rules.

Available query and inspection tools:

* `find_magento_module`: finds modules by exact or partial Magento module name such as `Magento_Catalog`, `Foo_Bar`, `Catalog`, or `Magento_`. Use this to confirm the canonical module name and path before generating code.
* `find_di_config_for_class`: finds `di.xml` declarations related to a PHP FQN or virtual type name such as `Magento\\Catalog\\Model\\Product` or `catalogProductRepository`. Use this to inspect preferences, virtual types, arguments, and related DI wiring before making changes.
* `find_plugins_for_method`: finds plugins for a target class or interface method. Pass a PHP FQN such as `Magento\\Catalog\\Api\\ProductRepositoryInterface` and a bare method name such as `save` or `getById`.
* `find_observers_for_event`: finds observers by full or partial event name such as `catalog_product_save_after` or `product_save` and returns matching `events.xml` declarations.
* `find_layout_entities`: finds layout handles, block names, and container names by exact or partial value such as `catalog_product_view`, `product.info.main`, or `checkout`.
* `find_ui_component`: finds UI component XML definitions by exact or partial component file name such as `product_form`, `sales_order_grid`, or `category_form` without the `.xml` extension.
* `find_acl_or_menu`: finds ACL resource IDs and admin menu entries by exact or partial identifier such as `Magento_Catalog::catalog` or `Foo_Bar::manage_items`.
* `describe_magento_cli_environment`: detects project-local CLI wrappers such as Mark Shust Docker scripts under project-level or Magento-root `bin/`, returns the exact command paths an agent should use, and includes example invocations for Magento CLI, PHP, Composer, and `n98-magerun`.

Notes:

* The IDE MCP server must be enabled in the JetBrains IDE.
* The IDE MCP server entry must be added to the agent MCP configuration.
* MCP tools work against the currently opened IDE project.
* Magento plugin support must be enabled for the project.
* Indexing must be finished before MCP queries and generators can run.
* Category EAV attribute generation creates both the data patch and `view/adminhtml/ui_component/category_form.xml`.
* `describe_magento_cli_environment` detects project-local wrappers such as Mark Shust Docker scripts under project-level or Magento-root `bin/` and returns the exact command paths an agent should use.

### MCP CLI wrapper configuration

If your Magento project uses local wrapper scripts such as Mark Shust Docker commands, configure them in:

`Settings > Languages & Frameworks > PHP > Frameworks > Magento > MCP CLI wrapper candidates`

Use a comma-separated list of relative paths, for example:

`bin/magento, bin/n98-magerun2, bin/cli`

Agent usage pattern:

1. Call `describe_magento_cli_environment`.
2. Use the returned wrapper path exactly, for example `./bin/magento cache:flush` or `./bin/n98-magerun2 sys:info`.
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
