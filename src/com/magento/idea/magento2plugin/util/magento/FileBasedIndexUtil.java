/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.util.magento;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.FileBasedIndex;
import com.jetbrains.php.lang.PhpFileType;
import com.magento.idea.magento2plugin.magento.packages.Areas;
import com.magento.idea.magento2plugin.magento.packages.File;
import com.magento.idea.magento2plugin.magento.packages.Package;
import com.magento.idea.magento2plugin.stubs.indexes.ModuleNameIndex;
import com.magento.idea.magento2plugin.util.RegExUtil;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class FileBasedIndexUtil {

    private FileBasedIndexUtil() {}

    /**
     * Find all modules virtual files.
     *
     * @param moduleName String
     * @param project Project
     * @return Collection
     */
    public static Collection<VirtualFile> findViewVfsByModuleName(
            final String moduleName,
            final Project project
    ) {
        final Collection<VirtualFile> viewVfs = new ArrayList<>();

        final Pattern pattern = Pattern.compile(RegExUtil.Magento.MODULE_NAME);
        final Matcher matcher = pattern.matcher(moduleName);
        if (!matcher.find()) {
            return viewVfs;
        }

        final Collection<VirtualFile> moduleVfs =
                FileBasedIndex.getInstance().getContainingFiles(ModuleNameIndex.KEY, moduleName,
                    GlobalSearchScope.getScopeRestrictedByFileTypes(
                        GlobalSearchScope.allScope(project),
                        PhpFileType.INSTANCE
                )
        );

        for (final VirtualFile moduleVf : moduleVfs) {
            viewVfs.addAll(getValues(moduleName, moduleVf, project));
        }
        return viewVfs;
    }

    /**
     * Find module config file for the certain area.
     *
     * @param virtualFieName String
     * @param area Areas
     * @param moduleName String
     * @param project Project
     * @return PsiFile
     */
    public static PsiFile findModuleConfigFile(
            final String virtualFieName,
            final Areas area,
            final String moduleName,
            final Project project
    ) {
        return findModuleFile(
            virtualFieName,
            area,
            moduleName,
            project,
            Package.moduleBaseAreaDir,
            null,
            false
        );
    }

    /**
     * Find module config file for the certain area.
     *
     * @param virtualFieName String
     * @param area Areas
     * @param moduleName String
     * @param project Project
     * @return PsiFile
     */
    public static PsiFile findModuleViewFile(
            final String virtualFieName,
            final Areas area,
            final String moduleName,
            final Project project,
            final String subdirectory
    ) {

        return findModuleFile(
                virtualFieName,
                area,
                moduleName,
                project,
                Package.moduleViewDir,
                subdirectory,
                true
        );
    }

    /**
     * Find module config file for the certain area.
     *
     * @param virtualFieName String
     * @param area Areas
     * @param moduleName String
     * @param project Project
     * @return PsiFile
     */
    @SuppressWarnings({"PMD.UseObjectForClearerAPI"})
    public static PsiFile findModuleFile(
            final String virtualFieName,
            final Areas area,
            final String moduleName,
            final Project project,
            final String directory,
            final String subdirectory,
            final boolean includeBaseArea
    ) {
        final Pattern pattern = Pattern.compile(RegExUtil.Magento.MODULE_NAME);
        final Matcher matcher = pattern.matcher(moduleName);
        if (!matcher.find()) {
            return null;
        }

        final Collection<VirtualFile> moduleVfs =
                FileBasedIndex.getInstance().getContainingFiles(ModuleNameIndex.KEY, moduleName,
                    GlobalSearchScope.getScopeRestrictedByFileTypes(
                        GlobalSearchScope.allScope(project),
                        PhpFileType.INSTANCE
                    )
                );
        if (moduleVfs.isEmpty()) {
            return null;
        }

        final VirtualFile moduleVf = moduleVfs.iterator().next();

        String relativePath = File.separator.concat(directory)
                .concat(File.separator);
        if (!area.equals(Areas.base) || includeBaseArea) {
            relativePath = relativePath.concat(area.toString()).concat(File.separator);
        }
        if (subdirectory != null) {
            relativePath = relativePath.concat(subdirectory).concat(File.separator);
        }
        relativePath = relativePath.concat(virtualFieName);

        final VirtualFile configFile = moduleVf.getParent().findFileByRelativePath(relativePath);
        if (configFile == null) {
            return null;
        }
        return PsiManager.getInstance(project).findFile(configFile);
    }

    /**
     * Find all modules virtual files by module virtual file.
     *
     * @param moduleVf VirtualFile
     * @param project Project
     * @return Collection
     */
    public static Collection<VirtualFile> findViewVfsByModuleVf(
            final VirtualFile moduleVf,
            final Project project
    ) {
        final Collection<VirtualFile> viewVfs = new ArrayList<>();

        for (final String moduleName : FileBasedIndex.getInstance()
                .getAllKeys(ModuleNameIndex.KEY, project)) {
            viewVfs.addAll(getValues(moduleName, moduleVf, project));
        }
        return viewVfs;
    }

    private static Collection<VirtualFile> getValues(
            final String moduleName,
            final VirtualFile moduleVf,
            final Project project
    ) {
        final Collection<VirtualFile> viewVfs = new ArrayList<>();
        FileBasedIndex.getInstance()
                .processValues(
                        ModuleNameIndex.KEY, moduleName, moduleVf,
                        (file, value) -> {
                            final VirtualFile viewVf = file.getParent()
                                    .findFileByRelativePath(value.concat("/view"));
                            if (viewVf != null) {
                                viewVfs.add(viewVf);
                            }
                            return false;
                        },
                        GlobalSearchScope.fileScope(project, moduleVf)
                );
        return viewVfs;
    }
}
