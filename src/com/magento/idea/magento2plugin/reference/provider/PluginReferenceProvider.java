/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.reference.provider;

import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.ProcessingContext;
import com.magento.idea.magento2plugin.indexes.PluginIndex;
import com.magento.idea.magento2plugin.magento.files.ModuleDiXml;
import com.magento.idea.magento2plugin.reference.xml.PolyVariantReferenceBase;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class PluginReferenceProvider extends PsiReferenceProvider {

    @SuppressWarnings({
            "PMD.CognitiveComplexity",
            "PMD.CyclomaticComplexity",
            "PMD.NPathComplexity"
    })
    @Override
    public @NotNull PsiReference[] getReferencesByElement(
            final @NotNull PsiElement element,
            final @NotNull ProcessingContext context
    ) {
        if (!(element.getParent() instanceof XmlAttribute)
                || !ModuleDiXml.NAME_ATTR.equals(((XmlAttribute) element.getParent()).getName())
                || !(element.getParent().getParent() instanceof XmlTag)
                || !ModuleDiXml.PLUGIN_TAG_NAME.equals(
                        ((XmlTag) element.getParent().getParent()).getName())
        ) {
            return PsiReference.EMPTY_ARRAY;
        }

        final XmlTag originalPluginTag = (XmlTag) element.getParent().getParent();
        final XmlTag originalTypeTag = originalPluginTag.getParentTag();

        if (originalTypeTag == null || !ModuleDiXml.TYPE_TAG.equals(originalTypeTag.getName())) {
            return PsiReference.EMPTY_ARRAY;
        }
        final XmlAttribute originalPluginNameAttr = originalPluginTag.getAttribute("name");
        final XmlAttribute originalTypeNameAttr = originalTypeTag.getAttribute("name");

        if (originalPluginNameAttr == null || originalTypeNameAttr == null) {
            return PsiReference.EMPTY_ARRAY;
        }
        final String originalPluginName = originalPluginNameAttr.getValue();
        final String originalTypeName = originalTypeNameAttr.getValue();

        if (originalPluginName == null || originalTypeName == null) {
            return PsiReference.EMPTY_ARRAY;
        }
        final Project project = element.getProject();

        final Collection<PsiElement> types = PluginIndex.getInstance(project).getPluginElements(
                originalTypeName,
                GlobalSearchScope.getScopeRestrictedByFileTypes(
                        GlobalSearchScope.allScope(project), XmlFileType.INSTANCE
                )
        );
        final List<PsiElement> psiElements = new ArrayList<>();

        for (final PsiElement type: types) {
            final XmlTag typeTag = (XmlTag) type.getParent().getParent();
            final XmlTag[] pluginTags = typeTag.findSubTags("plugin");

            for (final XmlTag pluginTag: pluginTags) {
                final XmlAttribute pluginNameAttribute = pluginTag.getAttribute("name");

                if (pluginNameAttribute != null
                        && pluginNameAttribute.getValue() != null
                        && originalPluginName.equals(pluginNameAttribute.getValue())) {
                    psiElements.add(pluginNameAttribute.getValueElement());
                }
            }
        }
        final List<PsiReference> psiReferences = new ArrayList<>();

        if (!psiElements.isEmpty()) {
            final int startIndex = element.getText().indexOf(originalPluginName);
            final int endIndex = startIndex + originalPluginName.length();
            final TextRange range = new TextRange(startIndex, endIndex);

            psiReferences.add(new PolyVariantReferenceBase(element, range, psiElements));
        }

        return psiReferences.toArray(new PsiReference[0]);
    }
}
