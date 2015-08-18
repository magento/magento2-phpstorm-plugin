package com.magento.idea.magento2plugin.xml.reference;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.PsiPolyVariantReferenceBase;
import com.intellij.psi.ResolveResult;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.PhpLangUtil;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Warider on 17.08.2015.
 */
public class TypeXmlReference extends PsiPolyVariantReferenceBase<PsiElement> {
    private static List<ResolveResultsFiller> resolveResultsFillers = new ArrayList<ResolveResultsFiller>();

    static {
        resolveResultsFillers.add(ClassesResultsFiller.INSTANCE);
    }

    public TypeXmlReference(@NotNull PsiElement element) {
        super(element);
    }

    @NotNull
    @Override
    public ResolveResult[] multiResolve(boolean incompleteCode) {
        List<ResolveResult> resolveResults = new ArrayList<>();

        for (ResolveResultsFiller filler: resolveResultsFillers) {
            filler.fillResults(getElement(), resolveResults);
        }

        return resolveResults.toArray(new ResolveResult[resolveResults.size()]);
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        return new Object[0];
    }
}

interface ResolveResultsFiller {
    public void fillResults(PsiElement psiElement, List<ResolveResult> results);
}

class ClassesResultsFiller implements ResolveResultsFiller {
    public static final ResolveResultsFiller INSTANCE = new ClassesResultsFiller();

    private ClassesResultsFiller() {}

    @Override
    public void fillResults(PsiElement psiElement, List<ResolveResult> results) {
        PhpIndex phpIndex = PhpIndex.getInstance(psiElement.getProject());
        String typeName = StringUtil.unquoteString(psiElement.getText());

        for (PhpClass phpClass : phpIndex.getClassesByFQN(PhpLangUtil.toFQN(typeName))) {
            results.add(new PsiElementResolveResult(phpClass));
        }
    }
}