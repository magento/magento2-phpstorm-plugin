package com.magento.idea.magento2plugin.xml.di.reference;

import com.intellij.patterns.XmlPatterns;
import com.intellij.psi.*;
import com.intellij.util.ProcessingContext;
import com.magento.idea.magento2plugin.xml.di.XmlHelper;
import com.magento.idea.magento2plugin.xml.di.reference.provider.ArgumentNameReferenceProvider;
import com.magento.idea.magento2plugin.xml.di.reference.provider.XmlReferenceProvider;
import com.magento.idea.magento2plugin.xml.reference.util.ClassesResultsFiller;
import com.magento.idea.magento2plugin.xml.reference.util.InterfacesResultsFiller;
import com.magento.idea.magento2plugin.xml.reference.util.ReferenceResultsFiller;
import com.magento.idea.magento2plugin.xml.reference.util.VirtualTypesResultsFiller;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Warider on 17.08.2015.
 */
public class DiReferenceContributor extends PsiReferenceContributor {
    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar psiReferenceRegistrar) {
        // <preference for="\SomeInterface" />
        psiReferenceRegistrar.registerReferenceProvider(
            XmlHelper.getDiPreferenceForPattern(),
            new XmlReferenceProvider(
                new ReferenceResultsFiller[]{
                    ClassesResultsFiller.INSTANCE,
                    InterfacesResultsFiller.INSTANCE
                }
            )
        );

        // <preference type="SomeClassOrVirtualType" />
        psiReferenceRegistrar.registerReferenceProvider(
            XmlHelper.getDiPreferenceTypePattern(),
            new XmlReferenceProvider(
                new ReferenceResultsFiller[]{
                    ClassesResultsFiller.INSTANCE,
                    VirtualTypesResultsFiller.INSTANCE
                }
            )
        );

        // <type name="\SomeClass"></type>
        psiReferenceRegistrar.registerReferenceProvider(
            XmlHelper.getDiTypePattern(),
            new XmlReferenceProvider(
                new ReferenceResultsFiller[]{
                    ClassesResultsFiller.INSTANCE,
                    InterfacesResultsFiller.INSTANCE,
                    VirtualTypesResultsFiller.INSTANCE
                }
            )
        );

        // <virtualType type="SomeClassOrVirtualType"></virtualType>
        psiReferenceRegistrar.registerReferenceProvider(
            XmlHelper.getDiVirtualTypePattern(),
            new XmlReferenceProvider(
                new ReferenceResultsFiller[]{
                    ClassesResultsFiller.INSTANCE,
                    VirtualTypesResultsFiller.INSTANCE
                }
            )
        );

        // <argument|item xsi:type="object">SomeClassOrVirtualType</argument|item>
        // <item xsi:type="string">SomeClassOrVirtualType</item> - lazy initialization
        psiReferenceRegistrar.registerReferenceProvider(
            XmlPatterns.or(
                XmlHelper.getArgumentValuePatternForType(XmlHelper.OBJECT_TYPE),
                XmlHelper.getItemValuePatternForType(XmlHelper.OBJECT_TYPE),
                XmlHelper.getItemValuePatternForType(XmlHelper.STRING_TYPE)
            ),
            new XmlReferenceProvider(
                new ReferenceResultsFiller[]{
                    ClassesResultsFiller.INSTANCE,
                    VirtualTypesResultsFiller.INSTANCE
                }
            )
        );

        // <argument name="typeName"></argument>
        psiReferenceRegistrar.registerReferenceProvider(
            XmlHelper.getArgumentNamePattern(),
            new ArgumentNameReferenceProvider()
        );

        // <argument|item xsi:type="init_parameter|const">SomeClass::SOME_CONSTANT</argument|item>
        psiReferenceRegistrar.registerReferenceProvider(
            XmlPatterns.or(
                XmlHelper.getArgumentValuePatternForType(XmlHelper.CONST_TYPE),
                XmlHelper.getArgumentValuePatternForType(XmlHelper.INIT_TYPE),
                XmlHelper.getItemValuePatternForType(XmlHelper.CONST_TYPE),
                XmlHelper.getItemValuePatternForType(XmlHelper.INIT_TYPE)
            ),
            new PsiReferenceProvider() {
                @NotNull
                @Override
                public PsiReference[] getReferencesByElement(@NotNull PsiElement psiElement, @NotNull ProcessingContext processingContext) {
                    return new PsiReference[] {new ConstantReference(psiElement)};
                }
            }
        );

        // <plugin type="SomePlugin" />
        psiReferenceRegistrar.registerReferenceProvider(
            XmlHelper.getPluginTypePattern(),
            new XmlReferenceProvider(
                new ReferenceResultsFiller[]{
                    ClassesResultsFiller.INSTANCE,
                    VirtualTypesResultsFiller.INSTANCE
                }
            )
        );
    }
}
