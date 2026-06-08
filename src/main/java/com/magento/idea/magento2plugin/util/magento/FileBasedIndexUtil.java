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
import com.magento.idea.magento2plugin.magento.packages.Areas;
import com.magento.idea.magento2plugin.magento.packages.File;
import com.magento.idea.magento2plugin.magento.packages.Package;
import com.magento.idea.magento2plugin.stubs.indexes.xml.ModuleXmlIndex;
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

        for (final VirtualFile moduleRoot : findModuleRoots(moduleName, project)) {
            addViewDirectory(viewVfs, moduleRoot);
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

        final Collection<VirtualFile> moduleRoots = findModuleRoots(moduleName, project);
        if (moduleRoots.isEmpty()) {
            return null;
        }

        final VirtualFile moduleRoot = moduleRoots.iterator().next();

        String relativePath = File.separator.concat(directory)
                .concat(File.separator);
        if (!area.equals(Areas.base) || includeBaseArea) {
            relativePath = relativePath.concat(area.toString()).concat(File.separator);
        }
        if (subdirectory != null) {
            relativePath = relativePath.concat(subdirectory).concat(File.separator);
        }
        relativePath = relativePath.concat(virtualFieName);

        final VirtualFile configFile = moduleRoot.findFileByRelativePath(relativePath);
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
                .getAllKeys(ModuleXmlIndex.KEY, project)) {
            for (final VirtualFile moduleRoot : findModuleRoots(moduleName, project)) {
                if (moduleVf.getPath().startsWith(moduleRoot.getPath())) {
                    addViewDirectory(viewVfs, moduleRoot);
                }
            }
        }
        return viewVfs;
    }

    private static Collection<VirtualFile> findModuleRoots(
            final String moduleName,
            final Project project
    ) {
        final Collection<VirtualFile> moduleRoots = new ArrayList<>();
        final Collection<VirtualFile> moduleXmlFiles = FileBasedIndex.getInstance().getContainingFiles(
                ModuleXmlIndex.KEY,
                moduleName,
                GlobalSearchScope.allScope(project)
        );

        for (final VirtualFile moduleXmlFile : moduleXmlFiles) {
            final VirtualFile moduleRoot = getModuleRoot(moduleXmlFile);
            if (moduleRoot != null) {
                moduleRoots.add(moduleRoot);
            }
        }

        return moduleRoots;
    }

    private static VirtualFile getModuleRoot(final VirtualFile moduleXmlFile) {
        if (moduleXmlFile == null || moduleXmlFile.getParent() == null) {
            return null;
        }

        return moduleXmlFile.getParent().getParent();
    }

    private static void addViewDirectory(
            final Collection<VirtualFile> viewVfs,
            final VirtualFile moduleRoot
    ) {
        final VirtualFile viewVf = moduleRoot.findChild("view");
        if (viewVf != null) {
            viewVfs.add(viewVf);
        }
    }
}
