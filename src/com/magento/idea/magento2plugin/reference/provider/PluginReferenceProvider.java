/**
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
import com.magento.idea.magento2plugin.reference.xml.PolyVariantReferenceBase;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class PluginReferenceProvider extends PsiReferenceProvider {
    @Override
    public @NotNull PsiReference[] getReferencesByElement(
            @NotNull final PsiElement element,
            @NotNull final ProcessingContext context
    ) {
        final List<PsiReference> psiReferences = new ArrayList<>();
        final Project project = element.getProject();
        final List<PsiElement> psiElements = new ArrayList<>();

        final XmlTag originalPluginTag = (XmlTag) element.getParent().getParent();
        final XmlTag originalTypeTag = originalPluginTag.getParentTag();
        final String originalPluginName = originalPluginTag.getAttribute("name").getValue();
        final String originalTypeName = originalTypeTag.getAttribute("name").getValue();

        final Collection<PsiElement> types = PluginIndex.getInstance(project).getPluginElements(
                originalTypeName,
                GlobalSearchScope.getScopeRestrictedByFileTypes(
                        GlobalSearchScope.allScope(project), XmlFileType.INSTANCE
                )
        );

        for (final PsiElement type: types) {
            final XmlTag typeTag = (XmlTag) type.getParent().getParent();
            final XmlTag[] pluginTags = typeTag.findSubTags("plugin");
            for (final XmlTag pluginTag: pluginTags) {
                final XmlAttribute pluginNameAttribute = pluginTag.getAttribute("name");
                if (pluginNameAttribute.getValue().equals(originalPluginName)) {
                    psiElements.add(pluginNameAttribute.getValueElement());
                }
            }
        }

        if (!psiElements.isEmpty()) {
            final int startIndex = element.getText().indexOf(originalPluginName);
            final int endIndex = startIndex + originalPluginName.length();
            final TextRange range = new TextRange(startIndex, endIndex);
            psiReferences.add(new PolyVariantReferenceBase(element, range, psiElements));
        }

        return psiReferences.toArray(new PsiReference[0]);
    }
}
