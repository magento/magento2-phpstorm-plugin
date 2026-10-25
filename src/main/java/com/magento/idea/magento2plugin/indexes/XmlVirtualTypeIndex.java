/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.indexes;

import com.intellij.codeInsight.completion.PrefixMatcher;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.indexing.FileBasedIndex;
import com.magento.idea.magento2plugin.stubs.indexes.VirtualTypeIndex;
import com.magento.idea.magento2plugin.util.xml.XmlPsiTreeUtil;
import java.util.ArrayList;
import java.util.Collection;

/** XML virtual-type declarations, independent of PHP class resolution. */
public final class XmlVirtualTypeIndex {

    private final Project project;

    /** Create a lookup bound to one project. */
    public XmlVirtualTypeIndex(final Project project) {
        this.project = project;
    }

    /** Find matching XML name attributes within the requested scope. */
    public Collection<PsiElement> getVirtualTypeElements(
            final String name,
            final GlobalSearchScope scope
    ) {
        final Collection<PsiElement> result = new ArrayList<>();
        final Collection<VirtualFile> files = FileBasedIndex.getInstance()
                .getContainingFiles(VirtualTypeIndex.KEY, name, scope);

        for (final VirtualFile file : files) {
            final PsiFile psiFile = PsiManager.getInstance(project).findFile(file);

            if (psiFile instanceof XmlFile) {
                result.addAll(XmlPsiTreeUtil.findAttributeValueElements(
                        (XmlFile) psiFile, "virtualType", "name", name
                ));
            }
        }

        return result;
    }

    /** Find matching names with at least one declaration in the requested scope. */
    public Collection<String> getAllVirtualTypeElementNames(
            final PrefixMatcher prefixMatcher,
            final GlobalSearchScope scope
    ) {
        final FileBasedIndex index = FileBasedIndex.getInstance();
        final Collection<String> names = new ArrayList<>();

        for (final String name : index.getAllKeys(VirtualTypeIndex.KEY, project)) {
            if (prefixMatcher.prefixMatches(name)
                    && !index.getContainingFiles(VirtualTypeIndex.KEY, name, scope).isEmpty()) {
                names.add(name);
            }
        }

        return names;
    }
}
