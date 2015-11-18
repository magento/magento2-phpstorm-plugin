package com.magento.idea.magento2plugin.xml.layout.reference.fill;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.indexing.ID;
import com.magento.idea.magento2plugin.xml.layout.index.AbstractComponentNameFileBasedIndex;
import com.magento.idea.magento2plugin.xml.reference.util.ReferenceResultsFiller;

import java.util.Arrays;
import java.util.List;

/**
 * Created by dkvashnin on 11/18/15.
 */
public class ComponentResultsFiller implements ReferenceResultsFiller {
    private ID<String, Void> indexId;
    private String componentType;

    public ComponentResultsFiller(ID<String, Void> indexId, String componentType) {
        this.indexId = indexId;
        this.componentType = componentType;
    }

    @Override
    public void fillResolveResults(PsiElement psiElement, List<ResolveResult> results, String typeName) {
        List<XmlTag> componentDeclarations = AbstractComponentNameFileBasedIndex
            .getComponentDeclarations(
                typeName,
                componentType,
                indexId,
                psiElement.getProject()
            );

        ResolveResult[] resolveResults = PsiElementResolveResult.createResults(componentDeclarations);
        results.addAll(Arrays.asList(resolveResults));
    }

    public void getVariants(PsiElement psiElement, List<Object> results) {
        results.addAll(AbstractComponentNameFileBasedIndex.getAllKeys(indexId, psiElement.getProject()));
    }
}
