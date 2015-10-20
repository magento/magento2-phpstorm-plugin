package com.magento.idea.magento2plugin.xml.di.reference;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.PsiPolyVariantReferenceBase;
import com.intellij.psi.ResolveResult;
import com.jetbrains.php.PhpIcons;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.Parameter;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by dkvashnin on 10/18/15.
 */
public class ArgumentNameReference extends PsiPolyVariantReferenceBase<PsiElement> {
    private String typeName;

    public ArgumentNameReference(@NotNull PsiElement psiElement, @NotNull String typeName) {
        super(psiElement);

        this.typeName = typeName;
    }

    @NotNull
    @Override
    public ResolveResult[] multiResolve(boolean b) {
        List<ResolveResult> list = new ArrayList<ResolveResult>();

        String argumentName = StringUtil.unquoteString(getElement().getText());
        list.addAll(
            getParameters()
                .stream()
                .filter(parameter -> parameter.getName().equals(argumentName))
                .map(PsiElementResolveResult::new)
                .collect(Collectors.toList())
        );

        return list.toArray(new ResolveResult[list.size()]);
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        List<LookupElement> variants = new ArrayList<LookupElement>();
        for (Parameter parameter: getParameters()) {
            variants.add(
                LookupElementBuilder
                    .create(parameter.getName())
                    .withIcon(PhpIcons.PARAMETER)
                    .withTypeText(parameter.getDeclaredType().toStringResolved())
            );
        }

        return variants.toArray();
    }

    private List<Parameter> getParameters() {
        List<Parameter> parameterList = new ArrayList<Parameter>();

        PhpIndex phpIndex = PhpIndex.getInstance(getElement().getProject());
        Collection<PhpClass> classesByFQN = phpIndex.getClassesByFQN(typeName);

        for (PhpClass phpClass: classesByFQN) {
            Method constructor = phpClass.getConstructor();
            if (constructor == null) {
                continue;
            }

            Parameter[] parameters = constructor.getParameters();
            Collections.addAll(parameterList, parameters);
        }

        return parameterList;
    }
}
