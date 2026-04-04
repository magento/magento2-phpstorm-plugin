/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.mcp

import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.xml.XmlFile
import com.intellij.psi.xml.XmlTag
import com.jetbrains.php.lang.psi.elements.PhpClass
import com.magento.idea.magento2plugin.actions.generation.data.CategoryEntityData
import com.magento.idea.magento2plugin.actions.generation.data.CategoryFormXmlData
import com.magento.idea.magento2plugin.actions.generation.data.CustomerEntityData
import com.magento.idea.magento2plugin.actions.generation.data.EavEntityDataInterface
import com.magento.idea.magento2plugin.actions.generation.data.ProductEntityData
import com.magento.idea.magento2plugin.actions.generation.data.SourceModelData
import com.magento.idea.magento2plugin.actions.generation.dialog.util.SplitEavAttributeCodeUtil
import com.magento.idea.magento2plugin.actions.generation.generator.CategoryFormXmlGenerator
import com.magento.idea.magento2plugin.actions.generation.generator.CustomerEavAttributePatchGenerator
import com.magento.idea.magento2plugin.actions.generation.generator.EavAttributeSetupPatchGenerator
import com.magento.idea.magento2plugin.actions.generation.generator.SourceModelGenerator
import com.magento.idea.magento2plugin.magento.files.AbstractPhpFile
import com.magento.idea.magento2plugin.magento.files.CategoryFormXmlFile
import com.magento.idea.magento2plugin.magento.files.CustomerEavAttributeDataPatchFile
import com.magento.idea.magento2plugin.magento.files.EavAttributeDataPatchFile
import com.magento.idea.magento2plugin.magento.packages.Areas
import com.magento.idea.magento2plugin.magento.packages.eav.AttributeInput
import com.magento.idea.magento2plugin.magento.packages.eav.AttributeScope
import com.magento.idea.magento2plugin.magento.packages.eav.AttributeType
import com.magento.idea.magento2plugin.util.GetPhpClassByFQN
import com.magento.idea.magento2plugin.util.RegExUtil
import com.magento.idea.magento2plugin.util.magento.FileBasedIndexUtil

internal object MagentoEavAttributeCommands {
    private const val PRODUCT_ACTION_NAME = "Magento MCP Create Product EAV Attribute"
    private const val CATEGORY_ACTION_NAME = "Magento MCP Create Category EAV Attribute"
    private const val CUSTOMER_ACTION_NAME = "Magento MCP Create Customer EAV Attribute"

    private val ATTRIBUTE_CODE_PATTERN = Regex(RegExUtil.LOWER_SNAKE_CASE)
    private val PHP_CLASS_PATTERN = Regex(RegExUtil.Magento.PHP_CLASS)
    private val DIRECTORY_PATTERN = Regex(RegExUtil.DIRECTORY)
    private val COMMA_SEPARATED_PATTERN = Regex(RegExUtil.Magento.COMMA_SEPARATED_STRING)

    fun createMagentoProductEavAttribute(
        project: Project,
        moduleName: String,
        attributeCode: String,
        attributeLabel: String,
        backendType: String,
        frontendInput: String,
        dataPatchName: String,
        attributeGroup: String,
        sortOrder: Int,
        scope: String,
        sourceModelClassFqn: String,
        applyTo: String,
        required: Boolean,
        visible: Boolean,
        usedInGrid: Boolean,
        visibleInGrid: Boolean,
        filterableInGrid: Boolean,
        htmlAllowedOnFront: Boolean,
        visibleOnFront: Boolean,
        options: List<String>
    ): String {
        val request = try {
            MagentoMcpCreateSupport.runReadAction {
                resolveProductRequest(
                    project = project,
                    moduleName = moduleName,
                    attributeCode = attributeCode,
                    attributeLabel = attributeLabel,
                    backendType = backendType,
                    frontendInput = frontendInput,
                    dataPatchName = dataPatchName,
                    attributeGroup = attributeGroup,
                    sortOrder = sortOrder,
                    scope = scope,
                    sourceModelClassFqn = sourceModelClassFqn,
                    applyTo = applyTo,
                    required = required,
                    visible = visible,
                    usedInGrid = usedInGrid,
                    visibleInGrid = visibleInGrid,
                    filterableInGrid = filterableInGrid,
                    htmlAllowedOnFront = htmlAllowedOnFront,
                    visibleOnFront = visibleOnFront,
                    options = options
                )
            }
        } catch (_: ReadAction.CannotReadException) {
            return "The request was cancelled by a pending write action. Retry."
        } catch (exception: EavAttributeValidationException) {
            return exception.message ?: "Product EAV attribute creation request is invalid."
        }

        return createWithRollback(
            project = project,
            request = request,
            actionName = PRODUCT_ACTION_NAME,
            title = "Created Magento product EAV attribute \"${request.attributeCode}\"."
        ) { createdFiles, transaction ->
            createSourceModelIfNeeded(project, request.sourceModelSpec, transaction, PRODUCT_ACTION_NAME)?.let {
                createdFiles += it
            }

            transaction.track(request.dataPatchRelativePath)
            val dataPatchFile = SilentEavAttributeSetupPatchGenerator(request.entityData, project)
                .generate(PRODUCT_ACTION_NAME, false)
                ?: return@createWithRollback buildDataPatchFailureMessage(request.dataPatchClassFqn)
            createdFiles += dataPatchFile

            null
        }
    }

    fun createMagentoCategoryEavAttribute(
        project: Project,
        moduleName: String,
        attributeCode: String,
        attributeLabel: String,
        backendType: String,
        frontendInput: String,
        dataPatchName: String,
        attributeGroup: String,
        sortOrder: Int,
        scope: String,
        sourceModelClassFqn: String,
        required: Boolean,
        visible: Boolean,
        options: List<String>
    ): String {
        val request = try {
            MagentoMcpCreateSupport.runReadAction {
                resolveCategoryRequest(
                    project = project,
                    moduleName = moduleName,
                    attributeCode = attributeCode,
                    attributeLabel = attributeLabel,
                    backendType = backendType,
                    frontendInput = frontendInput,
                    dataPatchName = dataPatchName,
                    attributeGroup = attributeGroup,
                    sortOrder = sortOrder,
                    scope = scope,
                    sourceModelClassFqn = sourceModelClassFqn,
                    required = required,
                    visible = visible,
                    options = options
                )
            }
        } catch (_: ReadAction.CannotReadException) {
            return "The request was cancelled by a pending write action. Retry."
        } catch (exception: EavAttributeValidationException) {
            return exception.message ?: "Category EAV attribute creation request is invalid."
        }

        return createWithRollback(
            project = project,
            request = request,
            actionName = CATEGORY_ACTION_NAME,
            title = "Created Magento category EAV attribute \"${request.attributeCode}\"."
        ) { createdFiles, transaction ->
            createSourceModelIfNeeded(project, request.sourceModelSpec, transaction, CATEGORY_ACTION_NAME)?.let {
                createdFiles += it
            }

            transaction.track(request.dataPatchRelativePath)
            val dataPatchFile = SilentEavAttributeSetupPatchGenerator(request.entityData, project)
                .generate(CATEGORY_ACTION_NAME, false)
                ?: return@createWithRollback buildDataPatchFailureMessage(request.dataPatchClassFqn)
            createdFiles += dataPatchFile

            transaction.track(request.categoryFormRelativePath)
            val categoryFormFile = CategoryFormXmlGenerator(
                CategoryFormXmlData(
                    request.entityData.group,
                    request.entityData.code,
                    request.entityData.input,
                    request.entityData.sortOrder
                ),
                project,
                request.moduleName,
                false
            ).generate(CATEGORY_ACTION_NAME, false)
                ?: return@createWithRollback "Magento category form field \"${request.attributeCode}\" could not be added to category_form.xml. Partial changes were rolled back."
            createdFiles += categoryFormFile

            null
        }
    }

    fun createMagentoCustomerEavAttribute(
        project: Project,
        moduleName: String,
        attributeCode: String,
        attributeLabel: String,
        backendType: String,
        frontendInput: String,
        dataPatchName: String,
        sortOrder: Int,
        sourceModelClassFqn: String,
        required: Boolean,
        visible: Boolean,
        userDefined: Boolean,
        usedInGrid: Boolean,
        visibleInGrid: Boolean,
        filterableInGrid: Boolean,
        system: Boolean,
        useInAdminhtmlCustomerForm: Boolean,
        useInAdminhtmlCheckoutForm: Boolean,
        useInCustomerAccountCreateForm: Boolean,
        useInCustomerAccountEditForm: Boolean,
        options: List<String>
    ): String {
        val request = try {
            MagentoMcpCreateSupport.runReadAction {
                resolveCustomerRequest(
                    project = project,
                    moduleName = moduleName,
                    attributeCode = attributeCode,
                    attributeLabel = attributeLabel,
                    backendType = backendType,
                    frontendInput = frontendInput,
                    dataPatchName = dataPatchName,
                    sortOrder = sortOrder,
                    sourceModelClassFqn = sourceModelClassFqn,
                    required = required,
                    visible = visible,
                    userDefined = userDefined,
                    usedInGrid = usedInGrid,
                    visibleInGrid = visibleInGrid,
                    filterableInGrid = filterableInGrid,
                    system = system,
                    useInAdminhtmlCustomerForm = useInAdminhtmlCustomerForm,
                    useInAdminhtmlCheckoutForm = useInAdminhtmlCheckoutForm,
                    useInCustomerAccountCreateForm = useInCustomerAccountCreateForm,
                    useInCustomerAccountEditForm = useInCustomerAccountEditForm,
                    options = options
                )
            }
        } catch (_: ReadAction.CannotReadException) {
            return "The request was cancelled by a pending write action. Retry."
        } catch (exception: EavAttributeValidationException) {
            return exception.message ?: "Customer EAV attribute creation request is invalid."
        }

        return createWithRollback(
            project = project,
            request = request,
            actionName = CUSTOMER_ACTION_NAME,
            title = "Created Magento customer EAV attribute \"${request.attributeCode}\"."
        ) { createdFiles, transaction ->
            createSourceModelIfNeeded(project, request.sourceModelSpec, transaction, CUSTOMER_ACTION_NAME)?.let {
                createdFiles += it
            }

            transaction.track(request.dataPatchRelativePath)
            val dataPatchFile = SilentCustomerEavAttributePatchGenerator(request.entityData, project)
                .generate(CUSTOMER_ACTION_NAME, false)
                ?: return@createWithRollback buildDataPatchFailureMessage(request.dataPatchClassFqn)
            createdFiles += dataPatchFile

            null
        }
    }

    private fun resolveProductRequest(
        project: Project,
        moduleName: String,
        attributeCode: String,
        attributeLabel: String,
        backendType: String,
        frontendInput: String,
        dataPatchName: String,
        attributeGroup: String,
        sortOrder: Int,
        scope: String,
        sourceModelClassFqn: String,
        applyTo: String,
        required: Boolean,
        visible: Boolean,
        usedInGrid: Boolean,
        visibleInGrid: Boolean,
        filterableInGrid: Boolean,
        htmlAllowedOnFront: Boolean,
        visibleOnFront: Boolean,
        options: List<String>
    ): ProductAttributeRequest {
        val common = resolveCommonRequest(
            project = project,
            moduleName = moduleName,
            attributeCode = attributeCode,
            attributeLabel = attributeLabel,
            backendType = backendType,
            frontendInput = frontendInput,
            dataPatchName = dataPatchName,
            entityType = "Product",
            sourceModelClassFqn = sourceModelClassFqn,
            options = options,
            dataPatchFile = EavAttributeDataPatchFile(
                moduleName.trim(),
                normalizeOrDefaultDataPatchName(dataPatchName, attributeCode, "Product")
            )
        )

        val normalizedGroup = attributeGroup.trim()
        if (normalizedGroup.isEmpty()) {
            throw EavAttributeValidationException("Provide a non-empty attributeGroup value.")
        }
        if (sortOrder < 0) {
            throw EavAttributeValidationException("sortOrder must be zero or greater.")
        }

        val normalizedScope = scope.trim().lowercase()
        val resolvedScope = AttributeScope.values().firstOrNull { it.name.lowercase() == normalizedScope }
            ?: throw EavAttributeValidationException("scope must be one of: global, store, website.")

        val normalizedApplyTo = applyTo.trim()
        if (normalizedApplyTo.isNotEmpty() && !COMMA_SEPARATED_PATTERN.matches(normalizedApplyTo)) {
            throw EavAttributeValidationException(
                "applyTo must be a comma-separated list without spaces inside values."
            )
        }

        val entityData = ProductEntityData().apply {
            setModuleName(common.moduleName)
            setDataPatchName(common.dataPatchName)
            setCode(common.attributeCode)
            setLabel(common.attributeLabel)
            setType(common.backendType)
            setInput(common.frontendInput)
            setSource(common.sourceModelSpec.sourceModelClassFqn)
            setSortOrder(sortOrder)
            setGroup(normalizedGroup)
            setScope(resolvedScope.scope)
            setRequired(required)
            setVisible(visible)
            setUsedInGrid(usedInGrid)
            setVisibleInGrid(visibleInGrid)
            setFilterableInGrid(filterableInGrid)
            setHtmlAllowedOnFront(htmlAllowedOnFront)
            setVisibleOnFront(visibleOnFront)
            setOptions(common.options)
            if (normalizedApplyTo.isNotEmpty()) {
                setApplyTo(normalizedApplyTo)
            }
        }

        return ProductAttributeRequest(
            moduleContext = common.moduleContext,
            attributeCode = common.attributeCode,
            attributeLabel = common.attributeLabel,
            backendType = common.backendType,
            frontendInput = common.frontendInput,
            dataPatchName = common.dataPatchName,
            dataPatchClassFqn = common.dataPatchClassFqn,
            sourceModelSpec = common.sourceModelSpec,
            entityData = entityData,
            dataPatchFile = common.dataPatchFile
        )
    }

    private fun resolveCategoryRequest(
        project: Project,
        moduleName: String,
        attributeCode: String,
        attributeLabel: String,
        backendType: String,
        frontendInput: String,
        dataPatchName: String,
        attributeGroup: String,
        sortOrder: Int,
        scope: String,
        sourceModelClassFqn: String,
        required: Boolean,
        visible: Boolean,
        options: List<String>
    ): CategoryAttributeRequest {
        val common = resolveCommonRequest(
            project = project,
            moduleName = moduleName,
            attributeCode = attributeCode,
            attributeLabel = attributeLabel,
            backendType = backendType,
            frontendInput = frontendInput,
            dataPatchName = dataPatchName,
            entityType = "Category",
            sourceModelClassFqn = sourceModelClassFqn,
            options = options,
            dataPatchFile = EavAttributeDataPatchFile(
                moduleName.trim(),
                normalizeOrDefaultDataPatchName(dataPatchName, attributeCode, "Category")
            )
        )

        val normalizedGroup = attributeGroup.trim()
        if (normalizedGroup.isEmpty()) {
            throw EavAttributeValidationException("Provide a non-empty attributeGroup value.")
        }
        if (sortOrder < 0) {
            throw EavAttributeValidationException("sortOrder must be zero or greater.")
        }

        val normalizedScope = scope.trim().lowercase()
        val resolvedScope = AttributeScope.values().firstOrNull { it.name.lowercase() == normalizedScope }
            ?: throw EavAttributeValidationException("scope must be one of: global, store, website.")

        val existingCategoryForm = FileBasedIndexUtil.findModuleViewFile(
            CategoryFormXmlFile.FILE_NAME,
            Areas.adminhtml,
            common.moduleName,
            project,
            CategoryFormXmlFile.SUB_DIRECTORY
        ) as? XmlFile
        if (existingCategoryForm != null) {
            val duplicateMessage = findDuplicateCategoryField(existingCategoryForm, common.attributeCode)
            if (duplicateMessage != null) {
                throw EavAttributeValidationException(
                    "$duplicateMessage in ${MagentoMcpSupport.relativePath(project, existingCategoryForm.virtualFile)}."
                )
            }
        }

        val entityData = CategoryEntityData().apply {
            setModuleName(common.moduleName)
            setDataPatchName(common.dataPatchName)
            setCode(common.attributeCode)
            setLabel(common.attributeLabel)
            setType(common.backendType)
            setInput(common.frontendInput)
            setSource(common.sourceModelSpec.sourceModelClassFqn)
            setSortOrder(sortOrder)
            setGroup(normalizedGroup)
            setScope(resolvedScope.scope)
            setRequired(required)
            setVisible(visible)
            setOptions(common.options)
        }

        return CategoryAttributeRequest(
            moduleContext = common.moduleContext,
            attributeCode = common.attributeCode,
            attributeLabel = common.attributeLabel,
            backendType = common.backendType,
            frontendInput = common.frontendInput,
            dataPatchName = common.dataPatchName,
            dataPatchClassFqn = common.dataPatchClassFqn,
            sourceModelSpec = common.sourceModelSpec,
            entityData = entityData,
            dataPatchFile = common.dataPatchFile
        )
    }

    private fun resolveCustomerRequest(
        project: Project,
        moduleName: String,
        attributeCode: String,
        attributeLabel: String,
        backendType: String,
        frontendInput: String,
        dataPatchName: String,
        sortOrder: Int,
        sourceModelClassFqn: String,
        required: Boolean,
        visible: Boolean,
        userDefined: Boolean,
        usedInGrid: Boolean,
        visibleInGrid: Boolean,
        filterableInGrid: Boolean,
        system: Boolean,
        useInAdminhtmlCustomerForm: Boolean,
        useInAdminhtmlCheckoutForm: Boolean,
        useInCustomerAccountCreateForm: Boolean,
        useInCustomerAccountEditForm: Boolean,
        options: List<String>
    ): CustomerAttributeRequest {
        val common = resolveCommonRequest(
            project = project,
            moduleName = moduleName,
            attributeCode = attributeCode,
            attributeLabel = attributeLabel,
            backendType = backendType,
            frontendInput = frontendInput,
            dataPatchName = dataPatchName,
            entityType = "Customer",
            sourceModelClassFqn = sourceModelClassFqn,
            options = options,
            dataPatchFile = CustomerEavAttributeDataPatchFile(
                moduleName.trim(),
                normalizeOrDefaultDataPatchName(dataPatchName, attributeCode, "Customer")
            )
        )

        if (sortOrder < 0) {
            throw EavAttributeValidationException("sortOrder must be zero or greater.")
        }

        val entityData = CustomerEntityData().apply {
            setModuleName(common.moduleName)
            setDataPatchName(common.dataPatchName)
            setCode(common.attributeCode)
            setLabel(common.attributeLabel)
            setType(common.backendType)
            setInput(common.frontendInput)
            setSource(common.sourceModelSpec.sourceModelClassFqn)
            setSortOrder(sortOrder)
            setRequired(required)
            setVisible(visible)
            setUserDefined(userDefined)
            setUsedInGrid(usedInGrid)
            setVisibleInGrid(visibleInGrid)
            setFilterableInGrid(filterableInGrid)
            setSystem(system)
            setUseInAdminhtmlCustomerForm(useInAdminhtmlCustomerForm)
            setUseInAdminhtmlCheckoutForm(useInAdminhtmlCheckoutForm)
            setUseInCustomerAccountCreateForm(useInCustomerAccountCreateForm)
            setUseInCustomerAccountEditForm(useInCustomerAccountEditForm)
            setOptions(common.options)
        }

        return CustomerAttributeRequest(
            moduleContext = common.moduleContext,
            attributeCode = common.attributeCode,
            attributeLabel = common.attributeLabel,
            backendType = common.backendType,
            frontendInput = common.frontendInput,
            dataPatchName = common.dataPatchName,
            dataPatchClassFqn = common.dataPatchClassFqn,
            sourceModelSpec = common.sourceModelSpec,
            entityData = entityData,
            dataPatchFile = common.dataPatchFile
        )
    }

    private fun resolveCommonRequest(
        project: Project,
        moduleName: String,
        attributeCode: String,
        attributeLabel: String,
        backendType: String,
        frontendInput: String,
        dataPatchName: String,
        entityType: String,
        sourceModelClassFqn: String,
        options: List<String>,
        dataPatchFile: AbstractPhpFile
    ): CommonAttributeRequest {
        val moduleContext = MagentoMcpCreateSupport.requireEditableModule(project, moduleName) {
            throw EavAttributeValidationException(it)
        }

        val normalizedAttributeCode = attributeCode.trim()
        if (normalizedAttributeCode.isEmpty()) {
            throw EavAttributeValidationException("Provide a non-empty attributeCode value.")
        }
        if (!ATTRIBUTE_CODE_PATTERN.matches(normalizedAttributeCode)) {
            throw EavAttributeValidationException(
                "attributeCode must start with a lowercase letter and contain only lowercase letters, numbers, and underscores."
            )
        }

        val normalizedAttributeLabel = attributeLabel.trim()
        if (normalizedAttributeLabel.isEmpty()) {
            throw EavAttributeValidationException("Provide a non-empty attributeLabel value.")
        }

        val normalizedBackendType = backendType.trim().lowercase()
        if (AttributeType.values().none { it.type == normalizedBackendType }) {
            throw EavAttributeValidationException(
                "backendType must be one of: ${AttributeType.values().joinToString(", ") { it.type }}."
            )
        }

        val normalizedFrontendInput = frontendInput.trim().lowercase()
        if (AttributeInput.values().none { it.input == normalizedFrontendInput }) {
            throw EavAttributeValidationException(
                "frontendInput must be one of: ${AttributeInput.values().joinToString(", ") { it.input }}."
            )
        }

        val normalizedDataPatchName = normalizeOrDefaultDataPatchName(
            dataPatchName = dataPatchName,
            attributeCode = normalizedAttributeCode,
            entityType = entityType
        )
        if (!PHP_CLASS_PATTERN.matches(normalizedDataPatchName)) {
            throw EavAttributeValidationException("dataPatchName must be a valid PHP class name.")
        }

        val normalizedDataPatchFile = when (dataPatchFile) {
            is EavAttributeDataPatchFile -> EavAttributeDataPatchFile(moduleContext.moduleName, normalizedDataPatchName)
            is CustomerEavAttributeDataPatchFile -> CustomerEavAttributeDataPatchFile(moduleContext.moduleName, normalizedDataPatchName)
            else -> dataPatchFile
        }
        val dataPatchClassFqn = normalizedDataPatchFile.classFqn
        if (GetPhpClassByFQN.getInstance(project).execute(dataPatchClassFqn) != null) {
            throw EavAttributeValidationException("Data patch class \"$dataPatchClassFqn\" already exists.")
        }

        val normalizedOptions = normalizeOptions(options)
        if (normalizedOptions != null && normalizedFrontendInput !in setOf(
                AttributeInput.SELECT.input,
                AttributeInput.MULTISELECT.input
            )
        ) {
            throw EavAttributeValidationException("options may only be provided for select or multiselect inputs.")
        }

        val sourceModelSpec = resolveSourceModelSpec(
            project = project,
            moduleContext = moduleContext,
            sourceModelClassFqn = sourceModelClassFqn
        )

        return CommonAttributeRequest(
            moduleContext = moduleContext,
            attributeCode = normalizedAttributeCode,
            attributeLabel = normalizedAttributeLabel,
            backendType = normalizedBackendType,
            frontendInput = normalizedFrontendInput,
            dataPatchName = normalizedDataPatchName,
            dataPatchClassFqn = dataPatchClassFqn,
            sourceModelSpec = sourceModelSpec,
            options = normalizedOptions,
            dataPatchFile = normalizedDataPatchFile
        )
    }

    private fun normalizeOrDefaultDataPatchName(
        dataPatchName: String,
        attributeCode: String,
        entityType: String
    ): String {
        val trimmed = dataPatchName.trim()
        if (trimmed.isNotEmpty()) {
            return trimmed
        }

        val capitalizedCode = SplitEavAttributeCodeUtil.execute(attributeCode.trim())
            .filter { it.isNotEmpty() }
            .joinToString("") { it.replaceFirstChar(Char::uppercaseChar) }

        return "Add${capitalizedCode}${entityType.replaceFirstChar(Char::uppercaseChar)}Attribute"
    }

    private fun normalizeOptions(options: List<String>): LinkedHashMap<Int, String>? {
        val normalizedValues = options.map { it.trim() }.filter { it.isNotEmpty() }
        if (normalizedValues.isEmpty()) {
            return null
        }

        return linkedMapOf<Int, String>().apply {
            normalizedValues.forEachIndexed { index, value ->
                put(index, value)
            }
        }
    }

    private fun resolveSourceModelSpec(
        project: Project,
        moduleContext: MagentoMcpCreateSupport.EditableModuleContext,
        sourceModelClassFqn: String
    ): SourceModelSpec {
        val normalizedSourceModelClassFqn = MagentoMcpSupport.normalizeFqn(sourceModelClassFqn)
        if (normalizedSourceModelClassFqn.isEmpty()) {
            return SourceModelSpec()
        }

        val existingSourceModel = GetPhpClassByFQN.getInstance(project).execute(normalizedSourceModelClassFqn)
        if (existingSourceModel != null) {
            return SourceModelSpec(sourceModelClassFqn = normalizedSourceModelClassFqn)
        }

        val sourceModelSegments = normalizedSourceModelClassFqn.split("\\").filter { it.isNotEmpty() }
        if (sourceModelSegments.size < 2 || sourceModelSegments.any { !PHP_CLASS_PATTERN.matches(it) }) {
            throw EavAttributeValidationException("sourceModelClassFqn must be a valid PHP class FQN.")
        }

        val expectedPrefix = "${moduleContext.moduleNamespace}\\"
        if (!normalizedSourceModelClassFqn.startsWith(expectedPrefix)) {
            return SourceModelSpec(sourceModelClassFqn = normalizedSourceModelClassFqn)
        }

        val relativeSourceModelFqn = normalizedSourceModelClassFqn.removePrefix(expectedPrefix)
        val moduleSourceModelSegments = relativeSourceModelFqn.split("\\").filter { it.isNotEmpty() }
        if (moduleSourceModelSegments.size < 2) {
            throw EavAttributeValidationException(
                "sourceModelClassFqn must include a sub-namespace and class name, for example \"${moduleContext.moduleNamespace}\\Model\\Source\\SampleSource\"."
            )
        }

        val sourceModelClassName = moduleSourceModelSegments.last()
        val sourceModelDirectory = moduleSourceModelSegments.dropLast(1).joinToString("/")
        if (!DIRECTORY_PATTERN.matches(sourceModelDirectory)) {
            throw EavAttributeValidationException("sourceModelClassFqn contains an invalid target directory.")
        }

        return SourceModelSpec(
            sourceModelClassFqn = normalizedSourceModelClassFqn,
            sourceModelData = SourceModelData().apply {
                setModuleName(moduleContext.moduleName)
                setClassName(sourceModelClassName)
                setDirectory(sourceModelDirectory)
            },
            relativePath = MagentoMcpCreateSupport.relativePath(sourceModelDirectory, "$sourceModelClassName.php")
        )
    }

    private fun createSourceModelIfNeeded(
        project: Project,
        sourceModelSpec: SourceModelSpec,
        transaction: MagentoMcpCreateSupport.FileTransaction,
        actionName: String
    ): PsiFile? {
        val sourceModelData = sourceModelSpec.sourceModelData ?: return null
        val relativePath = sourceModelSpec.relativePath
            ?: throw IllegalStateException("Missing source model relative path.")

        transaction.track(relativePath)
        return SilentSourceModelGenerator(sourceModelData, project).generate(actionName, false)
            ?: throw GenerationFailure(buildSourceModelFailureMessage(sourceModelSpec))
    }

    private fun createWithRollback(
        project: Project,
        request: AttributeRequestSummary,
        actionName: String,
        title: String,
        body: (MutableList<PsiFile>, MagentoMcpCreateSupport.FileTransaction) -> String?
    ): String {
        val transaction = MagentoMcpCreateSupport.FileTransaction(project, request.moduleContext.moduleDirectory)
        val createdFiles = mutableListOf<PsiFile>()

        return try {
            val failureMessage = body(createdFiles, transaction)
            if (failureMessage != null) {
                transaction.rollback()
                return failureMessage
            }

            buildSuccessMessage(
                project = project,
                title = title,
                request = request,
                createdFiles = createdFiles
            )
        } catch (failure: GenerationFailure) {
            transaction.rollback()
            failure.message ?: "Magento EAV attribute generation failed. Partial changes were rolled back."
        } catch (exception: Throwable) {
            transaction.rollback()
            "Magento EAV attribute \"${request.attributeCode}\" could not be created. Partial changes were rolled back. Reason: ${exception.message ?: exception::class.java.simpleName}"
        }
    }

    private fun buildSourceModelFailureMessage(sourceModelSpec: SourceModelSpec): String {
        val sourceModelClassFqn = sourceModelSpec.sourceModelClassFqn ?: "source model"
        return "Magento source model \"$sourceModelClassFqn\" could not be created. Partial changes were rolled back."
    }

    private fun buildDataPatchFailureMessage(dataPatchClassFqn: String): String {
        return "Magento EAV data patch \"$dataPatchClassFqn\" could not be created. Partial changes were rolled back."
    }

    private fun buildSuccessMessage(
        project: Project,
        title: String,
        request: AttributeRequestSummary,
        createdFiles: List<PsiFile>
    ): String {
        val lines = mutableListOf(
            title,
            "module: ${request.moduleName}",
            "attributeCode: ${request.attributeCode}",
            "attributeLabel: ${request.attributeLabel}",
            "backendType: ${request.backendType}",
            "frontendInput: ${request.frontendInput}",
            "dataPatch: ${request.dataPatchClassFqn}"
        )
        request.sourceModelSpec.sourceModelClassFqn?.let {
            lines += "sourceModel: $it"
        }
        lines += "files:"
        createdFiles.forEach { file ->
            lines += MagentoMcpSupport.relativePath(project, file.virtualFile)
        }
        return lines.joinToString("\n")
    }

    private fun findDuplicateCategoryField(categoryFormXmlFile: XmlFile, attributeCode: String): String? {
        val rootTag = categoryFormXmlFile.rootTag ?: return null
        for (fieldsetTag in rootTag.findSubTags(CategoryFormXmlFile.XML_TAG_FIELDSET)) {
            if (findDuplicateCategoryFieldInFieldset(fieldsetTag, attributeCode)) {
                val fieldsetName = fieldsetTag.getAttributeValue(CategoryFormXmlFile.XML_ATTR_FIELDSET_NAME)
                return if (fieldsetName.isNullOrBlank()) {
                    "Field \"$attributeCode\" is already declared in category_form.xml"
                } else {
                    "Field \"$attributeCode\" is already declared in category_form.xml fieldset \"$fieldsetName\""
                }
            }
        }

        return null
    }

    private fun findDuplicateCategoryFieldInFieldset(fieldsetTag: XmlTag, attributeCode: String): Boolean {
        return fieldsetTag.findSubTags(CategoryFormXmlFile.XML_TAG_FIELD).any {
            it.getAttributeValue(CategoryFormXmlFile.XML_ATTR_FIELD_NAME) == attributeCode
        }
    }

    private class SilentSourceModelGenerator(data: SourceModelData, project: Project) :
        SourceModelGenerator(data, project, true) {
        override fun onClassAlreadyExists(phpClass: PhpClass) {}
        override fun onFileGenerated(generatedFile: PsiFile?, actionName: String) {}
    }

    private class SilentEavAttributeSetupPatchGenerator(data: EavEntityDataInterface, project: Project) :
        EavAttributeSetupPatchGenerator(data, project, true) {
        override fun onClassAlreadyExists(phpClass: PhpClass) {}
        override fun onFileGenerated(generatedFile: PsiFile?, actionName: String) {}
    }

    private class SilentCustomerEavAttributePatchGenerator(data: CustomerEntityData, project: Project) :
        CustomerEavAttributePatchGenerator(data, project, true) {
        override fun onClassAlreadyExists(phpClass: PhpClass) {}
        override fun onFileGenerated(generatedFile: PsiFile?, actionName: String) {}
    }

    private class EavAttributeValidationException(message: String) : RuntimeException(message)

    private class GenerationFailure(message: String) : RuntimeException(message)

    private open class AttributeRequestSummary(
        val moduleContext: MagentoMcpCreateSupport.EditableModuleContext,
        val attributeCode: String,
        val attributeLabel: String,
        val backendType: String,
        val frontendInput: String,
        val dataPatchName: String,
        val dataPatchClassFqn: String,
        val sourceModelSpec: SourceModelSpec
    ) {
        val moduleName: String
            get() = moduleContext.moduleName
    }

    private data class CommonAttributeRequest(
        val moduleContext: MagentoMcpCreateSupport.EditableModuleContext,
        val attributeCode: String,
        val attributeLabel: String,
        val backendType: String,
        val frontendInput: String,
        val dataPatchName: String,
        val dataPatchClassFqn: String,
        val sourceModelSpec: SourceModelSpec,
        val options: LinkedHashMap<Int, String>?,
        val dataPatchFile: AbstractPhpFile
    ) {
        val moduleName: String
            get() = moduleContext.moduleName
    }

    private class ProductAttributeRequest(
        moduleContext: MagentoMcpCreateSupport.EditableModuleContext,
        attributeCode: String,
        attributeLabel: String,
        backendType: String,
        frontendInput: String,
        dataPatchName: String,
        dataPatchClassFqn: String,
        sourceModelSpec: SourceModelSpec,
        val entityData: ProductEntityData,
        val dataPatchFile: AbstractPhpFile
    ) : AttributeRequestSummary(
        moduleContext = moduleContext,
        attributeCode = attributeCode,
        attributeLabel = attributeLabel,
        backendType = backendType,
        frontendInput = frontendInput,
        dataPatchName = dataPatchName,
        dataPatchClassFqn = dataPatchClassFqn,
        sourceModelSpec = sourceModelSpec
    ) {
        val dataPatchRelativePath: String
            get() = MagentoMcpCreateSupport.phpFileRelativePath(dataPatchFile)
    }

    private class CategoryAttributeRequest(
        moduleContext: MagentoMcpCreateSupport.EditableModuleContext,
        attributeCode: String,
        attributeLabel: String,
        backendType: String,
        frontendInput: String,
        dataPatchName: String,
        dataPatchClassFqn: String,
        sourceModelSpec: SourceModelSpec,
        val entityData: CategoryEntityData,
        val dataPatchFile: AbstractPhpFile
    ) : AttributeRequestSummary(
        moduleContext = moduleContext,
        attributeCode = attributeCode,
        attributeLabel = attributeLabel,
        backendType = backendType,
        frontendInput = frontendInput,
        dataPatchName = dataPatchName,
        dataPatchClassFqn = dataPatchClassFqn,
        sourceModelSpec = sourceModelSpec
    ) {
        val dataPatchRelativePath: String
            get() = MagentoMcpCreateSupport.phpFileRelativePath(dataPatchFile)

        val categoryFormRelativePath: String
            get() = "view/adminhtml/ui_component/${CategoryFormXmlFile.FILE_NAME}"
    }

    private class CustomerAttributeRequest(
        moduleContext: MagentoMcpCreateSupport.EditableModuleContext,
        attributeCode: String,
        attributeLabel: String,
        backendType: String,
        frontendInput: String,
        dataPatchName: String,
        dataPatchClassFqn: String,
        sourceModelSpec: SourceModelSpec,
        val entityData: CustomerEntityData,
        val dataPatchFile: AbstractPhpFile
    ) : AttributeRequestSummary(
        moduleContext = moduleContext,
        attributeCode = attributeCode,
        attributeLabel = attributeLabel,
        backendType = backendType,
        frontendInput = frontendInput,
        dataPatchName = dataPatchName,
        dataPatchClassFqn = dataPatchClassFqn,
        sourceModelSpec = sourceModelSpec
    ) {
        val dataPatchRelativePath: String
            get() = MagentoMcpCreateSupport.phpFileRelativePath(dataPatchFile)
    }

    private data class SourceModelSpec(
        val sourceModelClassFqn: String? = null,
        val sourceModelData: SourceModelData? = null,
        val relativePath: String? = null
    )
}
