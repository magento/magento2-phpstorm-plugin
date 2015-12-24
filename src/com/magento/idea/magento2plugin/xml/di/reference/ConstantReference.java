package com.magento.idea.magento2plugin.xml.di.reference;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.PsiPolyVariantReferenceBase;
import com.intellij.psi.ResolveResult;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.PhpLangUtil;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by dkvashnin on 10/22/15.
 */
public class ConstantReference extends PsiPolyVariantReferenceBase<PsiElement> {
    public ConstantReference(PsiElement psiElement) {
        super(psiElement);
    }

    @NotNull
    @Override
    public ResolveResult[] multiResolve(boolean b) {
        PhpIndex phpIndex = PhpIndex.getInstance(getElement().getProject());
        List<String> parts = StringUtil.split(
            PhpLangUtil.toFQN(
                StringUtil.unquoteString(getElement().getText())
            ),
            "::"
        );

        if (parts.size() != 2) {
            return new ResolveResult[0];
        }

        String className = parts.get(0);
        String constantName = parts.get(1);

        List<ResolveResult> fields = new ArrayList<ResolveResult>();
        Collection<PhpClass> classesAndInterfaces = new ArrayList<>();
        classesAndInterfaces.addAll(phpIndex.getClassesByFQN(className));
        classesAndInterfaces.addAll(phpIndex.getInterfacesByFQN(className));

        for (PhpClass phpClass: classesAndInterfaces) {
            for(Field field: phpClass.getFields()) {
                if (field.isConstant() && field.getName().equals(constantName)) {
                    fields.add(new PsiElementResolveResult(field));
                }
            }
        }

        return fields.toArray(new ResolveResult[fields.size()]);
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        return new Object[0];
    }
}
