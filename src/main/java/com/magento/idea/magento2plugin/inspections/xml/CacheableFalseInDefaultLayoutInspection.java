/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.inspections.xml;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.codeInspection.XmlSuppressableInspectionTool;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.XmlElementVisitor;
import com.intellij.psi.xml.XmlAttribute;
import com.magento.idea.magento2plugin.bundles.InspectionBundle;
import com.magento.idea.magento2plugin.inspections.xml.fix.XmlRemoveCacheableAttributeQuickFix;
import com.magento.idea.magento2plugin.magento.files.LayoutXml;
import org.jetbrains.annotations.NotNull;

public class CacheableFalseInDefaultLayoutInspection extends XmlSuppressableInspectionTool {
    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(final @NotNull ProblemsHolder holder, final boolean isOnTheFly) {
        return new XmlElementVisitor() {
            private InspectionBundle inspectionBundle = new InspectionBundle();

            @Override
            public void visitXmlAttribute(XmlAttribute attribute) {
                String fileName = holder.getFile().getName();
                if (!fileName.equals(LayoutXml.DEFAULT_FILENAME)) return;
                final String text = attribute.getValue();
                final String attributeName = attribute.getName();
                if (!attributeName.equals(LayoutXml.CACHEABLE_ATTRIBUTE_NAME)) return;
                if (!attribute.getParent().getName().equals(LayoutXml.BLOCK_ATTRIBUTE_TAG_NAME)
                && !attribute.getParent().getName().equals(LayoutXml.REFERENCE_BLOCK_ATTRIBUTE_TAG_NAME)) return;
                if (text == null) return;
                if (text.equals(LayoutXml.CACHEABLE_ATTRIBUTE_VALUE_FALSE)) {
                    holder.registerProblem(
                        attribute,
                        inspectionBundle.message("inspection.cache.disabledCache"),
                        ProblemHighlightType.WARNING,
                        new XmlRemoveCacheableAttributeQuickFix()
                    );
                }
            }
        };
    }
}
