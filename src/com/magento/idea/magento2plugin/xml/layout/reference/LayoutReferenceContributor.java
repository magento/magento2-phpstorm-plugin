package com.magento.idea.magento2plugin.xml.layout.reference;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.patterns.XmlPatterns;
import com.intellij.psi.*;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.util.indexing.FileBasedIndex;
import com.magento.idea.magento2plugin.xml.XmlHelperUtility;
import com.magento.idea.magento2plugin.xml.di.reference.provider.XmlReferenceProvider;
import com.magento.idea.magento2plugin.xml.layout.LayoutUtility;
import com.magento.idea.magento2plugin.xml.layout.index.BlockFileBasedIndex;
import com.magento.idea.magento2plugin.xml.layout.index.ContainerFileBasedIndex;
import com.magento.idea.magento2plugin.xml.layout.reference.fill.BlockResultsFiller;
import com.magento.idea.magento2plugin.xml.layout.reference.fill.ContainerResultsFiller;
import com.magento.idea.magento2plugin.xml.reference.util.ClassesResultsFiller;
import com.magento.idea.magento2plugin.xml.reference.util.ReferenceResultsFiller;
import com.magento.idea.magento2plugin.xml.reference.util.VirtualTypesResultsFiller;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Created by dkvashnin on 11/18/15.
 */
public class LayoutReferenceContributor extends PsiReferenceContributor {
    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar psiReferenceRegistrar) {
        // <block class="\BlockClass" />
        psiReferenceRegistrar.registerReferenceProvider(
            XmlHelperUtility.getTagAttributeValuePattern("block", "class"),
            new XmlReferenceProvider(
                new ReferenceResultsFiller[]{
                    ClassesResultsFiller.INSTANCE,
                    VirtualTypesResultsFiller.INSTANCE
                }
            )
        );

        psiReferenceRegistrar.registerReferenceProvider(
            XmlPatterns.or(
                XmlHelperUtility.getTagAttributeValuePattern("referenceBlock", "name"),
                XmlHelperUtility.getTagAttributeValuePattern("block", "before"),
                XmlHelperUtility.getTagAttributeValuePattern("block", "after"),
                XmlHelperUtility.getTagAttributeValuePattern("remove", "name"),
                XmlHelperUtility.getTagAttributeValuePattern("move", "element"),
                XmlHelperUtility.getTagAttributeValuePattern("move", "destination"),
                XmlHelperUtility.getTagAttributeValuePattern("move", "before"),
                XmlHelperUtility.getTagAttributeValuePattern("move", "after")
            ),
            new XmlReferenceProvider(
                new ReferenceResultsFiller[]{
                    new BlockResultsFiller()
                }
            )
        );

        psiReferenceRegistrar.registerReferenceProvider(
            XmlHelperUtility.getTagAttributeValuePattern("referenceContainer", "name"),
            new XmlReferenceProvider(
                new ReferenceResultsFiller[]{
                    new ContainerResultsFiller()
                }
            )
        );

        psiReferenceRegistrar.registerReferenceProvider(
            XmlHelperUtility.getTagAttributeValuePattern("update", "handle"),
            new XmlReferenceProvider(
                new ReferenceResultsFiller[]{
                    (psiElement, results, typeName) -> results.addAll(
                        Arrays.asList(
                            PsiElementResolveResult.createResults(
                                LayoutUtility.getLayoutFiles(psiElement.getProject(), typeName)
                            )
                        )
                    )
                }
            )
        );
    }
}
