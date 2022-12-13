/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.indexes;

import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.SlowOperations;
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
        return getModuleNames(Package.vendor, true);
    }

    public List<String> getEditableThemeNames() {
        return getThemeNames("/" + Package.vendor + "/magento/|/tests/|/test/", true);
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
        return getNames(filterPattern, withinProject, RegExUtil.Magento.MODULE_NAME);
    }

    /**
     * Returns Theme Names.
     *
     * @param filterPattern String
     * @param withinProject boolean
     * @return List
     */
    public List<String> getThemeNames(final String filterPattern, final boolean withinProject) {
        return getNames(filterPattern, withinProject, RegExUtil.Magento.THEME_NAME);
    }

    private List<String> getNames(
            final String filterPattern,
            final boolean withinProject,
            final String pattern
    ) {
        final FileBasedIndex index = FileBasedIndex
                .getInstance();
        final List<String> allModulesList = new ArrayList<>();
        final Collection<String> allModules = index.getAllKeys(ModuleNameIndex.KEY, project);
        final Pattern compiled = Pattern.compile(filterPattern);
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
            final VirtualFile virtualFile = files.iterator().next();
            if (withinProject && !VfsUtilCore
                    .isAncestor(GetProjectBasePath.execute(project), virtualFile, false)) {
                continue;
            }

            final Matcher matcher = compiled.matcher(virtualFile.getPath());
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
     *
     * @return PsiDirectory
     */
    public @Nullable PsiDirectory getModuleDirectoryByModuleName(final String moduleName) {
        if (DumbService.getInstance(project).isDumb() || moduleName == null) {
            return null;
        }
        final FileBasedIndex index = FileBasedIndex
                .getInstance();
        final Collection<VirtualFile> files = new ArrayList<>();

        SlowOperations.allowSlowOperations(() -> {
            files.addAll(
                    index.getContainingFiles(
                            ModuleNameIndex.KEY,
                            moduleName,
                            GlobalSearchScope.getScopeRestrictedByFileTypes(
                                    GlobalSearchScope.allScope(project),
                                    PhpFileType.INSTANCE
                            )
                    )
            );
        });

        if (files.isEmpty()) {
            return null;
        }
        final VirtualFile virtualFile = files.iterator().next();

        return PsiManager.getInstance(project).findDirectory(virtualFile.getParent());
    }
}
