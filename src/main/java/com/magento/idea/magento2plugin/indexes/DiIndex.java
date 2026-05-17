/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.indexes;

import com.intellij.codeInsight.completion.PrefixMatcher;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
// CHECKSTYLE IGNORE check FOR NEXT 1 LINES
import com.intellij.psi.xml.*;//NOPMD
import com.intellij.util.indexing.FileBasedIndex;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.stubs.indexes.VirtualTypeIndex;
import com.magento.idea.magento2plugin.util.xml.XmlPsiTreeUtil;
// CHECKSTYLE IGNORE check FOR NEXT 1 LINES
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * TODO: enable style checks after decomposition.
 */
@SuppressWarnings({"PMD", "checkstyle:all"})
public class DiIndex {

    private static DiIndex INSTANCE;

    private Project project;

    private DiIndex() {
    }

    /**
     * Get Instance.
     *
     * @param project
     * @return DiIndex
     */
    public static DiIndex getInstance(final Project project) {
        if (null == INSTANCE) {
            INSTANCE = new DiIndex();
        }
        INSTANCE.project = project;
        return INSTANCE;
    }

    /**
     * Get Php Class Of Argument.
     *
     * @param psiArgumentValueElement
     * @return PhpClass
     */
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

    /**
     * Get Php Class Of Service Method.
     *
     * @param psiMethodValueElement
     * @return PhpClass
     */
    @Nullable
    public static PhpClass getPhpClassOfServiceMethod(XmlElement psiMethodValueElement) {
        return getPhpClassFromParentTagAttribute(psiMethodValueElement, "class");
    }

    /**
     * Get Php Class Of Job Method.
     *
     * @param psiMethodValueElement
     * @return PhpClass
     */
    @Nullable
    public static PhpClass getPhpClassOfJobMethod(XmlElement psiMethodValueElement) {
        return getPhpClassFromParentTagAttribute(psiMethodValueElement, "instance");
    }

    @Nullable
    private static PhpClass getPhpClassFromParentTagAttribute(
            @NotNull XmlElement psiElement,
            @NotNull String attributeName
    ) {
        XmlTag serviceTag = PsiTreeUtil.getParentOfType(psiElement, XmlTag.class);
        if (serviceTag == null) {
            return null;
        }

        XmlAttribute attribute = serviceTag.getAttribute(attributeName);
        if (attribute == null) {
            return null;
        }

        String className = attribute.getValue();
        if (className == null || className.isEmpty()) {
            return null;
        }

        try {
            Collection<PhpClass> phpClasses = PhpIndex
                    .getInstance(psiElement.getProject())
                    .getAnyByFQN(className);

            if (!phpClasses.isEmpty()) {
                return phpClasses.iterator().next();
            }
        } catch (ProcessCanceledException exception) {
            throw exception;
        } catch (Exception exception) {
            return null;
        }

        return null;
    }

    /**
     * Get Virtual Type Elements.
     *
     * @param name
     * @param scope
     * @return Collection<PsiElement>
     */
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

    /**
     * Get All Virtual Type Element Names.
     *
     * @param prefixMatcher
     * @param scope
     * @return Collection<String>
     */
    public Collection<String> getAllVirtualTypeElementNames(PrefixMatcher prefixMatcher, final GlobalSearchScope scope) {
        Collection<String> keys =
                FileBasedIndex.getInstance().getAllKeys(VirtualTypeIndex.KEY, project);

        keys.removeIf(k -> !prefixMatcher.prefixMatches(k));
        return keys;
    }

    /**
     * Get Top Type Of Virtual Type.
     *
     * @param name
     * @return String
     */
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
