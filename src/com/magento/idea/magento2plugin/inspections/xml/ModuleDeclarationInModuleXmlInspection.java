/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.inspections.xml;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.codeInspection.XmlSuppressableInspectionTool;
import com.intellij.patterns.XmlAttributeValuePattern;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.XmlElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlDocument;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.magento.idea.magento2plugin.bundles.InspectionBundle;
import com.magento.idea.magento2plugin.inspections.util.GetEditableModuleNameByRootFileUtil;
import com.magento.idea.magento2plugin.inspections.xml.fix.XmlModuleNameQuickFix;
import com.magento.idea.magento2plugin.magento.files.ModuleXml;
import com.magento.idea.magento2plugin.util.magento.IsFileInEditableModuleUtil;
import org.jetbrains.annotations.NotNull;

public class ModuleDeclarationInModuleXmlInspection extends XmlSuppressableInspectionTool {

    @Override
    public @NotNull PsiElementVisitor buildVisitor(
            final @NotNull ProblemsHolder problemsHolder,
            final boolean isOnTheFly
    ) {
        return new XmlElementVisitor() {

            @Override
            public void visitXmlAttributeValue(final XmlAttributeValue value) {
                final PsiFile file = value.getContainingFile();
                final String filename = file.getName();

                if (!ModuleXml.FILE_NAME.equals(filename)) {
                    return;
                }

                if (!IsFileInEditableModuleUtil.execute(file)) {
                    return;
                }

                if (isSubTag(value, (XmlFile) file)) {
                    return;
                }
                final PsiDirectory etcDirectory = file.getParent();

                if (etcDirectory == null) {
                    return;
                }
                final String attributeName = XmlAttributeValuePattern.getLocalName(value);

                if (attributeName != null && attributeName.equals(ModuleXml.MODULE_ATTR_NAME)) {
                    final String expectedName
                            = GetEditableModuleNameByRootFileUtil.execute(etcDirectory);
                    final String actualName = value.getValue();

                    if (actualName.equals(expectedName) || actualName.trim().isEmpty()) {
                        return;
                    }
                    final InspectionBundle inspectionBundle = new InspectionBundle();

                    problemsHolder.registerProblem(
                            value,
                            inspectionBundle.message(
                                    "inspection.moduleDeclaration.warning.wrongModuleName",
                                    actualName,
                                    expectedName
                            ),
                            ProblemHighlightType.ERROR,
                            new XmlModuleNameQuickFix(expectedName)
                    );
                }
            }
        };
    }

    protected boolean isSubTag(final XmlAttributeValue value, final XmlFile file) {
        final XmlAttribute xmlAttribute = PsiTreeUtil.getParentOfType(value, XmlAttribute.class);

        if (xmlAttribute == null) {
            return true;
        }
        final XmlTag xmlTag = PsiTreeUtil.getParentOfType(xmlAttribute, XmlTag.class);

        if (xmlTag == null) {
            return true;
        }
        final XmlDocument xmlDocument = file.getDocument();

        if (xmlDocument == null) {
            return true;
        }
        final XmlTag xmlRootTag = xmlDocument.getRootTag();

        if (xmlRootTag == null) {
            return true;
        }
        final XmlTag rootTag = PsiTreeUtil.getParentOfType(xmlTag, XmlTag.class);

        return rootTag == null || !(rootTag.getName().equals(xmlRootTag.getName()));
    }
}
