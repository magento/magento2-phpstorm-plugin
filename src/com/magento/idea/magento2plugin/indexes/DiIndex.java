/**
 * Copyright © Dmytro Kvashnin. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.indexes;

import com.intellij.codeInsight.completion.PrefixMatcher;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.*;
import com.intellij.util.indexing.FileBasedIndex;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.stubs.indexes.VirtualTypeIndex;
import com.magento.idea.magento2plugin.xml.XmlPsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DiIndex {

    private static DiIndex INSTANCE;

    private Project project;

    private DiIndex() {
    }

    public static DiIndex getInstance(final Project project) {
        if (null == INSTANCE) {
            INSTANCE = new DiIndex();
        }
        INSTANCE.project = project;
        return INSTANCE;
    }

    @Nullable
    public PhpClass getPhpClassOfArgument(XmlElement psiArgumentValueElement) {

        XmlTag typeTag = XmlPsiTreeUtil.getTypeTagOfArgument(psiArgumentValueElement);
        if (null == typeTag) {
            return null;
        }

        String className = typeTag.getAttributeValue("name");
        if (null == className) {
            return null;
        }
        className = getTopTypeOfVirtualType(className);

        PhpIndex phpIndex = PhpIndex.getInstance(psiArgumentValueElement.getProject());
        Collection<PhpClass> phpClasses = phpIndex.getAnyByFQN(className);

        if (phpClasses.size() > 0) {
            return phpClasses.iterator().next();
        }

        return null;
    }

    @Nullable
    public static PhpClass getPhpClassOfServiceMethod(XmlElement psiMethodValueElement) {
        XmlTag serviceTag = PsiTreeUtil.getParentOfType(psiMethodValueElement, XmlTag.class);
        if (serviceTag == null) {
            return null;
        }

        XmlAttribute attribute = serviceTag.getAttribute("class");
        if (attribute == null) {
            return null;
        }

        XmlAttributeValue valueElement = attribute.getValueElement();
        if (valueElement == null) {
            return null;
        }

        for (PsiReference reference : valueElement.getReferences()) {
            if (reference != null) {
                PsiElement element = reference.resolve();
                if (element instanceof PhpClass) {
                    return (PhpClass) element;
                }
            }
        }

        return null;
    }

    public Collection<PsiElement> getVirtualTypeElements(final String name, final GlobalSearchScope scope) {
        Collection<PsiElement> result = new ArrayList<>();

        Collection<VirtualFile> virtualFiles =
                FileBasedIndex.getInstance().getContainingFiles(VirtualTypeIndex.KEY, name, scope);

        for (VirtualFile virtualFile : virtualFiles) {
            XmlFile xmlFile = (XmlFile) PsiManager.getInstance(project).findFile(virtualFile);
            if (xmlFile != null) {
                Collection<XmlAttributeValue> valueElements = XmlPsiTreeUtil
                        .findAttributeValueElements(xmlFile, "virtualType", "name", name);
                result.addAll(valueElements);
            }
        }
        return result;
    }

    public Collection<String> getAllVirtualTypeElementNames(PrefixMatcher prefixMatcher, final GlobalSearchScope scope) {
        Collection<String> keys =
                FileBasedIndex.getInstance().getAllKeys(VirtualTypeIndex.KEY, project);

        keys.removeIf(k -> !prefixMatcher.prefixMatches(k));
        return keys;
    }

    @NotNull
    private String getTopTypeOfVirtualType(@NotNull String name) {
        List<String> values;
        int parentNestingLevel = 0;
        int maxNestingLevel = 5;

        do {
            values = FileBasedIndex.getInstance()
                    .getValues(VirtualTypeIndex.KEY, name, GlobalSearchScope.allScope(project));
            if (values.size() > 0 && values.get(0) != null) {
                name = values.get(0);
            }
        } while (values.size() > 0 || maxNestingLevel > parentNestingLevel++);

        return name;
    }
}
