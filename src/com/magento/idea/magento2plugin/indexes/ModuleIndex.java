/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.indexes;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.FileBasedIndex;
import com.jetbrains.php.lang.PhpFileType;
import com.magento.idea.magento2plugin.magento.packages.Package;
import com.magento.idea.magento2plugin.project.util.GetProjectBasePath;
import com.magento.idea.magento2plugin.stubs.indexes.ModuleNameIndex;
import com.magento.idea.magento2plugin.util.RegExUtil;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        return getModuleNames(Package.vendor, true);
    }

    public List<String> getModuleNames() {
        return getModuleNames("/tests/|/test/", false);
    }

    public List<String> getModuleNames(String filterPattern, boolean withinProject) {
        FileBasedIndex index = FileBasedIndex
                .getInstance();
        List<String> allModulesList = new ArrayList<>();
        Collection<String> allModules = index.getAllKeys(ModuleNameIndex.KEY, project);
        Pattern p = Pattern.compile(filterPattern);
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
            if (withinProject) {
                if (!VfsUtilCore.isAncestor(GetProjectBasePath.execute(project), virtualFile, false)) {
                    continue;
                }
            }
            Matcher m = p.matcher(virtualFile.getPath());
            if (m.find()) {
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
