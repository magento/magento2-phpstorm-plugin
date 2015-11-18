package com.magento.idea.magento2plugin.xml.reference.util;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.xml.XmlAttributeValue;
import com.magento.idea.magento2plugin.xml.di.index.VirtualTypesNamesFileBasedIndex;

import java.util.List;

/**
 * Created by dkvashnin on 11/3/15.
 */
public class VirtualTypesResultsFiller implements ReferenceResultsFiller {
    public static final ReferenceResultsFiller INSTANCE = new VirtualTypesResultsFiller();

    private VirtualTypesResultsFiller() {}

    @Override
    public void fillResolveResults(PsiElement psiElement, List<ResolveResult> results, String typeName) {
        XmlAttributeValue[] virtualTypesByName = VirtualTypesNamesFileBasedIndex.getVirtualTypesByName(
            psiElement.getProject(),
            typeName,
            psiElement.getResolveScope()
        );

        for (XmlAttributeValue virtualType: virtualTypesByName) {
            results.add(new PsiElementResolveResult(virtualType));
        }
    }
}
