/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.mcp

import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.util.Key
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.psi.xml.XmlTag
import com.magento.idea.magento2plugin.indexes.LayoutIndex
import com.magento.idea.magento2plugin.indexes.UIComponentIndex
import com.magento.idea.magento2plugin.magento.files.ModuleAclXml
import com.magento.idea.magento2plugin.magento.files.ModuleDiXml
import com.magento.idea.magento2plugin.magento.files.ModuleMenuXml

internal object MagentoMcpSnapshots {
    private val DI_SNAPSHOT_KEY = Key.create<CachedValue<DiSnapshot>>("magento.mcp.diSnapshot")
    private val EVENT_SNAPSHOT_KEY = Key.create<CachedValue<EventSnapshot>>("magento.mcp.eventSnapshot")
    private val VIEW_SNAPSHOT_KEY = Key.create<CachedValue<ViewSnapshot>>("magento.mcp.viewSnapshot")

    fun diSnapshot(project: Project): DiSnapshot = getCachedValue(project, DI_SNAPSHOT_KEY) {
        buildDiSnapshot(project)
    }

    fun eventSnapshot(project: Project): EventSnapshot = getCachedValue(project, EVENT_SNAPSHOT_KEY) {
        buildEventSnapshot(project)
    }

    fun viewSnapshot(project: Project): ViewSnapshot = getCachedValue(project, VIEW_SNAPSHOT_KEY) {
        buildViewSnapshot(project)
    }

    private fun <T> getCachedValue(
        project: Project,
        key: Key<CachedValue<T>>,
        builder: () -> T
    ): T = CachedValuesManager.getManager(project).getCachedValue(project, key, {
        CachedValueProvider.Result.create(
            builder(),
            PsiModificationTracker.MODIFICATION_COUNT,
            ProjectRootManager.getInstance(project)
        )
    }, false)

    private fun buildDiSnapshot(project: Project): DiSnapshot {
        val preferencesFor = linkedMapOf<String, MutableList<PreferenceRecord>>()
        val preferencesType = linkedMapOf<String, MutableList<PreferenceRecord>>()
        val typeDeclarations = linkedMapOf<String, MutableList<TypeDeclarationRecord>>()
        val virtualTypeByName = linkedMapOf<String, MutableList<VirtualTypeRecord>>()
        val virtualTypeByTarget = linkedMapOf<String, MutableList<VirtualTypeRecord>>()
        val pluginDeclarations = linkedMapOf<String, MutableList<PluginDeclarationRecord>>()

        for (xmlFile in MagentoMcpSupport.findXmlFilesByName(project, ModuleDiXml.FILE_NAME)) {
            ProgressManager.checkCanceled()

            val rootTag = xmlFile.rootTag ?: continue
            val filePath = MagentoMcpSupport.relativePath(project, xmlFile.virtualFile)
            val scope = MagentoMcpSupport.determineConfigScope(filePath, ModuleDiXml.FILE_NAME)

            for (preferenceTag in rootTag.findSubTags(ModuleDiXml.PREFERENCE_TAG_NAME)) {
                val record = PreferenceRecord(
                    filePath = filePath,
                    preferenceFor = MagentoMcpSupport.presentableFqn(
                        preferenceTag.getAttributeValue(ModuleDiXml.PREFERENCE_ATTR_FOR)
                    ),
                    preferenceType = MagentoMcpSupport.presentableFqn(
                        preferenceTag.getAttributeValue(ModuleDiXml.TYPE_ATTR)
                    )
                )
                record.preferenceFor?.let { preferencesFor.getOrPut(it, ::mutableListOf) += record }
                record.preferenceType?.let { preferencesType.getOrPut(it, ::mutableListOf) += record }
            }

            for (typeTag in rootTag.findSubTags(ModuleDiXml.TYPE_TAG)) {
                val typeName = MagentoMcpSupport.presentableFqn(typeTag.getAttributeValue(ModuleDiXml.NAME_ATTR)) ?: continue
                val argumentNames = typeTag.findFirstSubTag(ModuleDiXml.ARGUMENTS_TAG)
                    ?.findSubTags(ModuleDiXml.ARGUMENT_TAG)
                    ?.mapNotNull { it.getAttributeValue(ModuleDiXml.NAME_ATTR) }
                    .orEmpty()
                typeDeclarations.getOrPut(typeName, ::mutableListOf) += TypeDeclarationRecord(
                    filePath = filePath,
                    typeName = typeName,
                    argumentNames = argumentNames
                )

                for (pluginTag in typeTag.findSubTags(ModuleDiXml.PLUGIN_TAG_NAME)) {
                    pluginDeclarations.getOrPut(typeName, ::mutableListOf) += PluginDeclarationRecord(
                        filePath = filePath,
                        targetFqn = typeName,
                        pluginName = pluginTag.getAttributeValue(ModuleDiXml.NAME_ATTR) ?: "-",
                        pluginType = MagentoMcpSupport.presentableFqn(
                            pluginTag.getAttributeValue(ModuleDiXml.TYPE_ATTR)
                        ),
                        sortOrder = pluginTag.getAttributeValue(ModuleDiXml.SORT_ORDER_ATTR)?.toIntOrNull() ?: 0,
                        disabled = pluginTag.getAttributeValue(ModuleDiXml.DISABLED_ATTR_NAME)
                            .equals("true", ignoreCase = true),
                        scope = scope
                    )
                }
            }

            for (virtualTypeTag in rootTag.findSubTags(ModuleDiXml.VIRTUAL_TYPE_TAG)) {
                val record = VirtualTypeRecord(
                    filePath = filePath,
                    name = MagentoMcpSupport.presentableFqn(
                        virtualTypeTag.getAttributeValue(ModuleDiXml.NAME_ATTR)
                    ),
                    targetType = MagentoMcpSupport.presentableFqn(
                        virtualTypeTag.getAttributeValue(ModuleDiXml.TYPE_ATTR)
                    )
                )
                record.name?.let { virtualTypeByName.getOrPut(it, ::mutableListOf) += record }
                record.targetType?.let { virtualTypeByTarget.getOrPut(it, ::mutableListOf) += record }
            }
        }

        return DiSnapshot(
            preferencesFor = preferencesFor.freezeByFilePath(),
            preferencesType = preferencesType.freezeByFilePath(),
            typeDeclarations = typeDeclarations.freezeByFilePath(),
            virtualTypesByName = virtualTypeByName.freezeByFilePath(),
            virtualTypesByTarget = virtualTypeByTarget.freezeByFilePath(),
            pluginDeclarations = pluginDeclarations.freeze(compareBy<PluginDeclarationRecord>({ it.scope }, { it.sortOrder }, { it.filePath }))
        )
    }

    private fun buildEventSnapshot(project: Project): EventSnapshot {
        val observersByEvent = linkedMapOf<String, MutableList<EventObserverRecord>>()

        for (xmlFile in MagentoMcpSupport.findXmlFilesByName(project, "events.xml")) {
            ProgressManager.checkCanceled()

            val rootTag = xmlFile.rootTag ?: continue
            val filePath = MagentoMcpSupport.relativePath(project, xmlFile.virtualFile)

            for (eventTag in rootTag.findSubTags("event")) {
                val eventName = eventTag.getAttributeValue("name") ?: continue
                for (observerTag in eventTag.findSubTags("observer")) {
                    observersByEvent.getOrPut(eventName, ::mutableListOf) += EventObserverRecord(
                        filePath = filePath,
                        observerName = observerTag.getAttributeValue("name") ?: "-",
                        observerInstance = MagentoMcpSupport.presentableFqn(
                            observerTag.getAttributeValue("instance")
                        ) ?: "-",
                        disabled = observerTag.getAttributeValue("disabled").equals("true", ignoreCase = true)
                    )
                }
            }
        }

        return EventSnapshot(
            observersByEvent = observersByEvent.freeze(compareBy<EventObserverRecord>({ it.filePath }, { it.observerName }))
        )
    }

    private fun buildViewSnapshot(project: Project): ViewSnapshot {
        val handles = mutableListOf<LayoutHandleRecord>()
        val blocks = linkedMapOf<String, MutableList<BlockRecord>>()
        val containers = linkedMapOf<String, MutableList<ContainerRecord>>()
        val uiComponents = mutableListOf<UiComponentRecord>()
        val aclResources = linkedMapOf<String, MutableList<AclResourceRecord>>()
        val menuEntries = linkedMapOf<String, MutableList<MenuEntryRecord>>()

        for (xmlFile in LayoutIndex.getLayoutFiles(project)) {
            ProgressManager.checkCanceled()

            val filePath = MagentoMcpSupport.relativePath(project, xmlFile.virtualFile)
            handles += LayoutHandleRecord(xmlFile.virtualFile.nameWithoutExtension, filePath)
            xmlFile.rootTag?.let { collectLayoutEntities(it, filePath, blocks, containers) }
        }

        for (xmlFile in UIComponentIndex.getUiComponentFiles(project)) {
            ProgressManager.checkCanceled()

            uiComponents += UiComponentRecord(
                name = xmlFile.virtualFile.nameWithoutExtension,
                filePath = MagentoMcpSupport.relativePath(project, xmlFile.virtualFile),
                rootTag = xmlFile.rootTag?.name ?: "-"
            )
        }

        for (xmlFile in MagentoMcpSupport.findXmlFilesByName(project, ModuleAclXml.FILE_NAME)) {
            ProgressManager.checkCanceled()

            val rootTag = xmlFile.rootTag ?: continue
            val resourcesTag = rootTag.findFirstSubTag(ModuleAclXml.XML_TAG_ACL)
                ?.findFirstSubTag(ModuleAclXml.XML_TAG_RESOURCES)
                ?: continue
            collectAclResources(
                parentTag = resourcesTag,
                ancestry = emptyList(),
                filePath = MagentoMcpSupport.relativePath(project, xmlFile.virtualFile),
                records = aclResources
            )
        }

        for (xmlFile in MagentoMcpSupport.findXmlFilesByName(project, ModuleMenuXml.fileName)) {
            ProgressManager.checkCanceled()

            val rootTag = xmlFile.rootTag ?: continue
            val menuTag = rootTag.findFirstSubTag(ModuleMenuXml.menuTag) ?: continue
            val filePath = MagentoMcpSupport.relativePath(project, xmlFile.virtualFile)
            for (addTag in menuTag.findSubTags(ModuleMenuXml.addTag)) {
                val menuId = addTag.getAttributeValue(ModuleMenuXml.idTagAttribute) ?: continue
                menuEntries.getOrPut(menuId, ::mutableListOf) += MenuEntryRecord(
                    filePath = filePath,
                    title = addTag.getAttributeValue(ModuleMenuXml.titleTagAttribute) ?: "-",
                    resource = addTag.getAttributeValue(ModuleMenuXml.resourceTagAttribute) ?: "-",
                    parent = addTag.getAttributeValue(ModuleMenuXml.parentTagAttribute) ?: "-",
                    action = addTag.getAttributeValue(ModuleMenuXml.actionTagAttribute) ?: "-"
                )
            }
        }

        return ViewSnapshot(
            handles = handles.sortedBy(LayoutHandleRecord::name),
            blocks = blocks.freeze(compareBy<BlockRecord>({ it.filePath }, { it.blockClass ?: "" }, { it.template ?: "" })),
            containers = containers.freeze(compareBy<ContainerRecord>({ it.filePath }, { it.htmlTag ?: "" }, { it.htmlClass ?: "" })),
            uiComponents = uiComponents.sortedWith(compareBy<UiComponentRecord>({ it.name }, { it.filePath })),
            aclResources = aclResources.freeze(compareBy<AclResourceRecord>({ it.filePath }, { it.title ?: "" })),
            menuEntries = menuEntries.freeze(compareBy<MenuEntryRecord>({ it.filePath }, { it.title }, { it.parent }))
        )
    }

    private fun collectLayoutEntities(
        parentTag: XmlTag,
        filePath: String,
        blocks: MutableMap<String, MutableList<BlockRecord>>,
        containers: MutableMap<String, MutableList<ContainerRecord>>
    ) {
        for (childTag in parentTag.subTags) {
            ProgressManager.checkCanceled()

            when (childTag.name) {
                "block" -> {
                    val blockName = childTag.getAttributeValue("name")
                    if (!blockName.isNullOrBlank()) {
                        blocks.getOrPut(blockName, ::mutableListOf) += BlockRecord(
                            filePath = filePath,
                            blockClass = childTag.getAttributeValue("class"),
                            template = childTag.getAttributeValue("template")
                        )
                    }
                }

                "container" -> {
                    val containerName = childTag.getAttributeValue("name")
                    if (!containerName.isNullOrBlank()) {
                        containers.getOrPut(containerName, ::mutableListOf) += ContainerRecord(
                            filePath = filePath,
                            htmlTag = childTag.getAttributeValue("htmlTag"),
                            htmlClass = childTag.getAttributeValue("htmlClass")
                        )
                    }
                }
            }

            if (childTag.subTags.isNotEmpty()) {
                collectLayoutEntities(childTag, filePath, blocks, containers)
            }
        }
    }

    private fun collectAclResources(
        parentTag: XmlTag,
        ancestry: List<AclTreeNode>,
        filePath: String,
        records: MutableMap<String, MutableList<AclResourceRecord>>
    ) {
        for (resourceTag in parentTag.findSubTags(ModuleAclXml.XML_TAG_RESOURCE)) {
            ProgressManager.checkCanceled()

            val resourceId = resourceTag.getAttributeValue(ModuleAclXml.XML_ATTR_ID) ?: continue
            val title = resourceTag.getAttributeValue(ModuleAclXml.XML_ATTR_TITLE)
            val tree = ancestry + AclTreeNode(resourceId, title)
            records.getOrPut(resourceId, ::mutableListOf) += AclResourceRecord(
                filePath = filePath,
                title = title,
                tree = tree
            )
            collectAclResources(resourceTag, tree, filePath, records)
        }
    }

    private fun <T> Map<String, MutableList<T>>.freeze(comparator: Comparator<in T>): Map<String, List<T>> {
        return entries.associate { (key, values) -> key to values.sortedWith(comparator) }
    }

    private fun <T : HasFilePath> Map<String, MutableList<T>>.freezeByFilePath(): Map<String, List<T>> {
        return freeze(compareBy(HasFilePath::filePath))
    }
}

internal data class DiSnapshot(
    val preferencesFor: Map<String, List<PreferenceRecord>>,
    val preferencesType: Map<String, List<PreferenceRecord>>,
    val typeDeclarations: Map<String, List<TypeDeclarationRecord>>,
    val virtualTypesByName: Map<String, List<VirtualTypeRecord>>,
    val virtualTypesByTarget: Map<String, List<VirtualTypeRecord>>,
    val pluginDeclarations: Map<String, List<PluginDeclarationRecord>>
)

internal data class EventSnapshot(
    val observersByEvent: Map<String, List<EventObserverRecord>>
)

internal data class ViewSnapshot(
    val handles: List<LayoutHandleRecord>,
    val blocks: Map<String, List<BlockRecord>>,
    val containers: Map<String, List<ContainerRecord>>,
    val uiComponents: List<UiComponentRecord>,
    val aclResources: Map<String, List<AclResourceRecord>>,
    val menuEntries: Map<String, List<MenuEntryRecord>>
)

internal interface HasFilePath {
    val filePath: String
}

internal data class PreferenceRecord(
    override val filePath: String,
    val preferenceFor: String?,
    val preferenceType: String?
) : HasFilePath

internal data class TypeDeclarationRecord(
    override val filePath: String,
    val typeName: String,
    val argumentNames: List<String>
) : HasFilePath

internal data class VirtualTypeRecord(
    override val filePath: String,
    val name: String?,
    val targetType: String?
) : HasFilePath

internal data class PluginDeclarationRecord(
    override val filePath: String,
    val targetFqn: String,
    val pluginName: String,
    val pluginType: String?,
    val sortOrder: Int,
    val disabled: Boolean,
    val scope: String
) : HasFilePath

internal data class EventObserverRecord(
    override val filePath: String,
    val observerName: String,
    val observerInstance: String,
    val disabled: Boolean
) : HasFilePath

internal data class LayoutHandleRecord(
    val name: String,
    override val filePath: String
) : HasFilePath

internal data class BlockRecord(
    override val filePath: String,
    val blockClass: String?,
    val template: String?
) : HasFilePath

internal data class ContainerRecord(
    override val filePath: String,
    val htmlTag: String?,
    val htmlClass: String?
) : HasFilePath

internal data class UiComponentRecord(
    val name: String,
    override val filePath: String,
    val rootTag: String
) : HasFilePath

internal data class AclResourceRecord(
    override val filePath: String,
    val title: String?,
    val tree: List<AclTreeNode>
) : HasFilePath

internal data class AclTreeNode(
    val resourceId: String,
    val resourceTitle: String?
)

internal data class MenuEntryRecord(
    override val filePath: String,
    val title: String,
    val resource: String,
    val parent: String,
    val action: String
) : HasFilePath
