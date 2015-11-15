package com.magento.idea.magento2plugin.xml.di.reference;

import com.intellij.patterns.XmlPatterns;
import com.intellij.psi.*;
import com.intellij.util.ProcessingContext;
import com.magento.idea.magento2plugin.xml.di.XmlHelper;
import com.magento.idea.magento2plugin.xml.di.reference.provider.ArgumentNameReferenceProvider;
import com.magento.idea.magento2plugin.xml.di.reference.provider.DiInstanceReferenceProvider;
import com.magento.idea.magento2plugin.xml.reference.TypeReference;
import com.magento.idea.magento2plugin.xml.reference.util.ClassesResultsFiller;
import com.magento.idea.magento2plugin.xml.reference.util.InterfacesResultsFiller;
import com.magento.idea.magento2plugin.xml.reference.util.ResolveResultsFiller;
import com.magento.idea.magento2plugin.xml.reference.util.VirtualTypesResultsFiller;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Warider on 17.08.2015.
 */
public class DiReferenceContributor extends PsiReferenceContributor {
    private static final String OBJECT_TYPE_NAME = "object";
    private static final String CONST_TYPE_NAME = "const";
    private static final String INIT_TYPE_NAME = "init_parameter";

    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar psiReferenceRegistrar) {
        // <preference for="\SomeInterface" />
        psiReferenceRegistrar.registerReferenceProvider(
            XmlHelper.getDiPreferenceForPattern(),
            new DiInstanceReferenceProvider(
                new ResolveResultsFiller[]{
                    ClassesResultsFiller.INSTANCE,
                    InterfacesResultsFiller.INSTANCE
                }
            )
        );

        // <preference type="SomeClassOrVirtualType" />
        psiReferenceRegistrar.registerReferenceProvider(
            XmlHelper.getDiPreferenceTypePattern(),
            new DiInstanceReferenceProvider(
                new ResolveResultsFiller[]{
                    ClassesResultsFiller.INSTANCE,
                    VirtualTypesResultsFiller.INSTANCE
                }
            )
        );

        // <type name="\SomeClass"></type>
        psiReferenceRegistrar.registerReferenceProvider(
            XmlHelper.getDiTypePattern(),
            new DiInstanceReferenceProvider(
                new ResolveResultsFiller[]{
                    ClassesResultsFiller.INSTANCE,
                    InterfacesResultsFiller.INSTANCE,
                    VirtualTypesResultsFiller.INSTANCE
                }
            )
        );

        // <virtualType type="SomeClassOrVirtualType"></virtualType>
        psiReferenceRegistrar.registerReferenceProvider(
            XmlHelper.getDiVirtualTypePattern(),
            new DiInstanceReferenceProvider(
                new ResolveResultsFiller[]{
                    ClassesResultsFiller.INSTANCE,
                    VirtualTypesResultsFiller.INSTANCE
                }
            )
        );

        // <argument|item xsi:type="object">SomeClassOrVirtualType</argument|item>
        psiReferenceRegistrar.registerReferenceProvider(
            XmlPatterns.or(
                XmlHelper.getArgumentValuePatternForType(OBJECT_TYPE_NAME),
                XmlHelper.getItemValuePatternForType(OBJECT_TYPE_NAME)
            ),
            new DiInstanceReferenceProvider(
                new ResolveResultsFiller[]{
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
                XmlHelper.getArgumentValuePatternForType(CONST_TYPE_NAME),
                XmlHelper.getArgumentValuePatternForType(INIT_TYPE_NAME),
                XmlHelper.getItemValuePatternForType(CONST_TYPE_NAME),
                XmlHelper.getItemValuePatternForType(INIT_TYPE_NAME)
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
            new DiInstanceReferenceProvider(
                new ResolveResultsFiller[]{
                    ClassesResultsFiller.INSTANCE,
                    VirtualTypesResultsFiller.INSTANCE
                }
            )
        );
    }
}
