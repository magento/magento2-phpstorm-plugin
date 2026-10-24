/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.inspections.php;

import com.intellij.codeInsight.daemon.ImplicitUsageProvider;
import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlToken;
import com.intellij.psi.xml.XmlTokenType;
import com.jetbrains.php.lang.psi.elements.Field;
import com.magento.idea.magento2plugin.magento.files.ModuleDiXml;
import com.magento.idea.magento2plugin.project.Settings;
import org.jetbrains.annotations.NotNull;

/**
 * Includes DI constant reads in PHP's unused-symbol analysis.
 */
public final class DiConstantImplicitUsageProvider implements ImplicitUsageProvider {

    @Override
    public boolean isImplicitUsage(final @NotNull PsiElement element) {
        return isImplicitRead(element);
    }

    @Override
    public boolean isImplicitRead(final @NotNull PsiElement element) {
        if (!(element instanceof Field field) || !field.isConstant()
                || !field.getModifier().isPublic()
                || !Settings.getInstance(element.getProject()).pluginEnabled) {
            return false;
        }

        final GlobalSearchScope scope = GlobalSearchScope.getScopeRestrictedByFileTypes(
                GlobalSearchScope.allScope(element.getProject()), XmlFileType.INSTANCE);
        // Stop at the first resolved DI reference; matching text alone is not a usage.
        return !ReferencesSearch.search(field, scope)
                .forEach(reference -> !isDiConstantReference(reference));
    }

    @Override
    public boolean isImplicitWrite(final @NotNull PsiElement element) {
        return false;
    }

    private static boolean isDiConstantReference(final PsiReference reference) {
        final PsiElement element = reference.getElement();
        if (!(element instanceof XmlToken token)
                || token.getTokenType() != XmlTokenType.XML_DATA_CHARACTERS
                || !ModuleDiXml.FILE_NAME.equals(element.getContainingFile().getName())) {
            return false;
        }

        final XmlTag tag = PsiTreeUtil.getParentOfType(element, XmlTag.class);
        return tag != null
                && (ModuleDiXml.ARGUMENT_TAG.equals(tag.getName())
                    || ModuleDiXml.ITEM_TAG.equals(tag.getName()))
                && "const".equals(tag.getAttributeValue(ModuleDiXml.XSI_TYPE_ATTR));
    }
}
