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
import com.magento.idea.magento2plugin.bundles.CommonBundle;
import com.magento.idea.magento2plugin.bundles.ValidatorBundle;
import com.magento.idea.magento2plugin.magento.files.ModuleDbSchemaXml;
import com.magento.idea.magento2plugin.magento.packages.database.ColumnAttributes;
import com.magento.idea.magento2plugin.magento.packages.database.TableColumnTypes;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.swing.JOptionPane;
import org.jetbrains.annotations.NotNull;

public class DbSchemaXmlGenerator extends FileGenerator {

    private final Project project;
    private final String moduleName;
    private final DbSchemaXmlData dbSchemaXmlData;
    private final FindOrCreateDbSchemaXmlUtil findOrCreateDbSchemaXmlUtil;
    private final ValidatorBundle validatorBundle;
    private final CommonBundle commonBundle;

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
        this.validatorBundle = new ValidatorBundle();
        this.commonBundle = new CommonBundle();
        findOrCreateDbSchemaXmlUtil = new FindOrCreateDbSchemaXmlUtil(project);

        newTagsQueue = new LinkedList<>();
        newTagsChildParentRelationMap = new HashMap<>();
    }

    @Override
    @SuppressWarnings({
            "PMD.NPathComplexity",
            "PMD.CyclomaticComplexity",
            "PMD.CognitiveComplexity",
            "PMD.ExcessiveImports",
            "PMD.AvoidInstantiatingObjectsInLoops"
    })
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
        final XmlTag tableTag = findOrCreateTag(
                ModuleDbSchemaXml.XML_TAG_TABLE,
                ModuleDbSchemaXml.XML_ATTR_TABLE_NAME,
                rootTag,
                dbSchemaXmlData.getTableName(),
                dbSchemaXmlData.getTableAttributesMap()
        );

        boolean hasPrimaryKey = false;
        final Map<String, String> primaryKeyData = new HashMap<>();

        for (final Map<String, String> columnData : dbSchemaXmlData.getColumns()) {
            final String identityAttrValue =
                    columnData.get(ColumnAttributes.IDENTITY.getName());

            if (!hasPrimaryKey && Boolean.parseBoolean(identityAttrValue)) {
                hasPrimaryKey = true;
                primaryKeyData.putAll(columnData);
            }

            final String columnName = columnData.get(ColumnAttributes.NAME.getName());
            final String columnTypeValue = columnData.get(ColumnAttributes.TYPE.getName());
            final TableColumnTypes columnType = TableColumnTypes.getByValue(columnTypeValue);

            if (columnType == null) {
                final String errorMessage = validatorBundle.message(
                        "validator.dbSchema.invalidColumnType",
                        columnName == null ? "" : columnName

                );
                JOptionPane.showMessageDialog(
                        null,
                        errorMessage,
                        commonBundle.message("common.error"),
                        JOptionPane.ERROR_MESSAGE
                );
                return null;
            }

            final Map<String, String> attributes = new LinkedHashMap<>();
            final List<String> allowedColumns = ModuleDbSchemaXml.getAllowedAttributes(columnType);

            for (final Map.Entry<String, String> columnDataEntry : columnData.entrySet()) {
                if (allowedColumns.contains(columnDataEntry.getKey())
                        && !columnDataEntry.getValue().isEmpty()) {
                    attributes.put(columnDataEntry.getKey(), columnDataEntry.getValue());
                }
            }
            findOrCreateTag(
                    ModuleDbSchemaXml.XML_TAG_COLUMN,
                    ColumnAttributes.NAME.getName(),
                    tableTag,
                    columnName,
                    attributes
            );
        }

        if (hasPrimaryKey && !primaryKeyData.isEmpty()) {
            generatePrimaryKey(primaryKeyData, tableTag);
        }

        return commitDbSchemaXmlFile(dbSchemaXmlFile);
    }

    /**
     * Generate PK constraint and its index.
     *
     * @param primaryKeyData Map
     * @param tableTag XmlTag
     */
    private void generatePrimaryKey(
            @NotNull final Map<String, String> primaryKeyData,
            final XmlTag tableTag
    ) {
        final Map<String, String> attributes = new LinkedHashMap<>();
        attributes.put(
                ColumnAttributes.TYPE.getName(),
                ModuleDbSchemaXml.XML_ATTR_TYPE_PK
        );
        attributes.put(
                ModuleDbSchemaXml.XML_ATTR_CONSTRAINT_REFERENCE_ID_NAME,
                ModuleDbSchemaXml.XML_ATTR_REFERENCE_ID_PK
        );

        final XmlTag pkTag = findOrCreateTag(
                ModuleDbSchemaXml.XML_TAG_CONSTRAINT,
                ModuleDbSchemaXml.XML_ATTR_CONSTRAINT_REFERENCE_ID_NAME,
                tableTag,
                ModuleDbSchemaXml.XML_ATTR_REFERENCE_ID_PK,
                attributes
        );
        final Map<String, String> pkColumnAttributes = new HashMap<>();
        final String columnIdentityValue = primaryKeyData.get(
                ColumnAttributes.NAME.getName()
        );
        pkColumnAttributes.put(ColumnAttributes.NAME.getName(), columnIdentityValue);

        findOrCreateTag(
                ModuleDbSchemaXml.XML_TAG_COLUMN,
                ColumnAttributes.NAME.getName(),
                pkTag,
                columnIdentityValue,
                pkColumnAttributes
        );

        final Map<String, String> pkIndexAttributes = new LinkedHashMap<>();
        final List<String> indexColumnsNames = new LinkedList<>();
        indexColumnsNames.add(columnIdentityValue);

        pkIndexAttributes.put(
                ModuleDbSchemaXml.XML_ATTR_CONSTRAINT_REFERENCE_ID_NAME,
                ModuleDbSchemaXml.generateIndexReferenceId(
                        dbSchemaXmlData.getTableName(),
                        indexColumnsNames
                )
        );
        pkIndexAttributes.put(
                ModuleDbSchemaXml.XML_ATTR_INDEX_TYPE_NAME,
                ModuleDbSchemaXml.XML_ATTR_INDEX_TYPE_BTREE
        );

        final XmlTag pkIndexTag = findOrCreateTag(
                ModuleDbSchemaXml.XML_TAG_INDEX,
                ModuleDbSchemaXml.XML_ATTR_CONSTRAINT_REFERENCE_ID_NAME,
                tableTag,
                ModuleDbSchemaXml.XML_ATTR_REFERENCE_ID_PK,
                pkIndexAttributes
        );

        findOrCreateTag(
                ModuleDbSchemaXml.XML_TAG_COLUMN,
                ColumnAttributes.NAME.getName(),
                pkIndexTag,
                columnIdentityValue,
                pkColumnAttributes
        );
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
                    parent.addSubTag(tag, true);
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
        final XmlTag newTag = parent.createChildTag(targetTagName, null, null, false);

        if (attributes == null) {
            newTag.setAttribute(identityAttrName, targetIdentityAttrValue);
        } else {
            for (final Map.Entry<String, String> attrEntry : attributes.entrySet()) {
                if (attrEntry.getValue() != null) {
                    newTag.setAttribute(attrEntry.getKey(), attrEntry.getValue());
                }
            }
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
    @SuppressWarnings("PMD.UncommentedEmptyMethodBody")
    protected void fillAttributes(final Properties attributes) {}
}
