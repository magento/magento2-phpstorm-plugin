1.0.0
============= 
* Features:
    * "Go to GraphQL schema" line marker in scope of class/interface and method
    * "Go to GraphQL resolver class" line marke in scope of GraphQL schema type arguments
    * RequireJS mapping support (reference navigation, completion)
    * Plugin class methods generation
    * Plugin declaration inspection in scope of a Plugin Class
    * MFTF support (reference navigation, completion)
    * Fixed support of 2020.* versions of IDE's
    * Create a New Magento 2 Module action
    * Code Inspection: Duplicated Observer Usage in events XML
    * Create a Plugin class for a class public method action

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