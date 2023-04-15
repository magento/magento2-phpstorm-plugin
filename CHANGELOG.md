# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0).

## 5.2.0

## 5.1.1

### Fixed

- java.lang.NoClassDefFoundError: org/codehaus/plexus/util/StringUtils [#1530](https://github.com/magento/magento2-phpstorm-plugin/pull/1530)
- java.lang.Throwable: Must not start write action from within read action in the other thread - deadlock is coming [#1531](https://github.com/magento/magento2-phpstorm-plugin/pull/1531)
- Custom theme detection [#1532](https://github.com/magento/magento2-phpstorm-plugin/pull/1532)

## 5.1.0

### Fixed

- Fixed the parsing of plugin sort order value[#1235](https://github.com/magento/magento2-phpstorm-plugin/pull/1235)
- Fixed compatibility with 2023.1 [#1214](https://github.com/magento/magento2-phpstorm-plugin/pull/1214)

## 5.0.1

### Fixed

- Throwable: Stub index points to a file without PSI [#1232](https://github.com/magento/magento2-phpstorm-plugin/pull/1232)
- Create an entity - The delete button is displayed in a new entity form [#1268](https://github.com/magento/magento2-phpstorm-plugin/pull/1268)
- Create an entity - The translation function is not call on the buttons label [#1273](https://github.com/magento/magento2-phpstorm-plugin/pull/1273)
- Covered possible NullPointerException in InjectAViewModelDialog.java [#1213](https://github.com/magento/magento2-phpstorm-plugin/pull/1213)
- AWT events are not allowed inside write action [#1271](https://github.com/magento/magento2-phpstorm-plugin/pull/1271)
- MapReduceIndexMappingException: java.lang.NumberFormatException: For input string: "" [#1235](https://github.com/magento/magento2-phpstorm-plugin/pull/1235)

## 5.0.0

### Added

- Code generation of a Data Patch Boilerplate file [#1188](https://github.com/magento/magento2-phpstorm-plugin/pull/1188)
- Code generation of an Observer from the context menu [#1200](https://github.com/magento/magento2-phpstorm-plugin/pull/1200)
- Code generation of events.xml file [#1189](https://github.com/magento/magento2-phpstorm-plugin/pull/1189)
- Config Scope directory inspection [#1261](https://github.com/magento/magento2-phpstorm-plugin/pull/1261)

### Changed

- EAV attributes code generators: Added default values of the group property [#1259](https://github.com/magento/magento2-phpstorm-plugin/pull/1259)
- All code generators: Added constants visibility and class property types [#1260](https://github.com/magento/magento2-phpstorm-plugin/pull/1260)
- UCT custom coming versions [#1251](https://github.com/magento/magento2-phpstorm-plugin/pull/1251)

### Fixed

- Fixed wrong director(y|ies) generation for GraphQL resolver class [#1192](https://github.com/magento/magento2-phpstorm-plugin/pull/1192)
- Fixed IndexOutOfBoundsException: CreateResolverClassQuickFix.applyFix(CreateResolverClassQuickFix.java:43) [#1192](https://github.com/magento/magento2-phpstorm-plugin/pull/1192)
- Index out of range [#1239](https://github.com/magento/magento2-phpstorm-plugin/pull/1239)

## 4.4.0

### Added

- Code generation of a Data Patch file with a Customer EAV attribute [#583](https://github.com/magento/magento2-phpstorm-plugin/pull/583)
- Code generation of a Data Patch file with a Product EAV attribute [#527](https://github.com/magento/magento2-phpstorm-plugin/pull/527)
- Code generation of a Data Patch file with a Category EAV attribute [#569](https://github.com/magento/magento2-phpstorm-plugin/pull/569)
- Code generation of Readme file [#1133](https://github.com/magento/magento2-phpstorm-plugin/pull/1133)
- Code generation of GraphQl schema file [#1123](https://github.com/magento/magento2-phpstorm-plugin/pull/1123)
- Optional generation of Readme file during the creation of a new module [#1110](https://github.com/magento/magento2-phpstorm-plugin/pull/1110)
- Code completion for `system.xml` and `config.xml` [#1077](https://github.com/magento/magento2-phpstorm-plugin/pull/1077)
- Added easier navigation through plugins [#1121](https://github.com/magento/magento2-phpstorm-plugin/pull/1121)
- Added inspection to check if type attr value in the virtual type tag attribute value exists [#1176](https://github.com/magento/magento2-phpstorm-plugin/pull/1176)
- Added checks and detailed error messages during plugin activation [#1181](https://github.com/magento/magento2-phpstorm-plugin/pull/1181)

### Fixed

- Fixed NullPointerException at ObserverDeclarationInspection.java:188 [#1143](https://github.com/magento/magento2-phpstorm-plugin/issues/1143)
- Fixed IncorrectOperationException: Rebind cannot be performed for class PolyVariantReferenceBase [#1173](https://github.com/magento/magento2-phpstorm-plugin/pull/1173)
- Fixed create an observer for an event doesn't work through the context menu [#1166](https://github.com/magento/magento2-phpstorm-plugin/pull/1166)
- Fixed IOException: Invalid file name at ReportBuilder [#1154](https://github.com/magento/magento2-phpstorm-plugin/pull/1154)
- Fixed IllegalArgumentException in NewModuleAction class [#1150](https://github.com/magento/magento2-phpstorm-plugin/pull/1150)
- Fixed null data in ModuleIndex class [#1132](https://github.com/magento/magento2-phpstorm-plugin/pull/1132)
- Fixed StringIndexOutOfBoundsException: GitHubNewIssueBodyBuilderUtil [#1130](https://github.com/magento/magento2-phpstorm-plugin/pull/1130)
- Fixed ArrayIndexOutOfBoundsException: Index 1 out of bounds for length 0 in OverrideClassByAPreferenceDialog [#1129](https://github.com/magento/magento2-phpstorm-plugin/pull/1129)
- Fixed PatternSyntaxException: MagentoBasePathUtil.isMagentoFolderValid:35 for Windows styled dir path separator [#1126](https://github.com/magento/magento2-phpstorm-plugin/pull/1126)
- Fixed NullPointerException in the OverrideTemplateInThemeAction.isOverrideAllowed for virtualFile.getCanonicalPath() [#1125](https://github.com/magento/magento2-phpstorm-plugin/pull/1125)
- Fixed IllegalArgumentException: Argument for @NotNull parameter 'dataKey' must not be null in CompareTemplateAction [#1117](https://github.com/magento/magento2-phpstorm-plugin/pull/1117)
- Fixed argument for @NotNull parameter 'project' must not be null in the OverrideClassByAPreferenceAction [#1116](https://github.com/magento/magento2-phpstorm-plugin/pull/1116)
- Fixed New layout action doesn't accept valid layout names [#1114](https://github.com/magento/magento2-phpstorm-plugin/pull/1114)

## 4.3.1

### Changed

- Added raw plugin verifier configuration in [#1065](https://github.com/magento/magento2-phpstorm-plugin/pull/1065)

### Fixed

- Fixed bug with the file separator on Windows OS (while saving plugin settings) in [#1062](https://github.com/magento/magento2-phpstorm-plugin/pull/1062)
- Fixed bug with wrong text range for FilePathReferenceProvider.getReferencesByElement in [#1063](https://github.com/magento/magento2-phpstorm-plugin/pull/1063)
- Fixed module files action group is accessible from the theme context in [#1064](https://github.com/magento/magento2-phpstorm-plugin/pull/1064)
- Fixed bug with directory index is already disposed for Project in AllFilesExceptTestsScope.contains in [#1080](https://github.com/magento/magento2-phpstorm-plugin/pull/1080)
- Fixed bug with DumbService cannot be created because container is already disposed in MagentoComponentManager.getComponents in [#1081](https://github.com/magento/magento2-phpstorm-plugin/pull/1081)

## 4.3.0

### Added

- Code generation of `layout.xml` file [#1021](https://github.com/magento/magento2-phpstorm-plugin/pull/1021)
- Code generation of `page_types.xml` file [#1003](https://github.com/magento/magento2-phpstorm-plugin/pull/1003)
- Code generation of `crontab.xml file` [#1001](https://github.com/magento/magento2-phpstorm-plugin/pull/1001)
- Code generation of `email_templates.xml` file [#998](https://github.com/magento/magento2-phpstorm-plugin/pull/998)
- Code generation of `sections.xml` file [#997](https://github.com/magento/magento2-phpstorm-plugin/pull/997)
- Code generation of `fieldset.xml` file [#996](https://github.com/magento/magento2-phpstorm-plugin/pull/996)
- Code generation of `view.xml` file [#990](https://github.com/magento/magento2-phpstorm-plugin/pull/990)
- Code generation of `indexer.xml` file [#988](https://github.com/magento/magento2-phpstorm-plugin/pull/988)
- Code generation of `mview.xml` file [#987](https://github.com/magento/magento2-phpstorm-plugin/pull/987)
- Code generation of `widget.xml` file [#983](https://github.com/magento/magento2-phpstorm-plugin/pull/983)
- Code generation of `extension_attributes.xml` file [#982](https://github.com/magento/magento2-phpstorm-plugin/pull/982)
- Code generation of `system.xml` file [#978](https://github.com/magento/magento2-phpstorm-plugin/pull/978)
- Code generation of `config.xml` file [#976](https://github.com/magento/magento2-phpstorm-plugin/pull/976)
- Code generation of `webapi.xml` file [#971](https://github.com/magento/magento2-phpstorm-plugin/pull/971)
- Code generation of `di.xml` file [#970](https://github.com/magento/magento2-phpstorm-plugin/pull/970)
- Code generation of `acl.xml` file [#969](https://github.com/magento/magento2-phpstorm-plugin/pull/969)
- Code generation of `routes.xml` file [#958](https://github.com/magento/magento2-phpstorm-plugin/pull/958)
- Images support for Copy Magento Path [#1020](https://github.com/magento/magento2-phpstorm-plugin/pull/1020)
- Add/Replace an argument of a constructor via di.xml [#1027](https://github.com/magento/magento2-phpstorm-plugin/pull/1027)
- Possibility to compare overridden template with the original one [#1032](https://github.com/magento/magento2-phpstorm-plugin/pull/1032)
- Themes support of the UCT action execution [#1029](https://github.com/magento/magento2-phpstorm-plugin/pull/1029)
- Configuration files support of the UCT action execution [#1038](https://github.com/magento/magento2-phpstorm-plugin/pull/1038)
- Possibility to override a LESS file [#1036](https://github.com/magento/magento2-phpstorm-plugin/pull/1036)
- Added references for the extended MFTF tests [#974](https://github.com/magento/magento2-phpstorm-plugin/pull/974)

### Changed

- Improved RequireJS Mapping [#1045](https://github.com/magento/magento2-phpstorm-plugin/pull/1045)
- Improved the override a theme file feature [#1046](https://github.com/magento/magento2-phpstorm-plugin/pull/1046)
- Improved DocBlock code generator [#1022](https://github.com/magento/magento2-phpstorm-plugin/pull/1022)
- Added the possibility to create a plugin for a method in the parent class [#981](https://github.com/magento/magento2-phpstorm-plugin/pull/981)
- Extended custom search scope (to exclude test files from search) with the `is integration enabled` flag [#944](https://github.com/magento/magento2-phpstorm-plugin/pull/944)
- Extended uiComponent highlighting with the `is integration enabled` flag [#942](https://github.com/magento/magento2-phpstorm-plugin/pull/942)
- Added "module" prefix to module name in `composer.json` [#848](https://github.com/magento/magento2-phpstorm-plugin/pull/848)

### Fixed

- Sort order of the context actions [#1004](https://github.com/magento/magento2-phpstorm-plugin/pull/1004)
- Fixed the email template form title [#956](https://github.com/magento/magento2-phpstorm-plugin/pull/956)
- Placeholders on forms [#1009](https://github.com/magento/magento2-phpstorm-plugin/pull/1009) [#1008](https://github.com/magento/magento2-phpstorm-plugin/pull/1008) [#1005](https://github.com/magento/magento2-phpstorm-plugin/pull/1005) [#962](https://github.com/magento/magento2-phpstorm-plugin/pull/962) [#938](https://github.com/magento/magento2-phpstorm-plugin/pull/938)
- Creating new module with the package name in the valid package directory on Windows OS [#1013](https://github.com/magento/magento2-phpstorm-plugin/pull/1013)
- Inspection titles [#1015](https://github.com/magento/magento2-phpstorm-plugin/pull/1015)
- Fixed ArrayIndexOutOfBoundsException [#1018](https://github.com/magento/magento2-phpstorm-plugin/pull/1018)
- Generating UI form Delete button [#1019](https://github.com/magento/magento2-phpstorm-plugin/pull/1019)
- Removed deprecated method FilenameIndex#getVirtualFilesByName [#1037](https://github.com/magento/magento2-phpstorm-plugin/pull/1037)
- Removed deprecated method Function#getReturnType [#1043](https://github.com/magento/magento2-phpstorm-plugin/pull/1043)
- Issue with wrong references resolved for a class if a proxy class is generated for it [#1044](https://github.com/magento/magento2-phpstorm-plugin/pull/1044)
- Fixed Magento installation path suggestion [#1047](https://github.com/magento/magento2-phpstorm-plugin/pull/1047)
- Issue during composer json generation if in dependent module version tag is not specified [#972](https://github.com/magento/magento2-phpstorm-plugin/pull/972)

## 4.2.3

### Fixed

- Fixed a simple-json library is absent in the distribution in [#951](https://github.com/magento/magento2-phpstorm-plugin/pull/951)

## 4.2.2

### Added

- Added custom search scope to exclude test files from search [#read-more](https://github.com/magento/magento2-phpstorm-plugin/wiki/4.2.2-Release)
- Added the ability to open all plugins in the Find Tool Window [#read-more](https://github.com/magento/magento2-phpstorm-plugin/wiki/4.2.2-Release)
- Added uiComponent registration syntax highlighting as JSON [#read-more](https://github.com/magento/magento2-phpstorm-plugin/wiki/4.2.2-Release)
- Added new Magento version support (2.4.4-beta4) for the UCT feature in [#922](https://github.com/magento/magento2-phpstorm-plugin/pull/922)

### Changed

- Eliminated com.google.gson.JsonParser#parse usage in [#928](https://github.com/magento/magento2-phpstorm-plugin/pull/928)
- Eliminated LineMarkerXmlTagDecorator#checkDelete usage in [#910](https://github.com/magento/magento2-phpstorm-plugin/pull/910)
- Eliminated LineMarkerXmlTagDecorator#checkAdd usage in [#909](https://github.com/magento/magento2-phpstorm-plugin/pull/909)
- Enhanced errors showing for the new Magento 2 UI Component Form dialog window in [#892](https://github.com/magento/magento2-phpstorm-plugin/pull/892)
- Enhanced errors showing for the new Magento 2 UI Component Grid dialog window in [#891](https://github.com/magento/magento2-phpstorm-plugin/pull/891)
- Enhanced errors showing for the new Magento 2 DB Schema XML dialog window in [#890](https://github.com/magento/magento2-phpstorm-plugin/pull/890)
- Enhanced errors showing for the new Magento 2 Message Queue dialog window in [#889](https://github.com/magento/magento2-phpstorm-plugin/pull/889)
- Enhanced errors showing for the new Magento 2 Data Model dialog window in [#888](https://github.com/magento/magento2-phpstorm-plugin/pull/888)
- Enhanced errors showing for the new Magento 2 Models dialog window in [#887](https://github.com/magento/magento2-phpstorm-plugin/pull/887)
- Enhanced errors showing for the new Magento 2 Email Template dialog window in [#886](https://github.com/magento/magento2-phpstorm-plugin/pull/886)
- Enhanced errors showing for the new Magento 2 CLI Command dialog window in [#885](https://github.com/magento/magento2-phpstorm-plugin/pull/885)
- Enhanced errors showing for the new Magento 2 GraphQl Resolver dialog window in [#884](https://github.com/magento/magento2-phpstorm-plugin/pull/884)
- Enhanced errors showing for the new Magento 2 View Model dialog window in [#883](https://github.com/magento/magento2-phpstorm-plugin/pull/883)
- Enhanced errors showing for the new Magento 2 Cron Group dialog window in [#882](https://github.com/magento/magento2-phpstorm-plugin/pull/882)
- Enhanced errors showing for the new Magento 2 Cron Job dialog window in [#881](https://github.com/magento/magento2-phpstorm-plugin/pull/881)
- Enhanced errors showing for the new Magento 2 Controller dialog window in [#880](https://github.com/magento/magento2-phpstorm-plugin/pull/880)
- Enhanced errors showing for the new Magento 2 Block dialog window in [#865](https://github.com/magento/magento2-phpstorm-plugin/pull/865)

### Fixed

- Fixed overriding layouts and templates allows to select an incompatible theme in [#930](https://github.com/magento/magento2-phpstorm-plugin/pull/930)
- Fixed observer action visibility in [#936](https://github.com/magento/magento2-phpstorm-plugin/pull/936)
- Fixed general exception while accessing phpIndex.getAnyByFQN(classFQN) in [#934](https://github.com/magento/magento2-phpstorm-plugin/pull/934)
- Fixed ClassCastException for the GraphQlResolverIndex.getGraphQLUsages in [#927](https://github.com/magento/magento2-phpstorm-plugin/pull/927)
- Fixed plugin inspection does not work correctly in [#902](https://github.com/magento/magento2-phpstorm-plugin/pull/902)
- Fixed module name declaration inspection flags valid code in [#898](https://github.com/magento/magento2-phpstorm-plugin/pull/898)
- Fixed ClassCastException: PsiPlainTextFileImpl cannot be cast to class XmlFile in WebApiTypeIndex in [#897](https://github.com/magento/magento2-phpstorm-plugin/pull/897)
- Fixed IndexNotReadyException while opening a context menu in [#896](https://github.com/magento/magento2-phpstorm-plugin/pull/896)
- Fixed IllegalArgumentException: URLDecoder: Incomplete trailing escape (%) pattern in [#894](https://github.com/magento/magento2-phpstorm-plugin/pull/894)
- Fixed Throwable: AWT events are not allowed inside write action (UI Form) in [#893](https://github.com/magento/magento2-phpstorm-plugin/pull/893)
- Fixed Slow operations are prohibited on EDT when calling update method on the CreateAPluginAction in [#862](https://github.com/magento/magento2-phpstorm-plugin/pull/862)
- Fixed Throwable: when an IOException is thrown during new plugin generation in [#860](https://github.com/magento/magento2-phpstorm-plugin/pull/860)
- Fixed IndexOutOfBoundsException in case when entered no fields during ui form generation in [#859](https://github.com/magento/magento2-phpstorm-plugin/pull/859)
- Fixed NullPointerException when opening new cron job dialog for an invalid Magento 2 module in [#856](https://github.com/magento/magento2-phpstorm-plugin/pull/856)
- Fixed NoSuchElementException when creating new controller in [#844](https://github.com/magento/magento2-phpstorm-plugin/pull/844)
- Fixed Message Queue Connection does not match Naming Conventions in [#854](https://github.com/magento/magento2-phpstorm-plugin/pull/854)
- Fixed StringIndexOutOfBoundsException during UCT inspection via action execution in [852](https://github.com/magento/magento2-phpstorm-plugin/pull/852)
- Fixed NullPointerException in the PluginDeclarationInspection.fetchModuleNamesWhereSamePluginNameUsed (calling toString on null) in [#849](https://github.com/magento/magento2-phpstorm-plugin/pull/849)

## 4.2.1

### Fixed

- Fixed ArrayIndexOutOfBoundsException when used Copy Path/Reference action on the requirejs-config.js file in [#808](https://github.com/magento/magento2-phpstorm-plugin/pull/808)
- Fixed dialog disposition issue in [#811](https://github.com/magento/magento2-phpstorm-plugin/pull/811)
- Fixed stack overflow error for line-marker providers in [#813](https://github.com/magento/magento2-phpstorm-plugin/pull/813)
- Fixed stub ids not found for key in index in [#814](https://github.com/magento/magento2-phpstorm-plugin/pull/814)
- Fixed failed to build require-js index in [815](https://github.com/magento/magento2-phpstorm-plugin/pull/815)
- Fixed empty psi elements in the plugin inspections in [#816](https://github.com/magento/magento2-phpstorm-plugin/pull/816)
- Fixed empty psi element in module xml inspection in [#817](https://github.com/magento/magento2-phpstorm-plugin/pull/817)
- Fixed NullPointerException for the firstSortOrder.compareTo in [#818](https://github.com/magento/magento2-phpstorm-plugin/pull/818)
- Fixed invalid column types provided exception during db_schema.xml file generation in [#819](https://github.com/magento/magento2-phpstorm-plugin/pull/819)
- Fixed method designed for fully qualified names only in [#830](https://github.com/magento/magento2-phpstorm-plugin/pull/830)
- Fixed NoClassDefFoundError for com.intellij.lang.jsgraphql.GraphQLIcons in [#850](https://github.com/magento/magento2-phpstorm-plugin/pull/850)

## 4.2.0

### Added

- Extending of the built-in Upgrade Compatibility Tool [#read-more](https://github.com/magento/magento2-phpstorm-plugin/wiki/4.2.0-Release)

### Changed

- Enhanced Magento 2 version resolving in [#747](https://github.com/magento/magento2-phpstorm-plugin/pull/747), [#751](https://github.com/magento/magento2-phpstorm-plugin/pull/751), [#777](https://github.com/magento/magento2-phpstorm-plugin/pull/777)

### Fixed

- Fixed NullPointerException for the InjectAViewModelAction in [#800](https://github.com/magento/magento2-phpstorm-plugin/pull/800)
- Fixed NullPointerException for a name attribute of the event tag and code style fixes in [#799](https://github.com/magento/magento2-phpstorm-plugin/pull/799)
- Fixed NullPointerException for PluginReferenceProvider in [#801](https://github.com/magento/magento2-phpstorm-plugin/pull/801)

## 4.1.0

### Added

- Added UI integration with the Adobe Commerce Upgrade Compatibility Tool in [#625](https://github.com/magento/magento2-phpstorm-plugin/pull/625)
- Added MVP version of the built-in Upgrade Compatibility Tool, read more in [wiki](https://github.com/magento/magento2-phpstorm-plugin/wiki/4.1.0-Release#the-built-in-upgrade-compatibility-tool)

## 4.0.0

### Added

- Added XML file header include code template in [#615](https://github.com/magento/magento2-phpstorm-plugin/pull/615)
- Added Web API generation for the Magento Entity Creator in [#597](https://github.com/magento/magento2-phpstorm-plugin/pull/597) and [#607](https://github.com/magento/magento2-phpstorm-plugin/pull/607)
- Added DI XML plugin type attribute inspections in [#588](https://github.com/magento/magento2-phpstorm-plugin/pull/588)
- Added Web API interface for service (PHP class) generation in [#586](https://github.com/magento/magento2-phpstorm-plugin/pull/586)
- Added DI XML type tag attributes inspections that related to the PHP/Magento types in [#582](https://github.com/magento/magento2-phpstorm-plugin/pull/582)
- Added DI XML preference tag attributes inspections in [#578](https://github.com/magento/magento2-phpstorm-plugin/pull/578)
- Added Web API XML service tag attributes inspections in [#577](https://github.com/magento/magento2-phpstorm-plugin/pull/577)
- Added an error handler to help user with a new bug issue creation on the GitHub side in [#552](https://github.com/magento/magento2-phpstorm-plugin/pull/552) and [#593](https://github.com/magento/magento2-phpstorm-plugin/pull/593)
- Added Web API declaration generation in [#548](https://github.com/magento/magento2-phpstorm-plugin/pull/548) and [#595](https://github.com/magento/magento2-phpstorm-plugin/pull/595)
- Added JS and CSS support for Copy Magento Path action in [#536](https://github.com/magento/magento2-phpstorm-plugin/pull/536)

### Changed

- Changed the content of the generated plugin class in [#612](https://github.com/magento/magento2-phpstorm-plugin/pull/612)
- Changed using of hardcoded entity id value into the constant in all files generated by the Entity Creator in [#606](https://github.com/magento/magento2-phpstorm-plugin/pull/606)

### Fixed

- Fixed a casting exception in the XML index in [#617](https://github.com/magento/magento2-phpstorm-plugin/pull/617)
- Fixed a bug with plugin generation for complex non-primitive types in [#609](https://github.com/magento/magento2-phpstorm-plugin/pull/609)
- Fixed a bug with the namespace generation in a generated controller in [#571](https://github.com/magento/magento2-phpstorm-plugin/pull/571)
- Fixed a bug with directory validation for the generation dialogues in [#565](https://github.com/magento/magento2-phpstorm-plugin/pull/565)
- Fixed wrong entity data mapper file template position in [#549](https://github.com/magento/magento2-phpstorm-plugin/pull/549)
- Fixed incorrect data saving into table model within editing a table in [#544](https://github.com/magento/magento2-phpstorm-plugin/pull/544)
- Fixed an error when plugin or observer name is not set in [#533](https://github.com/magento/magento2-phpstorm-plugin/pull/533)
- Fixed incorrect duplication warning for disabled plugin in di.xml in [#529](https://github.com/magento/magento2-phpstorm-plugin/pull/529)

## 3.2.2

### Fixed

- New entity layout name and edit entity layout name inside it
- `Java.lang.IllegalArgumentException` During adding a new method into an existing plugin

## 3.2.1

### Fixed

- Directory validator
- Entity data mapper file template

## 3.2.0

### Added

- Code generation for a Magento Entity in [#521](https://github.com/magento/magento2-phpstorm-plugin/pull/521)
- Code generation for email templates in [#350](https://github.com/magento/magento2-phpstorm-plugin/pull/350)
- Reference navigation for disabled observers in `events.xml` in [#439](https://github.com/magento/magento2-phpstorm-plugin/pull/439)
- Line markers for test fixtures in [#477](https://github.com/magento/magento2-phpstorm-plugin/pull/477)

### Changed

- Added ability to set the module sequence at generating new module [#266](https://github.com/magento/magento2-phpstorm-plugin/pull/266)

### Fixed

- ArrayIndexOutOfBoundsException in the New Module Action in [#519](https://github.com/magento/magento2-phpstorm-plugin/pull/519)

## 3.1.3

### Changed

- Require restart on plugin update due to using native libraries

### Fixed

- Class completion doesn't display interfaces
- Fixed invalid check 'setup_version' in the etc/module.xml

## 3.1.2

### Fixed

- Reference navigation for classes under directories with underscores
- Fixed the array access exception when using copy path action

## 3.1.1

### Fixed

- Fixed null pointer exception on the copy path action

## 3.1.0

### Added

- Extended `.phpstorm.meta.php` for more convenient autocomplete [#467](https://github.com/magento/magento2-phpstorm-plugin/pull/467)
- Code generation for message queue in [#411](https://github.com/magento/magento2-phpstorm-plugin/pull/411)
- Code generation for declarative schema [#453](https://github.com/magento/magento2-phpstorm-plugin/pull/453)
- Inspection warning for disabled observer [#432](https://github.com/magento/magento2-phpstorm-plugin/pull/432)
- The action item to the context menu to copy file path in the Magento format [#451](https://github.com/magento/magento2-phpstorm-plugin/pull/451)

### Fixed

- The null pointer exception on the Create Module Dialog

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
