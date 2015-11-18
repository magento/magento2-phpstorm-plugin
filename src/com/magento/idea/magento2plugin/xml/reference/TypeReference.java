package com.magento.idea.magento2plugin.xml.reference;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiPolyVariantReferenceBase;
import com.intellij.psi.ResolveResult;
import com.magento.idea.magento2plugin.xml.reference.util.ReferenceResultsFiller;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Warider on 17.08.2015.
 */
public class TypeReference extends PsiPolyVariantReferenceBase<PsiElement> {
    private final boolean greedy;

    private ReferenceResultsFiller[] resultsFillers;

    public TypeReference(@NotNull PsiElement element, ReferenceResultsFiller[] resultsFillers) {
        this(element, resultsFillers, false);
    }

    public TypeReference(@NotNull PsiElement element, ReferenceResultsFiller[] resultsFillers, boolean greedy) {
        super(element);

        this.resultsFillers = resultsFillers;
        this.greedy = greedy;
    }

    @NotNull
    @Override
    public ResolveResult[] multiResolve(boolean incompleteCode) {
        List<ResolveResult> resolveResults = new ArrayList<>();
        String typeName = StringUtil.unquoteString(getElement().getText());

        for (ReferenceResultsFiller referenceResultsFiller : resultsFillers) {
            referenceResultsFiller.fillResolveResults(getElement(), resolveResults, typeName);
            if (!greedy && resolveResults.size() > 0) {
                break;
            }
        }

        return resolveResults.toArray(new ResolveResult[resolveResults.size()]);
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        return new Object[0];
    }
}

