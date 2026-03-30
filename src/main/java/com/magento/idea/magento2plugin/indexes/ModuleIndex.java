/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.indexes;

import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.FileBasedIndex;
import com.jetbrains.php.lang.PhpFileType;
import com.magento.idea.magento2plugin.stubs.indexes.ModuleNameIndex;
import com.magento.idea.magento2plugin.util.RegExUtil;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import com.magento.idea.magento2plugin.util.magento.IsFileInEditableModuleUtil;
import org.jetbrains.annotations.Nullable;

public final class ModuleIndex {

    private final Project project;

    /**
     * Constructor.
     *
     * @param project Project
     */
    public ModuleIndex(final Project project) {
        this.project = project;
    }

    public List<String> getEditableModuleNames() {
        return getModuleNames(true);
    }

    public List<String> getEditableThemeNames() {
        return getThemeNames(true);
    }

    public List<String> getModuleNames() {
        return getModuleNames(false);
    }

    /**
     * Returns Module Names.
     *
     * @param withinProject boolean
     * @return List
     */
    public List<String> getModuleNames(final boolean withinProject) {
        return getNames(withinProject, RegExUtil.Magento.MODULE_NAME);
    }

    /**
     * Returns Theme Names.
     *
     * @param withinProject boolean
     * @return List
     */
    public List<String> getThemeNames(final boolean withinProject) {
        return getNames(withinProject, RegExUtil.Magento.THEME_NAME);
    }

    private List<String> getNames(
            final boolean withinProject,
            final String pattern
    ) {
        return ReadAction.compute(() -> {
            final FileBasedIndex index = FileBasedIndex.getInstance();
            final List<String> allModulesList = new ArrayList<>();
            final Collection<String> allModules = index.getAllKeys(ModuleNameIndex.KEY, project);
            for (final String moduleName : allModules) {
                if (!moduleName.matches(pattern)) {
                    continue;
                }
                final Collection<VirtualFile> files = index.getContainingFiles(
                            ModuleNameIndex.KEY, moduleName,
                            GlobalSearchScope.getScopeRestrictedByFileTypes(
                        GlobalSearchScope.allScope(project),
                        PhpFileType.INSTANCE
                ));
                if (files.isEmpty()) {
                    continue;
                }
                for (final VirtualFile virtualFile : files) {
                    if (withinProject && !IsFileInEditableModuleUtil.execute(project, virtualFile)) {
                        continue;
                    }

                    allModulesList.add(moduleName);
                    break;
                }
            }
            Collections.sort(allModulesList);
            return allModulesList;
        });
    }

    /**
     * Returns PSI directory of the certain module.
     *
     * @param moduleName String
     *
     * @return PsiDirectory
     */
    public @Nullable PsiDirectory getModuleDirectoryByModuleName(final String moduleName) {
        return ReadAction.compute(() -> {
            if (DumbService.getInstance(project).isDumb() || moduleName == null) {
                return null;
            }
            final FileBasedIndex index = FileBasedIndex.getInstance();

            final Collection<VirtualFile> files = new ArrayList<>(index.getContainingFiles(
                    ModuleNameIndex.KEY,
                    moduleName,
                    GlobalSearchScope.getScopeRestrictedByFileTypes(
                            GlobalSearchScope.allScope(project),
                            PhpFileType.INSTANCE
                    )
            ));

            if (files.isEmpty()) {
                return null;
            }
            final VirtualFile virtualFile = files.iterator().next();

            return PsiManager.getInstance(project).findDirectory(virtualFile.getParent());
        });
    }
}
