/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.reference.provider;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlText;
import com.intellij.util.ProcessingContext;
import com.magento.idea.magento2plugin.magento.files.UiComponentXml;
import org.jetbrains.annotations.NotNull;

public class UiComponentItemReferenceProvider extends PsiReferenceProvider {
    private final RequireJsPathReferenceProvider requireJsProvider = new RequireJsPathReferenceProvider();
    private final KnockoutTemplatePathReferenceProvider templateProvider = new KnockoutTemplatePathReferenceProvider();
    private final KnockoutRegionReferenceProvider regionProvider = new KnockoutRegionReferenceProvider();

    @Override
    public PsiReference @NotNull [] getReferencesByElement(
            final @NotNull PsiElement element,
            final @NotNull ProcessingContext context
    ) {
        final XmlText xmlText = element instanceof XmlText
                ? (XmlText) element
                : element.getParent() instanceof XmlText ? (XmlText) element.getParent() : null;

        if (xmlText == null || !(xmlText.getParent() instanceof XmlTag)) {
            return PsiReference.EMPTY_ARRAY;
        }
        final XmlTag tag = (XmlTag) xmlText.getParent();
        final String itemName = tag.getAttributeValue(UiComponentXml.XML_ATTRIBUTE_NAME);

        if (UiComponentXml.XML_ATTRIBUTE_COMPONENT.equals(itemName)) {
            return requireJsProvider.getReferencesByElement(element, context);
        }
        if (isTemplateItem(tag, itemName)) {
            return templateProvider.getReferencesByElement(element, context);
        }
        if ("displayArea".equals(itemName)) {
            return regionProvider.getReferencesByElement(element, context);
        }

        return PsiReference.EMPTY_ARRAY;
    }

    private boolean isTemplateItem(
            final @NotNull XmlTag tag,
            final String itemName
    ) {
        if (UiComponentXml.XML_ATTRIBUTE_TEMPLATE.equals(itemName)
                || "childTemplate".equals(itemName)
                || "elementTmpl".equals(itemName)) {
            return true;
        }
        final XmlTag parentTag = tag.getParentTag();

        return parentTag != null
                && "templates".equals(parentTag.getAttributeValue(UiComponentXml.XML_ATTRIBUTE_NAME));
    }
}
