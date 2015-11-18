package com.magento.idea.magento2plugin.xml.reference.util;

import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveResult;

import java.util.List;

/**
 * Created by dkvashnin on 11/3/15.
 */
public interface ReferenceResultsFiller {
    public void fillResolveResults(PsiElement psiElement, List<ResolveResult> results, String typeName);
}
