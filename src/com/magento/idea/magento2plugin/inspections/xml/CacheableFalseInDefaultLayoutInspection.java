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
import com.magento.idea.magento2plugin.inspections.xml.fix.XmlRemoveCacheableAttributeQuickFix;
import com.magento.idea.magento2plugin.magento.files.LayoutXml;
import org.jetbrains.annotations.NotNull;

public class CacheableFalseInDefaultLayoutInspection extends XmlSuppressableInspectionTool {
    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(final @NotNull ProblemsHolder holder, final boolean isOnTheFly) {
        return new XmlElementVisitor() {
            private static final String CacheDisableProblemDescription = "Cacheable false attribute on the default layout will disable cache site-wide";
            @Override
            public void visitXmlAttribute(XmlAttribute attribute) {
                String fileName = holder.getFile().getName();
                if (!fileName.equals(LayoutXml.DefaultFileName)) return;
                final String text = attribute.getValue();
                final String attributeName = attribute.getName();
                if (!attributeName.equals(LayoutXml.CacheableAttributeName)) return;
                if (!attribute.getParent().getName().equals(LayoutXml.BlockAttributeTagName)) return;
                if (text == null) return;
                if (text.equals(LayoutXml.CacheableAttributeFalseValue)) {
                    holder.registerProblem(attribute, CacheDisableProblemDescription,
                            ProblemHighlightType.WARNING,
                            new XmlRemoveCacheableAttributeQuickFix());
                }
            }
        };
    }
}
