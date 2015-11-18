package com.magento.idea.magento2plugin.xml.layout.reference;

import com.intellij.psi.*;
import com.magento.idea.magento2plugin.xml.XmlHelperUtility;
import com.magento.idea.magento2plugin.xml.di.reference.provider.DiInstanceReferenceProvider;
import com.magento.idea.magento2plugin.xml.layout.index.BlockFileBasedIndex;
import com.magento.idea.magento2plugin.xml.layout.index.ContainerFileBasedIndex;
import com.magento.idea.magento2plugin.xml.layout.reference.fill.ComponentResultsFiller;
import com.magento.idea.magento2plugin.xml.reference.util.ClassesResultsFiller;
import com.magento.idea.magento2plugin.xml.reference.util.ResolveResultsFiller;
import com.magento.idea.magento2plugin.xml.reference.util.VirtualTypesResultsFiller;
import org.jetbrains.annotations.NotNull;

/**
 * Created by dkvashnin on 11/18/15.
 */
public class LayoutReferenceContributor extends PsiReferenceContributor {
    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar psiReferenceRegistrar) {
        // <block class="\BlockClass" />
        psiReferenceRegistrar.registerReferenceProvider(
            XmlHelperUtility.getTagAttributeValuePattern("block", "class"),
            new DiInstanceReferenceProvider(
                new ResolveResultsFiller[]{
                    ClassesResultsFiller.INSTANCE,
                    VirtualTypesResultsFiller.INSTANCE
                }
            )
        );

        psiReferenceRegistrar.registerReferenceProvider(
            XmlHelperUtility.getTagAttributeValuePattern("referenceBlock", "name"),
            new DiInstanceReferenceProvider(
                new ResolveResultsFiller[]{
                    new ComponentResultsFiller(BlockFileBasedIndex.NAME, "block")
                }
            )
        );

        psiReferenceRegistrar.registerReferenceProvider(
            XmlHelperUtility.getTagAttributeValuePattern("referenceContainer", "name"),
            new DiInstanceReferenceProvider(
                new ResolveResultsFiller[]{
                    new ComponentResultsFiller(ContainerFileBasedIndex.NAME, "container")
                }
            )
        );
    }
}
