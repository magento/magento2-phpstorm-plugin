1.0.1
============= 
* Features:
    * Create a CLI command action
    * Create a CRON group action
    * Create a CRON job action
    * Create a Controller action
    * Code Inspection: Module declaration inspections in the scope of`module.xml` and `registration.php`
    * Code Inspection: GraphQL resolver in the scope of a schema file
* Improvements:
    * Fixed the positioning of all dialog popups
    * Adjusted Magento root validation for consider `magento/framework` as a requirement
    * Adjusted Magento version validation RegExp to support patch versions
* Fixed bugs:
    * The `create a plugin action` is accessible from the wrong context
    * Null pointer exception on the new module group

1.0.0
============= 
* Features:
    * RequireJS mapping support (reference navigation, completion)
    * MFTF support MVP (reference navigation, completion)
    * Line markers for navigation from a plugin class to a target class
    * Line markers for navigation from a GraphQl resolver to schema and vice versa
    * Create a plugin for a class public method
    * Create a New Magento 2 Module action
    * Create a Block action
    * Create a View Model action
    * Create a new Magento 2 module as a separate project
    * Create an observer for an event action
    * Create a GraphQL resolver action
    * Override class by reference action
    * Plugin class methods generation
    * Code Inspection: Duplicated plugin Usage in di.xml
    * Code Inspection: Plugin declaration in the scope of a Plugin Class
    * Code Inspection: Warning regarding Cacheable false attribute in default XML
    * Code Inspection: GraphQL resolver in the scope of a PHP Class
    * Code Inspection: Duplicated Observer Usage in events XML
    * Moved plugin configuration from `Settings > Preferences > Languages & Frameworks > PHP > Magento` to
        `Settings > Preferences > Languages & Frameworks > PHP > Frameworks > Magento`
    * Fixed support of 2020.* versions of IDE's

0.3.0
============= 
* Features:
    * Extended navigation from PHP class to its XML declaration to support any configs
    * Documented local environment set up for plugin development
* Fixed bugs:
    * Fixed NullPointerException

0.2.3
=============
* Features:
    * Added JavaScript reference contributor
    * Support references for each part of FQN of PHP class, methods, constants
    * Support reference from XML/JavaScript for module name
    * Support reference from XML/JavaScript for module element path (e.g. Magento_Catalog::product/list/addto/compare.phtml)
    * Added project detector
    * Move configuration section to "Languages & Frameworks > Php > Magento"
    * Remove deprecated elements
    
0.2.2
============= 
* Features:
    * Added Module name to configuration tooltip
* Fixed bugs:
    * Fixed "Project disposed" exception
   
0.2.1
=============    
* Features:
    * added module name for "Goto configuration" labels
    
0.2.0
=============
* Features:
    * WebApi routes
    * nicer "Goto configuration" labels
    * plugin settings (manual reindex, URN generation, plugin on/off)
 
0.1
=============  
* Features:
    * Context type completion for:
        * Observers completion only for ObserverInterface impl in events.xml
        * Blocks completion only for BlockInterface name in layouts.xml
        * Preference configuration in di.xml
        * Type hinting for object arguments in di.xml
    * @api usage inspection in Module context
    * ObjectManager usage inspection in Module context
    * virtualType arguments resolution
    * webapi.xml interface/method completion/references
    * Support for old people using PhpStorm 8 or JDK1.7
    
0.0.9
=============    
* Features:
    * Added Reference and completion support for layouts
        * block: class, before, after
        * referenceBlock: name
        * move: element, destination, before, after
        * remove: name
        * update: handle
        * referenceContainer: name
    * Line marker reference for php class to Layout configuration

0.0.8
=============
* Features:
    * Added Line marker reference for php class/interface to DI configuration
    * Added Line marker reference to plugins

0.0.7
=============
* Features:
    * Added reference to configuration and observers (classes or virtualType)
    * Added reference to observers from configuration
    * Added reference to event dispatch from configuration
    
0.0.6
=============
* Features:
    * Added reference and completion support for virtual types/classes/arguments in DI configuration
    
0.0.5
=============
* Features:
    * Added reference support for classes/interfaces in DI configuration