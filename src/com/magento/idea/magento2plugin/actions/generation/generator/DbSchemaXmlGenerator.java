/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.google.common.collect.Lists;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.magento.idea.magento2plugin.actions.generation.data.DbSchemaXmlData;
import com.magento.idea.magento2plugin.actions.generation.generator.util.FindOrCreateDbSchemaXmlUtil;
import com.magento.idea.magento2plugin.magento.files.ModuleDbSchemaXml;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class DbSchemaXmlGenerator extends FileGenerator {
    private final Project project;
    private final String moduleName;
    private final DbSchemaXmlData dbSchemaXmlData;
    private final FindOrCreateDbSchemaXmlUtil findOrCreateDbSchemaXmlUtil;

    private final List<XmlTag> newTagsQueue;
    private final Map<XmlTag, XmlTag> newTagsChildParentRelationMap;

    /**
     * Constructor.
     *
     * @param dbSchemaXmlData DbSchemaXmlData
     * @param project Project
     * @param moduleName String
     */
    public DbSchemaXmlGenerator(
            final @NotNull DbSchemaXmlData dbSchemaXmlData,
            final @NotNull Project project,
            final @NotNull String moduleName
    ) {
        super(project);
        this.project = project;
        this.moduleName = moduleName;
        this.dbSchemaXmlData = dbSchemaXmlData;
        findOrCreateDbSchemaXmlUtil = new FindOrCreateDbSchemaXmlUtil(project);

        newTagsQueue = new LinkedList<>();
        newTagsChildParentRelationMap = new HashMap<>();
    }

    @Override
    public PsiFile generate(final String actionName) {
        final XmlFile dbSchemaXmlFile = (XmlFile) findOrCreateDbSchemaXmlUtil.execute(
                actionName,
                moduleName
        );
        if (dbSchemaXmlFile == null || !validateData(dbSchemaXmlData)) {
            return null;
        }

        final XmlTag rootTag = dbSchemaXmlFile.getRootTag();
        if (rootTag == null) {
            return null;
        }
        XmlTag tableTag = findOrCreateTag(
                ModuleDbSchemaXml.XML_TAG_TABLE,
                ModuleDbSchemaXml.XML_ATTR_TABLE_NAME,
                rootTag,
                dbSchemaXmlData.getTableName(),
                dbSchemaXmlData.getTableAttributesMap()
        );
        return commitDbSchemaXmlFile(dbSchemaXmlFile);
    }

    /**
     * Save db_schema.xml file.
     *
     * @param dbSchemaXmlFile XmlFile
     *
     * @return XmlFile
     */
    private XmlFile commitDbSchemaXmlFile(final XmlFile dbSchemaXmlFile) {
        WriteCommandAction.runWriteCommandAction(project, () -> {
            for (final XmlTag tag : Lists.reverse(newTagsQueue)) {
                if (newTagsChildParentRelationMap.containsKey(tag)) {
                    final XmlTag parent = newTagsChildParentRelationMap.get(tag);
                    parent.addSubTag(tag, false);
                }
            }
        });
        final PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(project);
        final Document document = psiDocumentManager.getDocument(dbSchemaXmlFile);

        if (document != null) {
            psiDocumentManager.commitDocument(document);
        }
        return dbSchemaXmlFile;
    }

    /**
     * Find or create subtag by attribute.
     *
     * @param targetTagName String
     * @param identityAttrName String
     * @param parent XmlTag
     * @param targetIdentityAttrValue String
     * @param attributes Map
     *
     * @return XmlTag
     */
    private XmlTag findOrCreateTag(
            final String targetTagName,
            final String identityAttrName,
            final XmlTag parent,
            final String targetIdentityAttrValue,
            final Map<String, String> attributes
    ) {
        for (final XmlTag childTag : parent.getSubTags()) {
            if (!childTag.getName().equals(targetTagName)) {
                continue;
            }
            final String childIdentityValue
                    = childTag.getAttributeValue(identityAttrName);
            if (childIdentityValue != null && childIdentityValue.equals(targetIdentityAttrValue)) {
                return childTag;
            }
        }
        final XmlTag newTag = parent.createChildTag(targetTagName, null, "", false);

        if (attributes != null) {
            for (Map.Entry<String, String> attrEntry : attributes.entrySet()) {
                if (attrEntry.getValue() != null) {
                    newTag.setAttribute(attrEntry.getKey(), attrEntry.getValue());
                }
            }
        } else {
            newTag.setAttribute(identityAttrName, targetIdentityAttrValue);
        }
        newTagsQueue.add(newTag);
        newTagsChildParentRelationMap.put(newTag, parent);

        return newTag;
    }

    /**
     * Check if all required data were provided.
     *
     * @param dbSchemaXmlData DbSchemaXmlData
     *
     * @return boolean
     */
    private boolean validateData(final DbSchemaXmlData dbSchemaXmlData) {
        return dbSchemaXmlData.getTableName() != null;
    }

    @Override
    protected void fillAttributes(Properties attributes) {}//NOPMD
}
