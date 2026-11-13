/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.reference.provider;

import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.ProcessingContext;
import com.magento.idea.magento2plugin.indexes.EventIndex;
import com.magento.idea.magento2plugin.reference.xml.PolyVariantReferenceBase;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class EventNameReferenceProvider extends PsiReferenceProvider {

    @NotNull
    @Override
    public PsiReference[] getReferencesByElement(
            @NotNull final PsiElement element,
            @NotNull final ProcessingContext context
    ) {
        final String value = StringUtil.unquoteString(element.getText());
        final List<PsiReference> psiReferences = new ArrayList<>();
        final List<PsiElement> targets = new ArrayList<>();

        for (final EventTargetProvider provider : EventTargetProvider.EP_NAME.getExtensionList()) {
            provider.collectEventTargets(element.getProject(), value, targets);
        }

        final Collection<PsiElement> xmlEvents = new EventIndex(element.getProject())
                .getEventElements(
                        value,
                        GlobalSearchScope.getScopeRestrictedByFileTypes(
                                GlobalSearchScope.allScope(element.getProject()),
                                XmlFileType.INSTANCE
                        )
                );
        for (final PsiElement xmlEvent : xmlEvents) {
            if (xmlEvent != element) {
                targets.add(xmlEvent);
            }
        }

        if (!targets.isEmpty()) {
            psiReferences.add(new PolyVariantReferenceBase(element, targets));
        }

        return psiReferences.toArray(new PsiReference[0]);
    }
}
