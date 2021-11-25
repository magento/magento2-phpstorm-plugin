package com.magento.idea.magento2plugin.reference.provider;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.impl.source.xml.XmlAttributeValueImpl;
import com.intellij.psi.impl.source.xml.XmlTagImpl;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.ProcessingContext;
import com.magento.idea.magento2plugin.indexes.EventIndex;
import com.magento.idea.magento2plugin.reference.xml.PolyVariantReferenceBase;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class ObserverNameReferenceProvider extends PsiReferenceProvider {
    @NotNull
    @Override
    public PsiReference [] getReferencesByElement(
            @NotNull final PsiElement element,
            @NotNull final ProcessingContext context
    ) {
        final List<PsiReference> psiReferences = new ArrayList<>();
        final XmlTagImpl eventTag = (XmlTagImpl) element.getParent().getParent().getParent();
        final String eventName = eventTag.getAttributeValue("name");

        if (eventName == null) {
            return psiReferences.toArray(new PsiReference[0]);
        }

        final String observerName = ((XmlAttributeValueImpl) element).getValue();
        final Collection<PsiElement> observers
                = new EventIndex(element.getProject()).getObservers(
                        eventName, observerName, GlobalSearchScope.allScope(element.getProject())
                );
        observers.removeIf(observer -> observer == element);

        if (!observers.isEmpty()) {
            psiReferences.add(new PolyVariantReferenceBase(element, observers));
        }

        return psiReferences.toArray(new PsiReference[0]);
    }
}
