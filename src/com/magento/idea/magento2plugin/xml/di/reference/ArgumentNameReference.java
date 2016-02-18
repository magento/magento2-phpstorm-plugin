package com.magento.idea.magento2plugin.xml.di.reference;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.PsiPolyVariantReferenceBase;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.xml.XmlTag;
import com.jetbrains.php.PhpIcons;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.Parameter;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.xml.di.XmlHelper;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by dkvashnin on 10/18/15.
 */
public class ArgumentNameReference extends PsiPolyVariantReferenceBase<PsiElement> {
    private XmlTag typeTag;
    private String typeName;

    public ArgumentNameReference(@NotNull PsiElement psiElement, @NotNull XmlTag typeTag, @NotNull String typeName) {
        super(psiElement);

        this.typeTag = typeTag;
        this.typeName = typeName;
    }

    @NotNull
    @Override
    public ResolveResult[] multiResolve(boolean b) {
        List<ResolveResult> list = new ArrayList<ResolveResult>();

        String argumentName = StringUtil.unquoteString(getElement().getText());
        for (Parameter parameter: getParameters()) {
            if (argumentName.equals(parameter.getName())) {
                list.add(new PsiElementResolveResult(parameter));
            }
        }

        return list.toArray(new ResolveResult[list.size()]);
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        List<LookupElement> variants = new ArrayList<LookupElement>();
        List<String> definedArguments = getAlreadyConfiguredArguments();

        for (Parameter parameter: getParameters()) {
            String argumentName = parameter.getName();
            if (definedArguments.contains(argumentName)) {
                continue;
            }

            variants.add(
                LookupElementBuilder
                    .create(argumentName)
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

    private List<String> getAlreadyConfiguredArguments()
    {
        List<String> result = new ArrayList<>();

        XmlTag argumentsTag = typeTag.findFirstSubTag(XmlHelper.ARGUMENTS_TAG);
        if (argumentsTag == null) {
            return result;
        }

        for (XmlTag argumentTag: argumentsTag.findSubTags(XmlHelper.ARGUMENT_TAG)) {
            result.add(
                argumentTag.getAttributeValue(XmlHelper.NAME_ATTRIBUTE)
            );
        }

        return result;
    }
}
