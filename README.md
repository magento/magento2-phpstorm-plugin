<p align="center">
    <a href="https://magento.com">
        <img src="https://static.magento.com/sites/all/themes/magento/logo.svg" width="300px" alt="Magento Commerce" />
    </a>
</p>

# PhpStorm Magento 2 Plugin

[![Version](http://phpstorm.espend.de/badge/8024/version)](https://plugins.jetbrains.com/plugin/8024)
[![Downloads](http://phpstorm.espend.de/badge/8024/downloads)](https://plugins.jetbrains.com/plugin/8024)
[![Made With Love](https://img.shields.io/badge/Made%20With-Love-orange.svg)](https://magento.com)

## Installation

1. Go to `Settings > Preferences` in the PhpStorm IDE
2. Navigate to `Plugins`
3. Click the `Browse repositories...` button and search for "Magento PhpStorm"
4. Install the plugin and restart PhpStorm
5. Go to `Settings > Preferences > Languages & Frameworks > PHP > Frameworks > Magento` in the PhpStorm IDE
6. Check `Enable` and click the `OK` button

## Works with

* PhpStorm >= 2023.1
* JRE >= 17

## Features

* Configuration smart completion and references for XML/JavaScript files
* `Navigate to configuration` reference in scope of class/interface
* `Go to plugin` reference in scope of class/interface and method
* `Navigate to Web API configuration` reference in scope of class/interface and method
* Plugin class methods generation
* Plugin declaration inspection
* RequireJS reference navigation and completion
* MFTF reference navigation and completion
* GraphQL navigation line markers
* Code generation
* Inspections for XML configuration

## Setting up development environment

1. Check out this repository
1. Open a folder with the project in the IntelliJ Ultimate using the `open` action button.
1. Make sure that you on the latest develop branch (e.g `5.3.0-develop`)
1. Right-click on the `build.gradle` file, choose "Import Gradle project" (if this is not exist look for "Build module '<root folder name>'") (you need to have Gradle plugin installed)
1. Check if the right SDK version is used for the project.
   - Current Java version for the project is **java 11**, so you should additionally download **SDK 11** and choose it in the module settings: `Right click by the project root > Open Module Settings > Project Settings > Project > Project SDK`
   - Check if right SDK version is used for the Gradle plugin: `Intellij IDEA > Preferences... > Build, Execution, Deployment > Build Tools > Gradle > Gradle JVM` and choose your JDK.
1. When the Gradle sections appeared in the right bar, navigate there and right-click `magento-2-php-storm-plugin > Tasks -> Intellij -> runIde`
1. Click `Run "magento-2-php-storm-plugin"` to run the plugin. You should see a new instance of IntelliJ launched with the plugin installed. Make sure the plugin is enabled in IntelliJ settings and indexing is finished. Plugin features should be accessible at this point.

## How to contribute
1) Start with looking into [Community Backlog](https://github.com/magento/magento2-phpstorm-plugin/projects/2). Any ticket in `Ready for Development` and `Good First Issue` columns are a good candidates to start.
2) Didn't satisfy your requirements? [Create a new issue](https://github.com/magento/magento2-phpstorm-plugin/issues/new). It can be for example:
   - **Bug report** - Found a bug in the code? Let us know!
   - **Enhancement** - Know how to improve existing functionality? Open an issue describe how to enhance the plugin.
   - **New feature proposal** - Know how to make a killer feature? Do not hesitate to submit your proposal.
3) The issue will appear in the `Ready for Grooming` column of the [Community Backlog](https://github.com/magento/magento2-phpstorm-plugin/projects/2). Once it will be discussed and approved the issue will be ready for development.
4) Refer to the [Contributing Guide](https://github.com/magento/magento2-phpstorm-plugin/blob/2.1.0-develop/.github/CONTRIBUTING.md) for more information on how to contribute.

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

Please read the [LICENSE.txt](https://github.com/magento/magento2-phpstorm-plugin/blob/master/LICENSE.txt) for the full text of the [Open Software License v. 3.0 (OSL-3.0)](http://opensource.org/licenses/osl-3.0.php).
