package com.magento.idea.magento2plugin.xml.layout.reference.fill;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.xml.XmlTag;
import com.magento.idea.magento2plugin.xml.layout.index.util.LayoutIndexUtility;
import com.magento.idea.magento2plugin.xml.reference.util.ReferenceResultsFiller;

import java.util.Arrays;
import java.util.List;

/**
 * Created by dkvashnin on 11/20/15.
 */
public class BlockResultsFiller implements ReferenceResultsFiller {
    @Override
    public void fillResolveResults(PsiElement psiElement, List<ResolveResult> results, String typeName) {
        List<XmlTag> componentDeclarations = LayoutIndexUtility
            .getBlockDeclarations(
                typeName,
                psiElement.getProject()
            );

        ResolveResult[] resolveResults = PsiElementResolveResult.createResults(componentDeclarations);
        results.addAll(Arrays.asList(resolveResults));
    }
}