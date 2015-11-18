package com.magento.idea.magento2plugin.xml.observer.reference.util;

import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.FileBasedIndex;
import com.magento.idea.magento2plugin.xml.observer.index.EventObserverFileBasedIndex;
import com.magento.idea.magento2plugin.xml.reference.util.ClassesResultsFiller;
import com.magento.idea.magento2plugin.xml.reference.util.ReferenceResultsFiller;
import com.magento.idea.magento2plugin.xml.reference.util.VirtualTypesResultsFiller;

import java.util.List;
import java.util.Set;

/**
 * Created by dkvashnin on 11/3/15.
 */
public class ClassResultsFillerWrapper implements ReferenceResultsFiller {
    public static final ReferenceResultsFiller INSTANCE = new ClassResultsFillerWrapper();

    @Override
    public void fillResolveResults(PsiElement psiElement, List<ResolveResult> results, String typeName) {
        List<Set<String>> observersTypesList = FileBasedIndex.getInstance().getValues(
            EventObserverFileBasedIndex.NAME,
            typeName,
            GlobalSearchScope.allScope(psiElement.getProject())
        );

        for (Set<String> observerTypes: observersTypesList) {
            for (String type: observerTypes) {
                ClassesResultsFiller.INSTANCE.fillResolveResults(psiElement, results, type);
                VirtualTypesResultsFiller.INSTANCE.fillResolveResults(psiElement, results, type);
            }
        }
    }
}
