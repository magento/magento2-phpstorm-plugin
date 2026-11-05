/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.indexes;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.indexing.FileBasedIndex;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.stubs.indexes.xml.PhpClassNameIndex;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class XmlIndex {

    private XmlIndex() {
    }

    /**
     * Get PHP class declarations in the *.xml files.
     *
     * @param phpClass PhpClass
     *
     * @return List[XmlTag]
     */
    public static List<XmlTag> getPhpClassDeclarations(final PhpClass phpClass) {
        
        final List<XmlTag> result = new ArrayList<>();
        final String fqn = phpClass.getPresentableFQN();
        final PsiManager psiManager = PsiManager.getInstance(phpClass.getProject());
        final Collection<VirtualFile> vfs = FileBasedIndex.getInstance()
                .getContainingFiles(
                        PhpClassNameIndex.KEY,
                        fqn,
                        GlobalSearchScope.allScope(phpClass.getProject())
                );

        for (final VirtualFile vf : vfs) {
            final PsiFile psiFile = psiManager.findFile(vf);

            if (!(psiFile instanceof XmlFile)) {
                continue;
            }
            PhpClassNameIndex.forEachClassReference(
                    (XmlFile) psiFile,
                    (xmlTag, classNames) -> {
                        if (classNames.contains(fqn)) {
                            result.add(xmlTag);
                        }
                    }
            );
        }

        return result;
    }
}
