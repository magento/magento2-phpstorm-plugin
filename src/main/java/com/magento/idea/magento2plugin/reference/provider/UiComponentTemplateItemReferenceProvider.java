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

/** Literal template values in UI configuration; component and displayArea semantics remain optional. */
public class UiComponentTemplateItemReferenceProvider extends PsiReferenceProvider {
    private final KnockoutTemplatePathReferenceProvider templateProvider = new KnockoutTemplatePathReferenceProvider();

    @Override
    public PsiReference @NotNull [] getReferencesByElement(final @NotNull PsiElement element,
                                                         final @NotNull ProcessingContext context) {
        final XmlText text = element instanceof XmlText ? (XmlText) element
                : element.getParent() instanceof XmlText ? (XmlText) element.getParent() : null;
        if (text == null || !(text.getParent() instanceof XmlTag)) {
            return PsiReference.EMPTY_ARRAY;
        }
        final XmlTag tag = (XmlTag) text.getParent();
        final String name = tag.getAttributeValue(UiComponentXml.XML_ATTRIBUTE_NAME);
        final XmlTag parent = tag.getParentTag();
        if (UiComponentXml.XML_ATTRIBUTE_TEMPLATE.equals(name) || "childTemplate".equals(name)
                || "elementTmpl".equals(name) || (parent != null
                && "templates".equals(parent.getAttributeValue(UiComponentXml.XML_ATTRIBUTE_NAME)))) {
            return templateProvider.getReferencesByElement(element, context);
        }
        return PsiReference.EMPTY_ARRAY;
    }
}
