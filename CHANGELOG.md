# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0). 

## 3.1.0

## 3.0.4

### Fixed

- Overriding the interface that generates invalid php code
- Overriding a template from the base area
- Disabled the ability to create a plugin for a class that implements `\Magento\Framework\ObjectManager\NoninterceptableInterface`

### Added

- `NoninterceptableInterface` case warning to the plugin inspection

## 3.0.3

### Fixed

- Fixed model generation with same names
- Fixed NPTR exception in theme directory view model action
- Fixed observer name validator
- Fixed plugin name validator

## 3.0.2

### Fixed

- Fixed the Inject a View Model dialog

## 3.0.1

### Fixed

- Skipped IDEA include tag causing error
- Fixed StringIndexOutOfBoundsException on PluginInspection

## 3.0.0

### Added

- Description for "Magento Routes XML" code template in [#349](https://github.com/magento/magento2-phpstorm-plugin/pull/349)
- Code completion and reference navigation for table names and column names in `db_schema.xml` file in [#351](https://github.com/magento/magento2-phpstorm-plugin/pull/351)
- Code completion and reference navigation for UI Component names in layout XMLs in [#354](https://github.com/magento/magento2-phpstorm-plugin/pull/354)
- Description for "Magento Layout XML" code template in [#365](https://github.com/magento/magento2-phpstorm-plugin/pull/365)
- Reference navigation for disabled plugins in `di.xml` in [#373](https://github.com/magento/magento2-phpstorm-plugin/pull/373)
- Code completion and reference navigation for Magento module names in `config.php` in [#374](https://github.com/magento/magento2-phpstorm-plugin/pull/374)
- Inspection warning when disabling a nonexistent plugin in `di.xml` in [#382](https://github.com/magento/magento2-phpstorm-plugin/pull/382)
- Description for "Magento Form Button Block Class" code template in [#383](https://github.com/magento/magento2-phpstorm-plugin/pull/383)
- Code generation for database models (model, resource model, and collection) in [#392](https://github.com/magento/magento2-phpstorm-plugin/pull/392)
- Code generation for data models (data interface and its implementation) in [#399](https://github.com/magento/magento2-phpstorm-plugin/pull/399)
- QuickFix for a missing GraphQL resolver defined in the the `schema.graphqls` file in [#399](https://github.com/magento/magento2-phpstorm-plugin/pull/399)

### Fixed

- Inability to save PhpStorm plugin settings after disabling the plugin with invalid field content in [#317](https://github.com/magento/magento2-phpstorm-plugin/pull/317)
- Field statuses in PhpStorm plugin settings not disabling if plugin is disabled in [#320](https://github.com/magento/magento2-phpstorm-plugin/pull/320)
- Missing linemarker for plugins to interface methods in [#328](https://github.com/magento/magento2-phpstorm-plugin/pull/328)
- Incorrect code completion for MFTF tags (stories, title, and description) in [#364](https://github.com/magento/magento2-phpstorm-plugin/pull/364)
- Argument name for types in `di.xml` not allowing underscores in [#370](https://github.com/magento/magento2-phpstorm-plugin/pull/370)
- Incorrect sort order validation in 'Create a New Plugin' code generation dialog in [#389](https://github.com/magento/magento2-phpstorm-plugin/pull/389)

### Changed

- Code generation dialog titles in [#363](https://github.com/magento/magento2-phpstorm-plugin/pull/363)

## 2.0.2

### Added

- PWA pure function Live Template 

### Fixed

- Library root for object manager autocomplete
- Line marker for interface plugin target
- Allowed theme override of non-magento composer themes
- Issue with `Magento Module Ui Grid Collection Data Provider Php`

## 2.0.1

### Fixed

- Directories structure for the override in theme action
- Constant disabling plugin on startup if Magento not in the root
- New module generation for the default Magento version

## 2.0.0

### Added

- Inject a view model to a block and a reference block from the context menu action
- Override in Theme action
- Generate Listing UI component (including all required files)
- Generate Form UI component (including all required files)
- Code Inspection: ACL resource title
- Reference navigation and completion for `crontab.xml`
- Reference navigation and completion for `menu.xml`

### Changed

- Adjusted module version to module.xml (considering the Magento version)
- Adjusted support of variadic arguments to plugin declaration inspection

### Fixed
- Fixed missing first letter name in `composer.json`
- Fixed the CLI command namespace
- Fixed endless loop of notifications on launch
- Fix `composer.json` generation with module dependency that doesn't have `composer.json`

## 1.0.1

### Added

- Create a CLI command action
- Create a CRON group action
- Create a CRON job action
- Create a Controller action
- Code Inspection: Module declaration inspections in the scope of `module.xml` and `registration.php`
- Code Inspection: GraphQL resolver in the scope of a schema file

### Changed

- Fixed the positioning of all dialog popups
- Adjusted Magento root validation for consider `magento/framework` as a requirement
- Adjusted Magento version validation RegExp to support patch versions

### Fixed

- The `create a plugin action` is accessible from the wrong context
- Null pointer exception on the new module group

## 1.0.0

### Added

- RequireJS mapping support (reference navigation, completion)
- MFTF support MVP (reference navigation, completion)
- Line markers for navigation from a plugin class to a target class
- Line markers for navigation from a GraphQl resolver to schema and vice versa
- Create a plugin for a class public method
- Create a New Magento 2 Module action
- Create a Block action
- Create a View Model action
- Create a new Magento 2 module as a separate project
- Create an observer for an event action
- Create a GraphQL resolver action
- Override class by reference action
- Plugin class methods generation
- Code Inspection: Duplicated plugin Usage in di.xml
- Code Inspection: Plugin declaration in the scope of a Plugin Class
- Code Inspection: Warning regarding Cacheable false attribute in default XML
- Code Inspection: GraphQL resolver in the scope of a PHP Class
- Code Inspection: Duplicated Observer Usage in events XML
- Moved plugin configuration from `Settings > Preferences > Languages & Frameworks > PHP > Magento` to
        `Settings > Preferences > Languages & Frameworks > PHP > Frameworks > Magento`
- Fixed support of 202## 0.* versions of IDE's

## 0.3.0

### Added

- Extended navigation from PHP class to its XML declaration to support any configs
- Documented local environment set up for plugin development

### Fixed

- Fixed NullPointerException

## 0.2.3

### Added

- Added JavaScript reference contributor
- Support references for each part of FQN of PHP class, methods, constants
- Support reference from XML/JavaScript for module name
- Support reference from XML/JavaScript for module element path (e.g. Magento_Catalog::product/list/addto/compare.phtml)
- Added project detector
- Move configuration section to "Languages & Frameworks > Php > Magento"
- Remove deprecated elements
    
## 0.2.2

### Added

- Added Module name to configuration tooltip

### Fixed

- Fixed "Project disposed" exception
   
## 0.2.1
   
### Added

- added module name for "Goto configuration" labels
    
## 0.2.0

### Added

- WebApi routes
- nicer "Goto configuration" labels
- plugin settings (manual reindex, URN generation, plugin on/off)
 
## 0.1
 
### Added

- Context type completion for:
    - Observers completion only for ObserverInterface impl in events.xml
    - Blocks completion only for BlockInterface name in layouts.xml
    - Preference configuration in di.xml
    - Type hinting for object arguments in di.xml
- @api usage inspection in Module context
- ObjectManager usage inspection in Module context
- virtualType arguments resolution
- webapi.xml interface/method completion/references
- Support for old people using PhpStorm 8 or JDK## 1.7
    
## 0.0.9
   
### Added

- Added Reference and completion support for layouts
    - block: class, before, after
    - referenceBlock: name
    - move: element, destination, before, after
    - remove: name
    - update: handle
    - referenceContainer: name
- Line marker reference for php class to Layout configuration

## 0.0.8

### Added
- Added Line marker reference for php class/interface to DI configuration
- Added Line marker reference to plugins

## 0.0.7

### Added

- Added reference to configuration and observers (classes or virtualType)
- Added reference to observers from configuration
- Added reference to event dispatch from configuration
    
## 0.0.6

### Added

- Added reference and completion support for virtual types/classes/arguments in DI configuration
    
## 0.0.5

### Added

- Added reference support for classes/interfaces in DI configuration
