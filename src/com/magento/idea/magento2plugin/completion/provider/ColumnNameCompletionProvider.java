/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.completion.provider;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.xml.XmlAttributeImpl;
import com.intellij.psi.impl.source.xml.XmlTagImpl;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.ProcessingContext;
import com.intellij.util.indexing.FileBasedIndex;
import com.magento.idea.magento2plugin.magento.files.ModuleDbSchemaXml;
import com.magento.idea.magento2plugin.stubs.indexes.xml.DeclarativeSchemaElementsIndex;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

/**
 * Provides column names for completion.
 */
public class ColumnNameCompletionProvider extends CompletionProvider<CompletionParameters> {
    @Override
    protected void addCompletions(
            final @NotNull CompletionParameters parameters,
            final @NotNull ProcessingContext context,
            final @NotNull CompletionResultSet result
    ) {
        final PsiElement position = parameters.getPosition().getOriginalElement();
        final String currentAttrName = getCurrentAttributeName(position);
        if (position == null || currentAttrName == null) {
            return;
        }
        String targetTableAttrName;

        switch (currentAttrName) {
            case ModuleDbSchemaXml.XML_ATTR_CONSTRAINT_COLUMN_NAME:
                targetTableAttrName = ModuleDbSchemaXml.XML_ATTR_CONSTRAINT_TABLE_NAME;
                break;
            case ModuleDbSchemaXml.XML_ATTR_CONSTRAINT_REFERENCE_COLUMN_NAME:
                targetTableAttrName = ModuleDbSchemaXml.XML_ATTR_CONSTRAINT_REFERENCE_TABLE_NAME;
                break;
            default:
                return;
        }
        final String targetTableName = getTargetTableFromPositionAndAttrName(
                position,
                targetTableAttrName
        );

        if (targetTableName == null) {
            return;
        }
        final Collection<String> tableAndColumnNames = FileBasedIndex.getInstance().getAllKeys(
                DeclarativeSchemaElementsIndex.KEY, position.getProject()
        );
        final List<String> filteredColumnNames = tableAndColumnNames.stream()
                .filter(name -> name.contains(targetTableName + ".")).collect(Collectors.toList())
                .stream().map(name -> name.substring(name.indexOf(".") + 1))
                .collect(Collectors.toList());

        for (final String columnName: filteredColumnNames) {
            result.addElement(LookupElementBuilder.create(columnName));
        }
    }

    /**
     * Get attribute name from position.
     *
     * @param position PsiElement
     *
     * @return String
     */
    private String getCurrentAttributeName(final PsiElement position) {
        if (position instanceof XmlAttributeImpl) {
            return ((XmlAttributeImpl) position).getName();
        } else {
            return getCurrentAttributeName(position.getParent());
        }
    }

    /**
     * Get reference table name from current position.
     *
     * @param position PsiElement
     * @param targetTableAttrName String
     *
     * @return String
     */
    private String getTargetTableFromPositionAndAttrName(
            final PsiElement position,
            final String targetTableAttrName
    ) {
        if (targetTableAttrName == null) {
            return null;
        }

        if (position instanceof XmlTagImpl
                && ((XmlTag) position).getName().equals(ModuleDbSchemaXml.XML_TAG_CONSTRAINT)) {
            return ((XmlTag) position)
                    .getAttributeValue(targetTableAttrName);
        } else {
            return getTargetTableFromPositionAndAttrName(
                    position.getParent(),
                    targetTableAttrName
            );
        }
    }
}
