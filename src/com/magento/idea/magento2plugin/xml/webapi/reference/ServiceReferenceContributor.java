package com.magento.idea.magento2plugin.xml.webapi.reference;

import com.intellij.patterns.XmlAttributeValuePattern;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceRegistrar;
import com.magento.idea.magento2plugin.xml.di.reference.provider.XmlReferenceProvider;
import com.magento.idea.magento2plugin.xml.reference.util.InterfacesResultsFiller;
import com.magento.idea.magento2plugin.xml.reference.util.ReferenceResultsFiller;
import com.magento.idea.magento2plugin.xml.webapi.XmlHelper;
import com.magento.idea.magento2plugin.xml.webapi.reference.fill.ServiceMethodResultsFiller;
import org.jetbrains.annotations.NotNull;

/**
 * Created by isentiabov on 20.12.2015.
 */
public class ServiceReferenceContributor extends PsiReferenceContributor{
    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar psiReferenceRegistrar) {
        // <service class="\Namespace\Interface" />
        XmlAttributeValuePattern serviceTag = XmlHelper.getTagAttributeValuePattern(
            XmlHelper.SERVICE_TAG,
            XmlHelper.CLASS_ATTRIBUTE
        );
        psiReferenceRegistrar.registerReferenceProvider(
            serviceTag,
            new XmlReferenceProvider(new ReferenceResultsFiller[]{
                InterfacesResultsFiller.INSTANCE
            }));

        // <service method="MethodName"/>
        XmlAttributeValuePattern methodAttribute = XmlHelper.getMethodAttributePattern();
        psiReferenceRegistrar.registerReferenceProvider(
            methodAttribute,
            new XmlReferenceProvider(new ReferenceResultsFiller[]{
                new ServiceMethodResultsFiller()
            })
        );
    }
}
