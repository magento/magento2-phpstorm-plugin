package com.magento.idea.magento2plugin.xml.di.reference;

import com.intellij.patterns.XmlPatterns;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.ProcessingContext;
import com.magento.idea.magento2plugin.xml.di.XmlHelper;
import com.magento.idea.magento2plugin.xml.di.reference.provider.ArgumentNameReferenceProvider;
import com.magento.idea.magento2plugin.xml.di.reference.provider.DiInstanceReferenceProvider;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Warider on 17.08.2015.
 */
public class XmlReferenceContributor extends PsiReferenceContributor {
    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar psiReferenceRegistrar) {
        // <preference for="\SomeInterface" />
        psiReferenceRegistrar.registerReferenceProvider(
            XmlHelper.getDiPreferenceForPattern(),
            new DiInstanceReferenceProvider(
                new TypeReference.ReferenceType[]{
                    TypeReference.ReferenceType.INTERFACE,
                    TypeReference.ReferenceType.CLASS
                }
            )
        );

        // <preference type="SomeClassOrVirtualType" />
        psiReferenceRegistrar.registerReferenceProvider(
            XmlHelper.getDiPreferenceTypePattern(),
            new DiInstanceReferenceProvider(
                new TypeReference.ReferenceType[]{
                    TypeReference.ReferenceType.CLASS,
                    TypeReference.ReferenceType.VIRTUAL_TYPE
                }
            )
        );

        // <type name="\SomeClass"></type>
        psiReferenceRegistrar.registerReferenceProvider(
            XmlHelper.getDiTypePattern(),
            new DiInstanceReferenceProvider(
                new TypeReference.ReferenceType[]{
                    TypeReference.ReferenceType.CLASS
                }
            )
        );

        // <virtualType type="SomeClassOrVirtualType"></virtualType>
        psiReferenceRegistrar.registerReferenceProvider(
            XmlHelper.getDiVirtualTypePattern(),
            new DiInstanceReferenceProvider(
                new TypeReference.ReferenceType[]{
                    TypeReference.ReferenceType.CLASS,
                    TypeReference.ReferenceType.VIRTUAL_TYPE
                }
            )
        );

        // <argument|item xsi:type="object">SomeClassOrVirtualType</argument|item>
        psiReferenceRegistrar.registerReferenceProvider(
            XmlPatterns.or(
                XmlHelper.getArgumentObjectPattern(),
                XmlHelper.getItemObjectPattern()
            ),
            new DiInstanceReferenceProvider(
                new TypeReference.ReferenceType[]{
                    TypeReference.ReferenceType.CLASS,
                    TypeReference.ReferenceType.VIRTUAL_TYPE
                }
            )
        );

        psiReferenceRegistrar.registerReferenceProvider(
            XmlHelper.getArgumentNamePattern(),
            new ArgumentNameReferenceProvider()
        );
    }
}
