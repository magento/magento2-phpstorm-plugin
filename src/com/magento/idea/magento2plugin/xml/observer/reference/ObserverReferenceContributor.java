package com.magento.idea.magento2plugin.xml.observer.reference;

import com.intellij.psi.*;
import com.magento.idea.magento2plugin.xml.di.reference.provider.XmlReferenceProvider;
import com.magento.idea.magento2plugin.xml.observer.XmlHelper;
import com.magento.idea.magento2plugin.xml.observer.reference.util.EventsDeclarationsFilesResultsFiller;
import com.magento.idea.magento2plugin.xml.reference.util.ClassesResultsFiller;
import com.magento.idea.magento2plugin.xml.reference.util.ReferenceResultsFiller;
import com.magento.idea.magento2plugin.xml.reference.util.VirtualTypesResultsFiller;
import org.jetbrains.annotations.NotNull;


/**
 * Created by dkvashnin on 11/2/15.
 */
public class ObserverReferenceContributor extends PsiReferenceContributor {
    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar psiReferenceRegistrar) {
        // <observer instance="\Namespace\Class" />
        psiReferenceRegistrar.registerReferenceProvider(
            XmlHelper.getTagAttributeValuePattern(XmlHelper.OBSERVER_TAG, XmlHelper.INSTANCE_ATTRIBUTE),
            new XmlReferenceProvider(
                new ReferenceResultsFiller[]{
                    ClassesResultsFiller.INSTANCE,
                    VirtualTypesResultsFiller.INSTANCE
                }
            )
        );

        psiReferenceRegistrar.registerReferenceProvider(
            XmlHelper.getTagAttributeValuePattern(XmlHelper.EVENT_TAG, XmlHelper.NAME_ATTRIBUTE),
            new XmlReferenceProvider(
                new ReferenceResultsFiller[]{
                    EventsDeclarationsFilesResultsFiller.INSTANCE
                }
            )
        );
    }
}
