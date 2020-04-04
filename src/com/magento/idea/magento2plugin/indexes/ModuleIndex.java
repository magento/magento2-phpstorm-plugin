/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.indexes;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.FileBasedIndex;
import com.jetbrains.php.lang.PhpFileType;
import com.magento.idea.magento2plugin.magento.packages.Package;
import com.magento.idea.magento2plugin.stubs.indexes.ModuleNameIndex;
import com.magento.idea.magento2plugin.util.RegExUtil;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ModuleIndex {

    private static ModuleIndex INSTANCE;

    private Project project;

    private ModuleIndex() {
    }

    public static ModuleIndex getInstance(Project project) {
        if (null == INSTANCE) {
            INSTANCE = new ModuleIndex();
        }
        INSTANCE.project = project;

        return INSTANCE;
    }

    public List<String> getEditableModuleNames() {
        FileBasedIndex index = FileBasedIndex
                .getInstance();
        List<String> allModulesList = new ArrayList<>();
        Collection<String> allModules = index.getAllKeys(ModuleNameIndex.KEY, project);
        for (String moduleName : allModules) {
            if (!moduleName.matches(RegExUtil.Magento.MODULE_NAME)) {
                continue;
            }
            Collection<VirtualFile> files = index.getContainingFiles(ModuleNameIndex.KEY, moduleName, GlobalSearchScope.getScopeRestrictedByFileTypes(
                    GlobalSearchScope.allScope(project),
                    PhpFileType.INSTANCE
            ));
            if (files.isEmpty()) {
                continue;
            }
            VirtualFile virtualFile = files.iterator().next();
            if (virtualFile.getPath().contains(Package.VENDOR)) {
                continue;
            }

            allModulesList.add(moduleName);
        }
        Collections.sort(allModulesList);
        return allModulesList;
    }

    public PsiDirectory getModuleDirectoryByModuleName(String moduleName) {
        FileBasedIndex index = FileBasedIndex
                .getInstance();
        Collection<VirtualFile> files = index.getContainingFiles(ModuleNameIndex.KEY, moduleName, GlobalSearchScope.getScopeRestrictedByFileTypes(
                GlobalSearchScope.allScope(project),
                PhpFileType.INSTANCE
        ));
        VirtualFile virtualFile = files.iterator().next();

        return PsiManager.getInstance(project).findDirectory(virtualFile.getParent());
    }
}
