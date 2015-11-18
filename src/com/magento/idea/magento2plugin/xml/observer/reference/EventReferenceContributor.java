package com.magento.idea.magento2plugin.xml.observer.reference;

import com.intellij.psi.*;
import com.magento.idea.magento2plugin.xml.observer.PhpPatternsHelper;
import com.magento.idea.magento2plugin.xml.observer.reference.util.ClassResultsFillerWrapper;
import com.magento.idea.magento2plugin.xml.observer.reference.util.EventsConfigurationFilesResultsFiller;
import com.magento.idea.magento2plugin.xml.reference.util.ReferenceResultsFiller;
import org.jetbrains.annotations.NotNull;

/**
 * Created by dkvashnin on 11/2/15.
 */
public class EventReferenceContributor extends PsiReferenceContributor {
    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar psiReferenceRegistrar) {
        psiReferenceRegistrar.registerReferenceProvider(
            PhpPatternsHelper.STRING_METHOD_ARGUMENT,
            new EventReferenceProvider(
                new ReferenceResultsFiller[] {
                    EventsConfigurationFilesResultsFiller.INSTANCE,
                    ClassResultsFillerWrapper.INSTANCE
                }
            )
        );
    }

}