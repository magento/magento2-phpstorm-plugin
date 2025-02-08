/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.json.psi.JsonElement;
import com.intellij.json.psi.JsonElementGenerator;
import com.intellij.json.psi.JsonFile;
import com.intellij.json.psi.JsonObject;
import com.intellij.json.psi.JsonProperty;
import com.intellij.json.psi.JsonPsiUtil;
import com.intellij.json.psi.impl.JsonObjectImpl;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.actions.generation.data.DbSchemaXmlData;
import com.magento.idea.magento2plugin.actions.generation.generator.util.FindOrCreateDbSchemaWhitelistJson;
import com.magento.idea.magento2plugin.magento.files.ModuleDbSchemaWhitelistJson;
import com.magento.idea.magento2plugin.magento.files.ModuleDbSchemaXml;
import com.magento.idea.magento2plugin.magento.packages.database.ColumnAttributes;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import org.jetbrains.annotations.NotNull;

/**
 * The db_schema_whitelist.json file generator.
 */
public class DbSchemaWhitelistJsonGenerator extends FileGenerator {

    private final Project project;
    private final JsonElementGenerator jsonElementGenerator;
    private final DbSchemaXmlData dbSchemaXmlData;
    private final String moduleName;

    /**
     * The db_schema_whitelist.json file generator constructor.
     *
     * @param project Project
     * @param dbSchemaXmlData DbSchemaXmlData
     * @param moduleName String
     */
    public DbSchemaWhitelistJsonGenerator(
            final @NotNull Project project,
            final @NotNull DbSchemaXmlData dbSchemaXmlData,
            final @NotNull String moduleName
    ) {
        super(project);
        this.project = project;
        jsonElementGenerator = new JsonElementGenerator(project);
        this.dbSchemaXmlData = dbSchemaXmlData;
        this.moduleName = moduleName;
    }

    /**
     * Generate db_schema_whitelist.json file.
     *
     * @param actionName String
     *
     * @return PsiFile
     */
    @Override
    public PsiFile generate(final String actionName) {
        final FindOrCreateDbSchemaWhitelistJson findOrCreateDbSchemaWhitelistJson =
                new FindOrCreateDbSchemaWhitelistJson(project);
        final JsonFile dbSchemaWhitelist = (JsonFile)
                findOrCreateDbSchemaWhitelistJson.execute(actionName, moduleName);

        final JsonElement topNode = dbSchemaWhitelist.getTopLevelValue();

        for (final JsonProperty prop : ((JsonObjectImpl) topNode).getPropertyList()) {
            if (prop.getName().equals(dbSchemaXmlData.getTableName())) {
                return dbSchemaWhitelist;
            }
        }

        WriteCommandAction.runWriteCommandAction(project, () -> {
            final JsonObject tableObject = jsonElementGenerator.createObject("");
            final JsonObject columnsObject = jsonElementGenerator.createObject("");
            final JsonObject constraintObject = jsonElementGenerator.createObject("");
            final JsonObject indexObject = jsonElementGenerator.createObject("");

            for (final Map<String, String> columnData : dbSchemaXmlData.getColumns()) {
                final String columnName = columnData.get(ColumnAttributes.NAME.getName());
                final boolean isIdentity =
                        Boolean.parseBoolean(
                                columnData.get(ColumnAttributes.IDENTITY.getName())
                        );

                if (isIdentity) {
                    JsonPsiUtil.addProperty(
                            constraintObject,
                            jsonElementGenerator.createProperty(
                                    ModuleDbSchemaXml.XML_ATTR_REFERENCE_ID_PK,
                                    "true"
                            ),
                            false
                    );

                    final String indexReferenceId = ModuleDbSchemaXml.generateIndexReferenceId(
                            dbSchemaXmlData.getTableName(),
                            Arrays.asList(columnName)
                    );

                    JsonPsiUtil.addProperty(
                            indexObject,
                            jsonElementGenerator.createProperty(
                                    indexReferenceId,
                                    "true"
                            ),
                            false
                    );
                }

                JsonPsiUtil.addProperty(
                        columnsObject,
                        jsonElementGenerator.createProperty(columnName, "true"),
                        false
                );
            }

            addChildObjectToObject(
                    tableObject,
                    ModuleDbSchemaWhitelistJson.COLUMN_OBJECT_NAME,
                    columnsObject
            );

            if (!indexObject.getPropertyList().isEmpty()) {
                addChildObjectToObject(
                        tableObject,
                        ModuleDbSchemaWhitelistJson.INDEX_OBJECT_NAME,
                        indexObject
                );
            }

            if (!constraintObject.getPropertyList().isEmpty()) {
                addChildObjectToObject(
                        tableObject,
                        ModuleDbSchemaWhitelistJson.CONSTRAINT_OBJECT_NAME,
                        constraintObject
                );
            }

            addChildObjectToObject(
                    (JsonObject) topNode,
                    dbSchemaXmlData.getTableName(),
                    tableObject
            );
        });

        final PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(project);
        final Document document = psiDocumentManager.getDocument(dbSchemaWhitelist);

        if (document != null) {
            psiDocumentManager.commitDocument(document);
        }

        return dbSchemaWhitelist;
    }

    /**
     * Add child object with specified name to the target object.
     *
     * @param target JsonObject
     * @param childName String
     * @param child JsonObject
     */
    private void addChildObjectToObject(
            final @NotNull JsonObject target,
            final @NotNull String childName,
            final @NotNull JsonObject child
    ) {
        JsonPsiUtil.addProperty(
                target,
                jsonElementGenerator.createProperty(
                        childName,
                        child.getText()
                ),
                false
        );
    }

    @Override
    @SuppressWarnings("PMD.UncommentedEmptyMethodBody")
    protected void fillAttributes(final Properties attributes) {}
}
