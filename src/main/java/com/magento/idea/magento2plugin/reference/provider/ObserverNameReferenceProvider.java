package com.magento.idea.magento2plugin.reference.provider;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlTag;
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

        if (!(element instanceof XmlAttributeValue)
                || !(element.getParent() instanceof XmlAttribute)
                || !(element.getParent().getParent() instanceof XmlTag)
                || !(element.getParent().getParent().getParent() instanceof XmlTag)) {
            return psiReferences.toArray(new PsiReference[0]);
        }

        final XmlTag observerTag = (XmlTag) element.getParent().getParent();
        final XmlTag eventTag = (XmlTag) observerTag.getParent();

        if (!"event".equals(eventTag.getName())) {
            return psiReferences.toArray(new PsiReference[0]);
        }

        final String eventName = eventTag.getAttributeValue("name");

        if (eventName == null) {
            return psiReferences.toArray(new PsiReference[0]);
        }

        final String observerName = ((XmlAttributeValue) element).getValue();
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
