/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.mcp

import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.jetbrains.php.lang.psi.PhpFile
import com.magento.idea.magento2plugin.actions.generation.context.EntityCreatorContext
import com.magento.idea.magento2plugin.actions.generation.data.UiComponentFormButtonData
import com.magento.idea.magento2plugin.actions.generation.data.UiComponentFormFieldData
import com.magento.idea.magento2plugin.actions.generation.data.UiComponentFormFieldsetData
import com.magento.idea.magento2plugin.actions.generation.data.converter.newentitydialog.*
import com.magento.idea.magento2plugin.actions.generation.data.dialog.EntityCreatorContextData
import com.magento.idea.magento2plugin.actions.generation.data.dialog.NewEntityDialogData
import com.magento.idea.magento2plugin.actions.generation.dialog.util.ClassPropertyFormatterUtil
import com.magento.idea.magento2plugin.actions.generation.generator.*
import com.magento.idea.magento2plugin.actions.generation.generator.php.SearchResultsGenerator
import com.magento.idea.magento2plugin.actions.generation.generator.php.SearchResultsInterfaceGenerator
import com.magento.idea.magento2plugin.actions.generation.generator.php.WebApiInterfaceWithDeclarationGenerator
import com.magento.idea.magento2plugin.actions.generation.generator.util.DbSchemaGeneratorUtil
import com.magento.idea.magento2plugin.actions.generation.generator.util.NamespaceBuilder
import com.magento.idea.magento2plugin.actions.generation.util.GenerationContextRegistry
import com.magento.idea.magento2plugin.magento.files.*
import com.magento.idea.magento2plugin.magento.files.actions.*
import com.magento.idea.magento2plugin.magento.files.commands.DeleteEntityByIdCommandFile
import com.magento.idea.magento2plugin.magento.files.commands.SaveEntityCommandFile
import com.magento.idea.magento2plugin.magento.files.queries.GetListQueryFile
import com.magento.idea.magento2plugin.magento.packages.Areas
import com.magento.idea.magento2plugin.magento.packages.File
import com.magento.idea.magento2plugin.magento.packages.HttpMethod
import com.magento.idea.magento2plugin.magento.packages.Package
import com.magento.idea.magento2plugin.magento.packages.PropertiesTypes
import com.magento.idea.magento2plugin.magento.packages.uicomponent.FormElementType
import com.magento.idea.magento2plugin.util.CamelCaseToSnakeCase
import com.magento.idea.magento2plugin.util.GetFirstClassOfFile
import com.magento.idea.magento2plugin.util.GetPhpClassByFQN
import com.magento.idea.magento2plugin.util.RegExUtil
import com.magento.idea.magento2plugin.util.php.PhpTypeMetadataParserUtil
import java.util.Locale

internal object MagentoEntityCrudCommands {
    private const val ACTION_NAME = "Create Entity CRUD"
    private const val DEFAULT_TABLE_ENGINE = "innodb"
    private const val DEFAULT_TABLE_RESOURCE = "default"
    private const val DEFAULT_MENU_SORT_ORDER = 100
    private const val ADMINHTML = "adminhtml"
    private const val PROPERTY_NAME_KEY = "Name"
    private const val PROPERTY_TYPE_KEY = "Type"

    private val ENTITY_NAME_PATTERN = Regex(RegExUtil.ALPHANUMERIC)
    private val TABLE_NAME_PATTERN = Regex(RegExUtil.LOWER_SNAKE_CASE)
    private val IDENTIFIER_PATTERN = Regex(RegExUtil.IDENTIFIER)
    private val PROPERTY_DEFINITION_PATTERN = Regex("^([a-z][a-z0-9_]*):(int|float|string|bool)$")

    fun createMagentoEntityCrud(
        project: Project,
        moduleName: String,
        entityName: String,
        tableName: String,
        idFieldName: String,
        properties: List<String>,
        createAdminUiComponents: Boolean,
        createDataInterface: Boolean,
        createWebApi: Boolean
    ): String {
        val request = try {
            MagentoMcpCreateSupport.runReadAction {
                resolveRequest(
                    project = project,
                    moduleName = moduleName,
                    entityName = entityName,
                    tableName = tableName,
                    idFieldName = idFieldName,
                    properties = properties,
                    createAdminUiComponents = createAdminUiComponents,
                    createDataInterface = createDataInterface,
                    createWebApi = createWebApi
                )
            }
        } catch (_: ReadAction.CannotReadException) {
            return "The request was cancelled by a pending write action. Retry."
        } catch (exception: EntityCrudValidationException) {
            return exception.message ?: "Entity CRUD creation request is invalid."
        }

        val transaction = MagentoMcpCreateSupport.FileTransaction(project, request.moduleContext.moduleDirectory)
        request.expectedRelativePaths.forEach(transaction::track)

        val contextRegistry = GenerationContextRegistry.getInstance()
        val previousContext = contextRegistry.context
        return try {
            val generationContext = EntityCreatorContext().apply {
                putUserData(
                    EntityCreatorContext.DTO_TYPE,
                    if (request.dialogData.hasDtoInterface()) {
                        request.contextData.dtoInterfaceNamespaceBuilder.classFqn
                    } else {
                        request.contextData.dtoModelNamespaceBuilder.classFqn
                    }
                )
                putUserData(EntityCreatorContext.ENTITY_ID, request.dialogData.idFieldName)
            }
            contextRegistry.setContext(generationContext)

            generateCrudScaffold(project, request)

            val missingPaths = request.expectedRelativePaths.filterNot { relativePath ->
                request.moduleContext.moduleDirectory.virtualFile.findFileByRelativePath(relativePath) != null
            }
            if (missingPaths.isNotEmpty()) {
                transaction.rollback()
                return "Magento entity CRUD scaffold \"${request.entityName}\" could not be created completely. Missing files after generation: ${missingPaths.joinToString(", ")}. Partial changes were rolled back."
            }

            buildSuccessMessage(project, request)
        } catch (exception: EntityCrudGenerationException) {
            transaction.rollback()
            exception.message ?: "Magento entity CRUD scaffold \"${request.entityName}\" could not be created. Partial changes were rolled back."
        } catch (exception: Throwable) {
            transaction.rollback()
            "Magento entity CRUD scaffold \"${request.entityName}\" could not be created. Partial changes were rolled back. Reason: ${exception.message ?: exception::class.java.simpleName}"
        } finally {
            contextRegistry.setContext(previousContext)
        }
    }

    private fun resolveRequest(
        project: Project,
        moduleName: String,
        entityName: String,
        tableName: String,
        idFieldName: String,
        properties: List<String>,
        createAdminUiComponents: Boolean,
        createDataInterface: Boolean,
        createWebApi: Boolean
    ): EntityCrudRequest {
        val moduleContext = MagentoMcpCreateSupport.requireEditableModule(project, moduleName) {
            throw EntityCrudValidationException(it)
        }

        val normalizedEntityName = entityName.trim()
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        if (normalizedEntityName.isEmpty()) {
            throw EntityCrudValidationException("Provide a non-empty entityName value.")
        }
        if (!ENTITY_NAME_PATTERN.matches(normalizedEntityName)) {
            throw EntityCrudValidationException("entityName must contain only letters and numbers.")
        }

        val snakeCaseName = CamelCaseToSnakeCase.getInstance().convert(normalizedEntityName)
        val normalizedTableName = tableName.trim().ifEmpty { snakeCaseName }
        if (!TABLE_NAME_PATTERN.matches(normalizedTableName)) {
            throw EntityCrudValidationException("tableName must use lower_snake_case.")
        }
        if (normalizedTableName.length > 64) {
            throw EntityCrudValidationException("tableName must be 64 characters or fewer.")
        }

        val normalizedIdFieldName = idFieldName.trim().ifEmpty { "${snakeCaseName}_id" }
        if (!IDENTIFIER_PATTERN.matches(normalizedIdFieldName)) {
            throw EntityCrudValidationException("idFieldName may contain only letters, numbers, underscores, and hyphens.")
        }

        val propertySpecs = properties.mapIndexed { index, propertyDefinition ->
            parsePropertyDefinition(propertyDefinition, index + 1)
        }

        val label = buildLabelFromSnakeCase(snakeCaseName)
        val derivedRoute = snakeCaseName
        val derivedFormLabel = "$label Form"
        val derivedFormName = "${snakeCaseName}_form"
        val derivedGridName = "${snakeCaseName}_listing"
        val derivedAclId = "${moduleContext.moduleName}::management"
        val derivedAclTitle = "$label Management"
        val derivedMenuId = derivedAclId
        val derivedMenuTitle = derivedAclTitle

        val formattedProperties = mutableListOf(
            ClassPropertyFormatterUtil.formatSingleProperty(normalizedIdFieldName, PropertiesTypes.INT.propertyType)
        )
        propertySpecs.forEach { spec ->
            formattedProperties += ClassPropertyFormatterUtil.formatSingleProperty(spec.name, spec.type)
        }

        val shortEntityProperties = propertySpecs.map { spec ->
            linkedMapOf(
                PROPERTY_NAME_KEY to spec.name,
                PROPERTY_TYPE_KEY to spec.type
            )
        }
        val entityProperties = DbSchemaGeneratorUtil.complementShortPropertiesByDefaults(shortEntityProperties)
            .toMutableList()
        entityProperties.add(0, DbSchemaGeneratorUtil.getTableIdentityColumnData(normalizedIdFieldName))

        val fieldsets = listOf(UiComponentFormFieldsetData("general", "General", "10"))
        val fields = buildFields(normalizedIdFieldName, propertySpecs)
        val buttons = buildButtons(moduleContext.moduleName, normalizedEntityName, derivedFormName)

        val dialogData = NewEntityDialogData(
            normalizedEntityName,
            normalizedTableName,
            normalizedIdFieldName,
            DEFAULT_TABLE_ENGINE,
            DEFAULT_TABLE_RESOURCE,
            createAdminUiComponents,
            createDataInterface,
            createWebApi,
            derivedRoute,
            derivedFormLabel,
            derivedFormName,
            derivedGridName,
            createAdminUiComponents,
            createAdminUiComponents,
            createAdminUiComponents,
            createAdminUiComponents,
            createAdminUiComponents,
            ModuleMenuXml.defaultAcl,
            derivedAclId,
            derivedAclTitle,
            "",
            DEFAULT_MENU_SORT_ORDER,
            derivedMenuId,
            derivedMenuTitle,
            ClassPropertyFormatterUtil.joinProperties(formattedProperties)
        )

        val actionsPathPrefix = dialogData.route + File.separator +
            normalizedEntityName.lowercase(Locale.getDefault()) + File.separator
        val contextData = EntityCreatorContextData(
            project,
            moduleContext.moduleName,
            ACTION_NAME,
            false,
            createWebApi,
            actionsPathPrefix + "index",
            actionsPathPrefix + "edit",
            actionsPathPrefix + "new",
            actionsPathPrefix + "delete",
            DataModelFile(moduleContext.moduleName, normalizedEntityName + "Data").namespaceBuilder,
            DataModelInterfaceFile(moduleContext.moduleName, normalizedEntityName + "Interface").namespaceBuilder,
            NamespaceBuilder(
                moduleContext.moduleName,
                EditActionFile.CLASS_NAME,
                ControllerBackendPhp.DEFAULT_DIR + File.separator + normalizedEntityName
            ),
            NewActionFile(moduleContext.moduleName, normalizedEntityName).namespaceBuilder,
            entityProperties,
            buttons,
            fieldsets,
            fields
        )

        val expectedPhpFiles = buildExpectedPhpFiles(
            moduleName = moduleContext.moduleName,
            entityName = normalizedEntityName,
            createAdminUiComponents = createAdminUiComponents,
            createDataInterface = createDataInterface,
            createWebApi = createWebApi
        )
        val duplicatePhpFile = expectedPhpFiles.firstOrNull { file ->
            GetPhpClassByFQN.getInstance(project).execute(file.classFqn) != null
        }
        if (duplicatePhpFile != null) {
            throw EntityCrudValidationException("PHP class \"${duplicatePhpFile.classFqn}\" already exists.")
        }

        return EntityCrudRequest(
            moduleContext = moduleContext,
            entityName = normalizedEntityName,
            tableName = normalizedTableName,
            idFieldName = normalizedIdFieldName,
            dialogData = dialogData,
            contextData = contextData,
            expectedPhpFiles = expectedPhpFiles,
            expectedRelativePaths = buildExpectedRelativePaths(
                contextData = contextData,
                dialogData = dialogData,
                expectedPhpFiles = expectedPhpFiles
            )
        )
    }

    private fun parsePropertyDefinition(propertyDefinition: String, index: Int): PropertySpec {
        val normalized = propertyDefinition.trim()
        if (normalized.isEmpty()) {
            throw EntityCrudValidationException("properties[$index] must not be empty.")
        }
        val match = PROPERTY_DEFINITION_PATTERN.matchEntire(normalized)
            ?: throw EntityCrudValidationException(
                "properties[$index] must use the format field_name:type with type one of int, float, string, bool."
            )

        return PropertySpec(
            name = match.groupValues[1],
            type = match.groupValues[2]
        )
    }

    private fun buildFields(
        idFieldName: String,
        propertySpecs: List<PropertySpec>
    ): List<UiComponentFormFieldData> {
        val result = mutableListOf(
            UiComponentFormFieldData(
                idFieldName,
                "Entity ID",
                "0",
                "general",
                FormElementType.HIDDEN.type,
                "text",
                idFieldName
            )
        )

        propertySpecs.forEachIndexed { index, spec ->
            result += UiComponentFormFieldData(
                spec.name,
                buildLabelFromSnakeCase(spec.name),
                "${index}0",
                "general",
                FormElementType.getDefaultForProperty(PropertiesTypes.getByValue(spec.type)).type,
                spec.type,
                spec.name
            )
        }

        return result
    }

    private fun buildButtons(moduleName: String, entityName: String, formName: String): List<UiComponentFormButtonData> {
        val directory = "Block/Form/$entityName"
        val saveNamespace = NamespaceBuilder(moduleName, "Save", directory)
        val backNamespace = NamespaceBuilder(moduleName, "Back", directory)
        val deleteNamespace = NamespaceBuilder(moduleName, "Delete", directory)

        return listOf(
            UiComponentFormButtonData(
                directory,
                "Save",
                moduleName,
                "Save",
                saveNamespace.namespace,
                "Save Entity",
                "10",
                formName,
                saveNamespace.classFqn
            ),
            UiComponentFormButtonData(
                directory,
                "Back",
                moduleName,
                "Back",
                backNamespace.namespace,
                "Back To Grid",
                "20",
                formName,
                backNamespace.classFqn
            ),
            UiComponentFormButtonData(
                directory,
                "Delete",
                moduleName,
                "Delete",
                deleteNamespace.namespace,
                "Delete Entity",
                "30",
                formName,
                deleteNamespace.classFqn
            )
        )
    }

    private fun buildExpectedPhpFiles(
        moduleName: String,
        entityName: String,
        createAdminUiComponents: Boolean,
        createDataInterface: Boolean,
        createWebApi: Boolean
    ): List<AbstractPhpFile> {
        val files = linkedSetOf<AbstractPhpFile>(
            ModelFile(moduleName, entityName + "Model"),
            ResourceModelFile(moduleName, entityName + "Resource"),
            CollectionModelFile(moduleName, entityName + "Collection", entityName + "Model"),
            DataModelFile(moduleName, entityName + "Data"),
            EntityDataMapperFile(moduleName, entityName),
            GetListQueryFile(moduleName, entityName),
            SaveEntityCommandFile(moduleName, entityName),
            DeleteEntityByIdCommandFile(moduleName, entityName)
        )

        if (createDataInterface) {
            files += DataModelInterfaceFile(moduleName, entityName + "Interface")
        }

        if (createWebApi) {
            files += SearchResultsInterfaceFile(moduleName, entityName)
            files += SearchResultsFile(moduleName, entityName)
            files += WebApiInterfaceFile(moduleName, GetListQueryFile(moduleName, entityName, true).webApiInterfaceName)
            files += WebApiInterfaceFile(moduleName, SaveEntityCommandFile(moduleName, entityName, true).webApiInterfaceName)
            files += WebApiInterfaceFile(moduleName, DeleteEntityByIdCommandFile(moduleName, entityName, true).webApiInterfaceName)
        }

        if (createAdminUiComponents) {
            files += IndexActionFile(moduleName, entityName)
            files += EditActionFile(moduleName, entityName)
            files += SaveActionFile(moduleName, entityName)
            files += DeleteActionFile(moduleName, entityName)
            files += NewActionFile(moduleName, entityName)
            files += FormGenericButtonBlockFile(moduleName, entityName)
            files += FormButtonBlockFile(moduleName, "Save", "Block/Form/$entityName")
            files += FormButtonBlockFile(moduleName, "Back", "Block/Form/$entityName")
            files += FormButtonBlockFile(moduleName, "Delete", "Block/Form/$entityName")
            files += GridActionColumnFile(moduleName, entityName)
            files += UiComponentDataProviderFile(
                moduleName,
                entityName + "DataProvider",
                UiComponentDataProviderFile.DIRECTORY
            )
        }

        return files.toList()
    }

    private fun buildExpectedRelativePaths(
        contextData: EntityCreatorContextData,
        dialogData: NewEntityDialogData,
        expectedPhpFiles: List<AbstractPhpFile>
    ): List<String> {
        val paths = linkedSetOf<String>()
        expectedPhpFiles.forEach { paths += MagentoMcpCreateSupport.phpFileRelativePath(it) }

        paths += MagentoMcpCreateSupport.configFileRelativePath("base", ModuleDbSchemaXml.FILE_NAME)
        paths += MagentoMcpCreateSupport.configFileRelativePath("base", ModuleDbSchemaWhitelistJson.FILE_NAME)
        paths += MagentoMcpCreateSupport.configFileRelativePath("base", ModuleAclXml.FILE_NAME)
        paths += MagentoMcpCreateSupport.configFileRelativePath(ADMINHTML, ModuleMenuXml.fileName)

        if (dialogData.hasDtoInterface()) {
            paths += MagentoMcpCreateSupport.configFileRelativePath("base", ModuleDiXml.FILE_NAME)
        }

        if (dialogData.hasWebApi()) {
            paths += MagentoMcpCreateSupport.configFileRelativePath("base", ModuleWebApiXmlFile.FILE_NAME)
        }

        if (dialogData.hasAdminUiComponents()) {
            paths += MagentoMcpCreateSupport.configFileRelativePath(ADMINHTML, RoutesXml.FILE_NAME)
            paths += "view/$ADMINHTML/${Package.moduleViewUiComponentDir}/${UiComponentGridXmlFile(dialogData.gridName).fileName}"
            paths += "view/$ADMINHTML/${Package.moduleViewUiComponentDir}/${UiComponentFormXmlFile(dialogData.formName).fileName}"
            paths += "view/$ADMINHTML/${LayoutXml.PARENT_DIR}/${LayoutXml(dialogData.route, dialogData.entityName, IndexActionFile.CLASS_NAME).fileName}"
            paths += "view/$ADMINHTML/${LayoutXml.PARENT_DIR}/${LayoutXml(dialogData.route, dialogData.entityName, EditActionFile.CLASS_NAME).fileName}"
            paths += "view/$ADMINHTML/${LayoutXml.PARENT_DIR}/${NewEntityLayoutFile(contextData.newViewAction.replace(File.separator, "_")).fileName}"
        }

        return paths.toList()
    }

    @Suppress("LongMethod")
    private fun generateCrudScaffold(project: Project, request: EntityCrudRequest) {
        val dialogData = request.dialogData
        val contextData = request.contextData

        generateRequired("model") {
            ModuleModelGenerator(ModelDtoConverter(contextData, dialogData), project).generate(ACTION_NAME, false)
        }
        generateRequired("resource model") {
            ModuleResourceModelGenerator(ResourceModelDtoConverter(contextData, dialogData), project)
                .generate(ACTION_NAME, false)
        }
        generateRequired("collection model") {
            ModuleCollectionGenerator(CollectionModelDtoConverter(contextData, dialogData), project)
                .generate(ACTION_NAME, false)
        }
        generateRequired("data model") {
            DataModelGenerator(project, DataModelDtoConverter(contextData, dialogData)).generate(ACTION_NAME, false)
        }

        if (dialogData.hasDtoInterface()) {
            generateRequired("data model interface") {
                DataModelInterfaceGenerator(DataModelInterfaceDtoConverter(contextData, dialogData), project)
                    .generate(ACTION_NAME, false)
            }
            generateRequired("preference di.xml") {
                PreferenceDiXmlGenerator(PreferenceDiXmlFileDtoConverter(contextData, dialogData), project)
                    .generate(ACTION_NAME, false)
            }
        }

        if (dialogData.hasAdminUiComponents()) {
            generateRequired("adminhtml routes.xml") {
                RoutesXmlGenerator(RoutesXmlDtoConverter(contextData, dialogData), project).generate(ACTION_NAME, false)
            }
        }

        generateRequired("acl.xml") {
            AclXmlGenerator(AclXmlDtoConverter(contextData, dialogData), contextData.moduleName, project)
                .generate(ACTION_NAME, false)
        }
        generateRequired("menu.xml") {
            MenuXmlGenerator(MenuXmlDtoConverter(contextData, dialogData), project).generate(ACTION_NAME, false)
        }

        if (dialogData.hasAdminUiComponents()) {
            generateRequired("index controller") {
                IndexActionGenerator(IndexActionDtoConverter(contextData, dialogData), project).generate(ACTION_NAME, false)
            }
            generateRequired("grid layout xml") {
                LayoutXmlGenerator(GridLayoutXmlDtoConverter(contextData, dialogData), project).generate(ACTION_NAME, false)
            }
        }

        generateRequired("entity data mapper") {
            EntityDataMapperGenerator(EntityDataMapperDtoConverter(contextData, dialogData), project)
                .generate(ACTION_NAME, false)
        }

        val getListQueryFile = generateRequired("get list query") {
            GetListQueryModelGenerator(GetListQueryDtoConverter(contextData, dialogData), project)
                .generate(ACTION_NAME, false)
        }
        if (dialogData.hasWebApi()) {
            generateWebApiInterfaceAndDeclaration(
                project = project,
                moduleName = contextData.moduleName,
                serviceFile = getListQueryFile,
                interfaceName = GetListQueryFile(contextData.moduleName, request.entityName, true).webApiInterfaceName,
                url = GetListQueryFile(contextData.moduleName, request.entityName, true).webApiUrl,
                httpMethod = HttpMethod.GET.name,
                serviceMethod = GetListQueryFile.WEB_API_METHOD_NAME,
                aclResource = dialogData.aclId
            )
        }

        if (dialogData.hasAdminUiComponents()) {
            generateRequired("data provider") {
                UiComponentDataProviderGenerator(
                    DataProviderDtoConverter(contextData, dialogData),
                    contextData.moduleName,
                    project
                ).generate(ACTION_NAME, false)
            }
            generateRequired("grid action column") {
                GridActionColumnFileGenerator(GridActionColumnDtoConverter(contextData, dialogData), project)
                    .generate(ACTION_NAME, false)
            }
            generateRequired("ui component grid") {
                UiComponentGridXmlGenerator(UiComponentGridDtoConverter(contextData, dialogData), project)
                    .generate(ACTION_NAME, false)
            }
            generateRequired("form layout xml") {
                LayoutXmlGenerator(FormLayoutDtoConverter(contextData, dialogData), project).generate(ACTION_NAME, false)
            }
            generateRequired("new action layout xml") {
                NewEntityLayoutGenerator(NewEntityLayoutDtoConverter(contextData, dialogData), project)
                    .generate(ACTION_NAME, false)
            }
        }

        val saveCommandFile = generateRequired("save command") {
            SaveEntityCommandGenerator(SaveEntityCommandDtoConverter(contextData, dialogData), project)
                .generate(ACTION_NAME, false)
        }
        if (dialogData.hasWebApi()) {
            generateWebApiInterfaceAndDeclaration(
                project = project,
                moduleName = contextData.moduleName,
                serviceFile = saveCommandFile,
                interfaceName = SaveEntityCommandFile(contextData.moduleName, request.entityName, true).webApiInterfaceName,
                url = SaveEntityCommandFile(contextData.moduleName, request.entityName, true).webApiUrl,
                httpMethod = HttpMethod.POST.name,
                serviceMethod = SaveEntityCommandFile.WEB_API_METHOD_NAME,
                aclResource = dialogData.aclId
            )
        }

        val deleteCommandFile = generateRequired("delete by id command") {
            DeleteEntityByIdCommandGenerator(DeleteEntityByIdCommandDtoConverter(contextData, dialogData), project)
                .generate(ACTION_NAME, false)
        }
        if (dialogData.hasWebApi()) {
            generateWebApiInterfaceAndDeclaration(
                project = project,
                moduleName = contextData.moduleName,
                serviceFile = deleteCommandFile,
                interfaceName = DeleteEntityByIdCommandFile(contextData.moduleName, request.entityName, true).webApiInterfaceName,
                url = DeleteEntityByIdCommandFile(contextData.moduleName, request.entityName, true).webApiUrl,
                httpMethod = HttpMethod.DELETE.name,
                serviceMethod = DeleteEntityByIdCommandFile.WEB_API_METHOD_NAME,
                aclResource = dialogData.aclId
            )
        }

        if (dialogData.hasAdminUiComponents()) {
            generateRequired("save controller") {
                SaveEntityControllerFileGenerator(FormSaveControllerDtoConverter(contextData, dialogData), project)
                    .generate(ACTION_NAME, false)
            }
            generateRequired("delete controller") {
                DeleteEntityControllerFileGenerator(FormDeleteControllerDtoConverter(contextData, dialogData), project)
                    .generate(ACTION_NAME, false)
            }
            generateRequired("edit controller") {
                EditEntityActionGenerator(FormEditControllerDtoConverter(contextData, dialogData), project)
                    .generate(ACTION_NAME, false)
            }
            generateRequired("generic button block") {
                FormGenericButtonBlockGenerator(FormGenericButtonBlockDtoConverter(contextData, dialogData), project)
                    .generate(ACTION_NAME, false)
            }
            generateRequired("new controller") {
                NewActionEntityControllerFileGenerator(NewControllerDtoConverter(contextData, dialogData), project)
                    .generate(ACTION_NAME, false)
            }
            generateRequired("ui component form") {
                UiComponentFormGenerator(UiComponentFormLayoutDtoConverter(contextData, dialogData), project)
                    .generate(ACTION_NAME, false)
            }
        }

        val dbSchemaData = DbSchemaXmlDtoConverter(contextData, dialogData)
        generateRequired("db_schema.xml") {
            DbSchemaXmlGenerator(dbSchemaData, project, contextData.moduleName).generate(ACTION_NAME, false)
        }
        generateRequired("db_schema_whitelist.json") {
            DbSchemaWhitelistJsonGenerator(project, dbSchemaData, contextData.moduleName).generate(ACTION_NAME, false)
        }

        if (dialogData.hasWebApi()) {
            generateRequired("search results interface") {
                SearchResultsInterfaceGenerator(SearchResultsDtoConverter(contextData, dialogData), project)
                    .generate(ACTION_NAME, false)
            }
            generateRequired("search results class") {
                SearchResultsGenerator(SearchResultsDtoConverter(contextData, dialogData), project)
                    .generate(ACTION_NAME, false)
            }
        }
    }

    private fun generateWebApiInterfaceAndDeclaration(
        project: Project,
        moduleName: String,
        serviceFile: PsiFile,
        interfaceName: String,
        url: String,
        httpMethod: String,
        serviceMethod: String,
        aclResource: String
    ) {
        val phpFile = serviceFile as? PhpFile
            ?: throw EntityCrudGenerationException("Web API service source file is not a PHP file.")
        val serviceClass = GetFirstClassOfFile.getInstance().execute(phpFile)
            ?: throw EntityCrudGenerationException("Web API service class could not be resolved.")
        val methods = PhpTypeMetadataParserUtil.getMethodsByNames(serviceClass, serviceMethod)
        if (methods.isEmpty()) {
            throw EntityCrudGenerationException("Web API service method \"$serviceMethod\" was not found in ${serviceClass.fqn.removePrefix("\\")}.")
        }

        WebApiInterfaceWithDeclarationGenerator(
            com.magento.idea.magento2plugin.actions.generation.data.php.WebApiInterfaceData(
                moduleName,
                PhpTypeMetadataParserUtil.getFqn(serviceClass),
                interfaceName,
                PhpTypeMetadataParserUtil.getShortDescription(serviceClass),
                methods
            ),
            com.magento.idea.magento2plugin.actions.generation.data.xml.WebApiXmlRouteData(
                moduleName,
                url,
                httpMethod,
                serviceClass.fqn.removePrefix("\\"),
                serviceMethod,
                aclResource
            ),
            project
        ).generate(ACTION_NAME, false)
    }

    private fun generateRequired(stepName: String, block: () -> PsiFile?): PsiFile {
        return block() ?: throw EntityCrudGenerationException(
            "Magento entity CRUD scaffold generation failed while creating $stepName."
        )
    }

    private fun buildSuccessMessage(project: Project, request: EntityCrudRequest): String {
        val existingPaths = request.expectedRelativePaths.filter { relativePath ->
            request.moduleContext.moduleDirectory.virtualFile.findFileByRelativePath(relativePath) != null
        }

        val lines = mutableListOf(
            "Created Magento entity CRUD scaffold \"${request.entityName}\".",
            "module: ${request.moduleName}",
            "table: ${request.tableName}",
            "idField: ${request.idFieldName}",
            "adminUiComponents: ${request.dialogData.hasAdminUiComponents()}",
            "dataInterface: ${request.dialogData.hasDtoInterface()}",
            "webApi: ${request.dialogData.hasWebApi()}",
            "files:"
        )
        existingPaths.forEach { relativePath ->
            val virtualFile = request.moduleContext.moduleDirectory.virtualFile.findFileByRelativePath(relativePath)
            if (virtualFile != null) {
                lines += MagentoMcpSupport.relativePath(project, virtualFile)
            }
        }
        return lines.joinToString("\n")
    }

    private fun buildLabelFromSnakeCase(value: String): String {
        return value.split('_').joinToString(" ") { part ->
            part.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        }
    }

    private class EntityCrudValidationException(message: String) : RuntimeException(message)
    private class EntityCrudGenerationException(message: String) : RuntimeException(message)

    private data class PropertySpec(
        val name: String,
        val type: String
    )

    private data class EntityCrudRequest(
        val moduleContext: MagentoMcpCreateSupport.EditableModuleContext,
        val entityName: String,
        val tableName: String,
        val idFieldName: String,
        val dialogData: NewEntityDialogData,
        val contextData: EntityCreatorContextData,
        val expectedPhpFiles: List<AbstractPhpFile>,
        val expectedRelativePaths: List<String>
    ) {
        val moduleName: String
            get() = moduleContext.moduleName
    }
}
