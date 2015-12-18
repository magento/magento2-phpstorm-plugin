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
public class ClassesResultsFiller implements ReferenceResultsFiller {
    public static final ReferenceResultsFiller INSTANCE = new ClassesResultsFiller();

    private ClassesResultsFiller() {}

    @Override
    public void fillResolveResults(PsiElement psiElement, List<ResolveResult> results, String typeName) {
        PhpIndex phpIndex = PhpIndex.getInstance(psiElement.getProject());

        for (PhpClass phpClass : phpIndex.getClassesByFQN(PhpLangUtil.toFQN(typeName))) {
            addResult(results, phpClass);
        }
    }

    protected void addResult(List<ResolveResult> results, PhpClass phpClass) {
        results.add(new PsiElementResolveResult(phpClass));
    }
}
