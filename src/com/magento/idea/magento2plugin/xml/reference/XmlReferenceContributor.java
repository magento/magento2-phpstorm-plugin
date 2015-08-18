package com.magento.idea.magento2plugin.xml.reference;

import com.intellij.patterns.XmlPatterns;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceRegistrar;
import com.magento.idea.magento2plugin.xml.XmlHelper;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Warider on 17.08.2015.
 */
public class XmlReferenceContributor extends PsiReferenceContributor {
    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar psiReferenceRegistrar) {
        // type links
        psiReferenceRegistrar.registerReferenceProvider(
            XmlPatterns.or(
                XmlHelper.getDiTypePattern(),
                XmlHelper.getDiPreferencePattern(),
                XmlHelper.getDiVirtualTypePattern(),
                XmlHelper.getArgumentObjectPattern(),
                XmlHelper.getItemObjectPattern()
            ),
            new DiInstanceReferenceProvider()
        );
    }
}
