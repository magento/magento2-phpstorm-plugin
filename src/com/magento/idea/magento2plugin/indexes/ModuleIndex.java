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

public final class ModuleIndex {

    private static ModuleIndex instance;

    private Project project;

    private ModuleIndex() {
    }

    /**
     * Constructor.
     *
     * @param project Project
     * @return ModuleIndex
     */
    public static ModuleIndex getInstance(final Project project) {
        if (null == instance) { //NOPMD
            instance = new ModuleIndex();
        }
        instance.project = project;

        return instance;
    }

    public List<String> getEditableModuleNames() {
        return getModuleNames(Package.vendor, true);
    }

    public List<String> getModuleNames() {
        return getModuleNames("/tests/|/test/", false);
    }

    /**
     * Returns Module Names.
     *
     * @param filterPattern String
     * @param withinProject boolean
     * @return List
     */
    public List<String> getModuleNames(final String filterPattern, final boolean withinProject) {
        final FileBasedIndex index = FileBasedIndex
                .getInstance();
        final List<String> allModulesList = new ArrayList<>();
        final Collection<String> allModules = index.getAllKeys(ModuleNameIndex.KEY, project);
        final Pattern pattern = Pattern.compile(filterPattern);
        for (final String moduleName : allModules) {
            if (!moduleName.matches(RegExUtil.Magento.MODULE_NAME)) {
                continue;
            }
            final Collection<VirtualFile> files = index.getContainingFiles(
                    ModuleNameIndex.KEY,
                    moduleName,
                    GlobalSearchScope.getScopeRestrictedByFileTypes(
                        GlobalSearchScope.allScope(project),
                        PhpFileType.INSTANCE
                ));
            if (files.isEmpty()) {
                continue;
            }
            final VirtualFile virtualFile = files.iterator().next();
            if (withinProject
                    && !VfsUtilCore.isAncestor(
                            GetProjectBasePath.execute(project), virtualFile, false
                )) {
                continue;
            }
            final Matcher matcher = pattern.matcher(virtualFile.getPath());
            if (matcher.find()) {
                continue;
            }
            allModulesList.add(moduleName);
        }
        Collections.sort(allModulesList);
        return allModulesList;
    }

    /**
     * Returns PSI directory of the certain module.
     *
     * @param moduleName String
     * @return PsiDirectory
     */
    public PsiDirectory getModuleDirectoryByModuleName(final String moduleName) {
        final FileBasedIndex index = FileBasedIndex
                .getInstance();
        final Collection<VirtualFile> files = index.getContainingFiles(
                ModuleNameIndex.KEY,
                moduleName,
                GlobalSearchScope.getScopeRestrictedByFileTypes(
                    GlobalSearchScope.allScope(project),
                    PhpFileType.INSTANCE
            ));
        final VirtualFile virtualFile = files.iterator().next();

        return PsiManager.getInstance(project).findDirectory(virtualFile.getParent());
    }
}
