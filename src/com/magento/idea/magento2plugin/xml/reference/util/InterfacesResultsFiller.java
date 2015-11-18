package com.magento.idea.magento2plugin.xml.reference.util;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.ResolveResult;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.PhpLangUtil;
import com.jetbrains.php.lang.psi.elements.PhpClass;

import java.util.List;

/**
 * Created by dkvashnin on 11/3/15.
 */
public class InterfacesResultsFiller implements ReferenceResultsFiller {
    public static final ReferenceResultsFiller INSTANCE = new InterfacesResultsFiller();

    private InterfacesResultsFiller() {}

    @Override
    public void fillResolveResults(PsiElement psiElement, List<ResolveResult> results, String typeName) {
        PhpIndex phpIndex = PhpIndex.getInstance(psiElement.getProject());

        for (PhpClass phpClass : phpIndex.getInterfacesByFQN(PhpLangUtil.toFQN(typeName))) {
            results.add(new PsiElementResolveResult(phpClass));
        }
    }
}
