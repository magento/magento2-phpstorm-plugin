/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.magento.idea.magento2plugin.actions.generation.data.DataProviderDeclarationData;
import com.magento.idea.magento2plugin.actions.generation.generator.util.CommitXmlFileUtil;
import com.magento.idea.magento2plugin.actions.generation.generator.util.FindOrCreateDiXml;
import com.magento.idea.magento2plugin.magento.files.ModuleDiXml;
import com.magento.idea.magento2plugin.magento.packages.Areas;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DataProviderDeclarationGenerator extends FileGenerator {
    private final DataProviderDeclarationData dataProviderDeclarationData;
    private final FindOrCreateDiXml findOrCreateDiXml;

    private final List<XmlTag> addSubTagsQueue;
    private final Map<XmlTag, XmlTag> childParentRelationMap;

    /**
     * Constructor.
     *
     * @param dataProviderDeclarationData AclXmlData
     * @param project Project
     */
    public DataProviderDeclarationGenerator(
            final @NotNull DataProviderDeclarationData dataProviderDeclarationData,
            final Project project
    ) {
        super(project);
        this.dataProviderDeclarationData = dataProviderDeclarationData;
        findOrCreateDiXml = new FindOrCreateDiXml(project);

        addSubTagsQueue = new LinkedList<>();
        childParentRelationMap = new HashMap<>();
    }

    @Override
    public PsiFile generate(final String actionName) {
        final XmlFile diXml = (XmlFile) findOrCreateDiXml.execute(
                actionName,
                dataProviderDeclarationData.getModuleName(),
                Areas.base.toString()
        );
        if (diXml == null) {
            return null;
        }

        final XmlTag rootTag = diXml.getRootTag();
        if (rootTag == null) {
            return null;
        }

        createTypeTagForCollectionFactory(rootTag);
        createVirtualTypeTag(rootTag);

        CommitXmlFileUtil.execute(diXml, addSubTagsQueue, childParentRelationMap);
        return diXml;
    }

    protected void createVirtualTypeTag(final XmlTag rootTag) {
        final XmlTag virtualTypeTag = rootTag.createChildTag(
                ModuleDiXml.VIRTUAL_TYPE_TAG,
                null,
                "",
                false
        );
        virtualTypeTag.setAttribute(
                ModuleDiXml.NAME_ATTR,
                dataProviderDeclarationData.getVirtualTypeName()
        );
        virtualTypeTag.setAttribute(ModuleDiXml.TYPE_ATTR, ModuleDiXml.DATA_PROVIDER_SEARCH_RESULT);
        addSubTagsQueue.add(virtualTypeTag);
        childParentRelationMap.put(virtualTypeTag, rootTag);

        final XmlTag arguments = virtualTypeTag.createChildTag(
                ModuleDiXml.ARGUMENTS_TAG,
                null,
                "",
                false
        );
        addSubTagsQueue.add(arguments);
        childParentRelationMap.put(arguments, virtualTypeTag);

        final XmlTag argumentMainTable = virtualTypeTag.createChildTag(
                ModuleDiXml.ARGUMENT_TAG,
                null,
                dataProviderDeclarationData.getTableName(),
                false
        );
        argumentMainTable.setAttribute(ModuleDiXml.NAME_ATTR, ModuleDiXml.MAIN_TABLE_ATTR);
        argumentMainTable.setAttribute(ModuleDiXml.XSI_TYPE_ATTR, ModuleDiXml.XSI_TYPE_STRING);
        addSubTagsQueue.add(argumentMainTable);
        childParentRelationMap.put(argumentMainTable, arguments);

        final XmlTag argumentResourceModel = virtualTypeTag.createChildTag(
                ModuleDiXml.ARGUMENT_TAG,
                null,
                dataProviderDeclarationData.getCollectionFqn(),
                false
        );
        argumentResourceModel.setAttribute(ModuleDiXml.NAME_ATTR, ModuleDiXml.RESOURCE_MODEL_ATTR);
        argumentResourceModel.setAttribute(ModuleDiXml.XSI_TYPE_ATTR, ModuleDiXml.XSI_TYPE_STRING);
        addSubTagsQueue.add(argumentResourceModel);
        childParentRelationMap.put(argumentResourceModel, arguments);
    }

    protected void createTypeTagForCollectionFactory(final XmlTag rootTag) { //NOPMD
        final XmlTag[] typeTags = rootTag.findSubTags(ModuleDiXml.TYPE_TAG);
        XmlTag collectionTypeTag = null;
        for (final XmlTag typeTag: typeTags) {
            @Nullable final XmlAttribute nameAttribute =
                    typeTag.getAttribute(ModuleDiXml.NAME_ATTR);
            if (nameAttribute == null) {
                continue;
            }
            @Nullable final String collectionName = nameAttribute.getValue();
            if (collectionName.equals(ModuleDiXml.DATA_PROVIDER_COLLECTION_FACTORY)) {
                collectionTypeTag = typeTag;
            }
        }

        if (collectionTypeTag == null) {
            collectionTypeTag = rootTag.createChildTag(ModuleDiXml.TYPE_TAG, null, "", false);
            collectionTypeTag.setAttribute(
                    ModuleDiXml.NAME_TAG,
                    ModuleDiXml.DATA_PROVIDER_COLLECTION_FACTORY
            );
            addSubTagsQueue.add(collectionTypeTag);
            childParentRelationMap.put(collectionTypeTag, rootTag);
        }

        XmlTag argumentsTag = collectionTypeTag.findFirstSubTag(ModuleDiXml.ARGUMENTS_TAG);

        if (argumentsTag == null) {
            argumentsTag = collectionTypeTag.createChildTag(
                    ModuleDiXml.ARGUMENTS_TAG,
                    null,
                    "",
                    false
            );
            addSubTagsQueue.add(argumentsTag);
            childParentRelationMap.put(argumentsTag, collectionTypeTag);
        }

        final XmlTag[] argumentTags = argumentsTag.findSubTags(ModuleDiXml.ARGUMENT_TAG);
        XmlTag collectionsArgumentTag = null;
        for (final XmlTag argumentTag: argumentTags) {
            @Nullable final XmlAttribute nameAttribute =
                    argumentTag.getAttribute(ModuleDiXml.NAME_ATTR);
            if (nameAttribute == null) {
                continue;
            }
            @Nullable final String collectionName = nameAttribute.getValue();
            if (collectionName.equals(ModuleDiXml.COLLECTIONS_ATTR_VALUE)) {
                collectionsArgumentTag = argumentTag;
            }
        }

        if (collectionsArgumentTag == null) {
            collectionsArgumentTag = argumentsTag.createChildTag(
                    ModuleDiXml.ARGUMENT_TAG,
                    null,
                    "",
                    false
            );
            collectionsArgumentTag.setAttribute(
                    ModuleDiXml.XSI_TYPE_ATTR,
                    ModuleDiXml.XSI_TYPE_ARRAY
            );
            addSubTagsQueue.add(collectionsArgumentTag);
            childParentRelationMap.put(collectionsArgumentTag, argumentsTag);
        }

        final XmlTag[] itemTags = collectionsArgumentTag.findSubTags(ModuleDiXml.ITEM_TAG);
        XmlTag sourceItemTag = null;
        for (final XmlTag itemTag: itemTags) {
            @Nullable final XmlAttribute nameAttribute =
                    itemTag.getAttribute(ModuleDiXml.NAME_ATTR);
            if (nameAttribute == null) {
                continue;
            }
            @Nullable final String collectionName = nameAttribute.getValue();
            if (collectionName.equals(dataProviderDeclarationData.getDataSource())) {
                sourceItemTag = itemTag;
            }
        }

        if (sourceItemTag == null) {
            sourceItemTag = collectionsArgumentTag.createChildTag(
                    ModuleDiXml.ITEM_TAG,
                    null,
                    dataProviderDeclarationData.getVirtualTypeName(),
                    false
            );
            sourceItemTag.setAttribute(ModuleDiXml.XSI_TYPE_ATTR, ModuleDiXml.XSI_TYPE_STRING);
            sourceItemTag.setAttribute(
                    ModuleDiXml.NAME_ATTR,
                    dataProviderDeclarationData.getDataSource()
            );
            addSubTagsQueue.add(sourceItemTag);
            childParentRelationMap.put(sourceItemTag, collectionsArgumentTag);
        }
    }

    @Override
    protected void fillAttributes(Properties attributes) {} //NOPMD
}
