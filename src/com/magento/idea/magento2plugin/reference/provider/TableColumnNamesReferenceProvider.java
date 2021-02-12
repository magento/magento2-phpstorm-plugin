/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.reference.provider;

import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.impl.source.xml.XmlAttributeImpl;
import com.intellij.psi.impl.source.xml.XmlAttributeValueImpl;
import com.intellij.psi.impl.source.xml.XmlTagImpl;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlDocument;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.ProcessingContext;
import com.intellij.util.indexing.FileBasedIndex;
import com.magento.idea.magento2plugin.magento.files.ModuleDbSchemaXml;
import com.magento.idea.magento2plugin.reference.xml.PolyVariantReferenceBase;
import com.magento.idea.magento2plugin.stubs.indexes.xml.DeclarativeSchemaElementsIndex;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;


/**
 * Reference provider for table and column names in the db_schema.xml files.
 */
public class TableColumnNamesReferenceProvider extends PsiReferenceProvider {
    @NotNull
    @Override
    @SuppressWarnings({"PMD.NPathComplexity", "PMD.CyclomaticComplexity",
            "PMD.AvoidDeeplyNestedIfStmts"})
    public PsiReference[] getReferencesByElement(
            final @NotNull PsiElement element,
            final @NotNull ProcessingContext context
    ) {
        final List<PsiReference> psiReferences = new ArrayList<>();
        String identifier = ((XmlAttributeValueImpl) element).getValue();
        String columnIdentifier = null;
        String tableNameIdentifier = null;

        final String parentTag = getParentTagName(element);
        if (parentTag != null && parentTag.equals(ModuleDbSchemaXml.XML_TAG_CONSTRAINT)) {
            final XmlAttribute attribute = getParentAttribute(element);
            final String attributeName = attribute.getName();

            if (attributeName.equals(ModuleDbSchemaXml.XML_ATTR_CONSTRAINT_COLUMN_NAME)) {
                columnIdentifier = identifier;
                tableNameIdentifier = getNearAttributeValueForElement(
                        attribute,
                        ModuleDbSchemaXml.XML_TAG_CONSTRAINT,
                        ModuleDbSchemaXml.XML_ATTR_CONSTRAINT_TABLE_NAME
                );

                if (tableNameIdentifier != null) {
                    identifier = tableNameIdentifier + "." + columnIdentifier;
                }
            }
            if (attributeName.equals(
                    ModuleDbSchemaXml.XML_ATTR_CONSTRAINT_REFERENCE_COLUMN_NAME)) {
                columnIdentifier = identifier;
                tableNameIdentifier = getNearAttributeValueForElement(
                        attribute,
                        ModuleDbSchemaXml.XML_TAG_CONSTRAINT,
                        ModuleDbSchemaXml.XML_ATTR_CONSTRAINT_REFERENCE_TABLE_NAME
                );

                if (tableNameIdentifier != null) {
                    identifier = tableNameIdentifier + "." + columnIdentifier;
                }
            }
        }
        final Collection<VirtualFile> files = FileBasedIndex.getInstance()
                .getContainingFiles(DeclarativeSchemaElementsIndex.KEY, identifier,
                        GlobalSearchScope.getScopeRestrictedByFileTypes(
                                GlobalSearchScope.allScope(element.getProject()),
                                XmlFileType.INSTANCE
                        )
                );

        final List<PsiElement> psiElements = new ArrayList<>();
        final PsiManager psiManager = PsiManager.getInstance(element.getProject());

        for (final VirtualFile virtualFile : files) {
            if (virtualFile.equals(element.getContainingFile().getVirtualFile())) {
                continue;
            }
            final XmlDocument xmlDocument = ((XmlFile) Objects.requireNonNull(
                    psiManager.findFile(virtualFile))
            ).getDocument();

            if (xmlDocument != null) {
                final XmlTag xmlRootTag = xmlDocument.getRootTag();

                if (xmlRootTag != null) {
                    final XmlTag[] tableTags = xmlRootTag.getSubTags();
                    for (final XmlTag tableTag : tableTags) {
                        final String tableName =
                                tableTag.getAttributeValue(ModuleDbSchemaXml.XML_ATTR_TABLE_NAME);
                        if (tableTag.getName().equals(ModuleDbSchemaXml.XML_TAG_TABLE)
                                && tableName != null
                                && tableName.equals(identifier)
                                && columnIdentifier == null
                        ) {
                            psiElements.add(tableTag);
                        }

                        if (tableName != null && tableName.equals(tableNameIdentifier)) {
                            final XmlTag[] columnTags = tableTag
                                    .findSubTags(ModuleDbSchemaXml.XML_TAG_COLUMN);
                            for (final XmlTag columnTag : columnTags) {
                                final String columnName =
                                        columnTag.getAttributeValue(
                                                ModuleDbSchemaXml.XML_ATTR_COLUMN_NAME
                                        );
                                if (columnName != null
                                        && columnName.equals(columnIdentifier)) {
                                    psiElements.add(columnTag);
                                }
                            }
                        }
                    }
                }
            }
        }

        if (!psiElements.isEmpty()) {
            psiReferences.add(new PolyVariantReferenceBase(element, psiElements));
        }

        return psiReferences.toArray(new PsiReference[0]);
    }

    /**
     * Get parent tag of element.
     *
     * @param element PsiElement
     *
     * @return String
     */
    private String getParentTagName(final PsiElement element) {
        if (element instanceof XmlTagImpl) {
            return ((XmlTagImpl) element).getName();
        } else {
            return getParentTagName(element.getParent());
        }
    }

    /**
     * Get parent attribute from element.
     *
     * @param element PsiElement
     *
     * @return XmlAttribute
     */
    private XmlAttribute getParentAttribute(final PsiElement element) {
        if (element instanceof XmlAttributeImpl) {
            return (XmlAttribute) element;
        } else {
            return getParentAttribute(element.getParent());
        }
    }

    /**
     * Get near attribute value.
     *
     * @param element XmlAttribute
     * @param parentTagName String
     * @param targetAttributeName String
     *
     * @return String
     */
    private String getNearAttributeValueForElement(
            final XmlAttribute element,
            final String parentTagName,
            final String targetAttributeName
    ) {
        final XmlTag parentTag = element.getParent();
        if (parentTag.getName().equals(parentTagName)) {
            return parentTag.getAttributeValue(targetAttributeName);
        }
        return null;
    }
}
