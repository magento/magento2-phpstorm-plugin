/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.indexes;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.indexing.FileBasedIndex;
import com.magento.idea.magento2plugin.magento.files.ModuleDiXml;
import com.magento.idea.magento2plugin.util.xml.XmlPsiTreeUtil;
import java.util.ArrayList;
import java.util.Collection;

public final class PluginIndex {

    private static PluginIndex INSTANCE; //NOPMD

    private Project project;

    private PluginIndex() {
    }

    /**
     * Getter fo index instance.
     *
     * @param project Project
     * @return PluginIndex
     */
    public static PluginIndex getInstance(final Project project) {
        if (null == INSTANCE) { //NOPMD
            INSTANCE = new PluginIndex();
        }
        INSTANCE.project = project;

        return INSTANCE;
    }

    /**
     * Getter fo plugin elements.
     *
     * @param name String
     * @param scope GlobalSearchScope
     * @return Collection
     */
    public Collection<PsiElement> getPluginElements(
            final String name,
            final GlobalSearchScope scope
    ) {
        final Collection<PsiElement> result = new ArrayList<>();

        final Collection<VirtualFile> virtualFiles =
                FileBasedIndex.getInstance().getContainingFiles(
                        com.magento.idea.magento2plugin.stubs.indexes.PluginIndex.KEY,
                        name,
                        scope
                );

        for (final VirtualFile virtualFile : virtualFiles) {
            final XmlFile xmlFile = (XmlFile) PsiManager.getInstance(project).findFile(virtualFile);
            final Collection<XmlAttributeValue> valueElements = XmlPsiTreeUtil
                    .findAttributeValueElements(
                            xmlFile,
                            ModuleDiXml.TYPE_ATTR,
                            ModuleDiXml.NAME_ATTR,
                            name
                    );
            result.addAll(valueElements);
        }
        return result;
    }
}
