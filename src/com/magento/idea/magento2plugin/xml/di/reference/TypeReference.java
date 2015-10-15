package com.magento.idea.magento2plugin.xml.di.reference;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.PsiPolyVariantReferenceBase;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.xml.XmlAttributeValue;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.PhpLangUtil;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.xml.di.index.VirtualTypesNamesFileBasedIndex;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Warider on 17.08.2015.
 */
public class TypeReference extends PsiPolyVariantReferenceBase<PsiElement> {
    private static HashMap<ReferenceType, ResolveResultsFiller> resolveResultsFillers = new HashMap<ReferenceType, ResolveResultsFiller>();

    public static enum ReferenceType {
        CLASS, INTERFACE, VIRTUAL_TYPE
    }

    static {
        resolveResultsFillers.put(ReferenceType.CLASS, ClassesResultsFiller.INSTANCE);
        resolveResultsFillers.put(ReferenceType.INTERFACE, InterfacesResultsFiller.INSTANCE);
        resolveResultsFillers.put(ReferenceType.VIRTUAL_TYPE, VirtualTypesResultsFiller.INSTANCE);
    }

    private ReferenceType[] referenceTypes;

    public TypeReference(@NotNull PsiElement element, ReferenceType[] referenceTypes) {
        super(element);

        this.referenceTypes = referenceTypes;
    }

    @NotNull
    @Override
    public ResolveResult[] multiResolve(boolean incompleteCode) {
        List<ResolveResult> resolveResults = new ArrayList<>();

        for (ReferenceType referenceType: referenceTypes) {
            resolveResultsFillers.get(referenceType).fillResults(getElement(), resolveResults);
            if (resolveResults.size() > 0) {
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

class InterfacesResultsFiller implements ResolveResultsFiller {
    public static final ResolveResultsFiller INSTANCE = new InterfacesResultsFiller();

    private InterfacesResultsFiller() {}

    @Override
    public void fillResults(PsiElement psiElement, List<ResolveResult> results) {
        PhpIndex phpIndex = PhpIndex.getInstance(psiElement.getProject());
        String typeName = StringUtil.unquoteString(psiElement.getText());

        for (PhpClass phpClass : phpIndex.getInterfacesByFQN(PhpLangUtil.toFQN(typeName))) {
            results.add(new PsiElementResolveResult(phpClass));
        }
    }
}

class VirtualTypesResultsFiller implements ResolveResultsFiller {
    public static final ResolveResultsFiller INSTANCE = new VirtualTypesResultsFiller();

    private VirtualTypesResultsFiller() {}

    @Override
    public void fillResults(PsiElement psiElement, List<ResolveResult> results) {
        XmlAttributeValue[] virtualTypesByName = VirtualTypesNamesFileBasedIndex.getVirtualTypesByName(
            psiElement.getProject(),
            psiElement.getText(),
            psiElement.getResolveScope()
        );

        for (XmlAttributeValue virtualType: virtualTypesByName) {
            results.add(new PsiElementResolveResult(virtualType));
        }
    }
}