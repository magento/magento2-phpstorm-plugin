package com.magento.idea.magento2plugin.xml.observer.reference;

import com.intellij.patterns.XmlPatterns;
import com.intellij.psi.*;
import com.magento.idea.magento2plugin.xml.di.reference.provider.DiInstanceReferenceProvider;
import com.magento.idea.magento2plugin.xml.reference.XmlHelperUtility;
import com.magento.idea.magento2plugin.xml.reference.util.ClassesResultsFiller;
import com.magento.idea.magento2plugin.xml.reference.util.ResolveResultsFiller;
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
            XmlPatterns.or(
                XmlHelperUtility.getTagAttributeValuePattern("observer", "instance", "events")
            ),
            new DiInstanceReferenceProvider(
                new ResolveResultsFiller[]{
                    ClassesResultsFiller.INSTANCE,
                    VirtualTypesResultsFiller.INSTANCE
                }
            )
        );
    }
}
