/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.indexes;

import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.FileBasedIndex;
import com.jetbrains.php.lang.PhpFileType;
import com.magento.idea.magento2plugin.magento.packages.Package;
import com.magento.idea.magento2plugin.project.Settings;
import com.magento.idea.magento2plugin.stubs.indexes.ModuleNameIndex;
import com.magento.idea.magento2plugin.util.RegExUtil;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.nio.file.Paths;
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
            final Set<String> allModulesSet = new LinkedHashSet<>();
            final FileBasedIndex index = FileBasedIndex.getInstance();
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
                        )
                );
                if (files.isEmpty()) {
                    continue;
                }
                for (final VirtualFile virtualFile : files) {
                    if (withinProject && !IsFileInEditableModuleUtil.execute(project, virtualFile)) {
                        continue;
                    }

                    allModulesSet.add(moduleName);
                    break;
                }
            }

            collectEditableFilesystemModuleNames(allModulesSet, pattern);

            final List<String> allModulesList = new ArrayList<>(allModulesSet);
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
            if (moduleName == null) {
                return null;
            }

            if (!DumbService.getInstance(project).isDumb()) {
                final FileBasedIndex index = FileBasedIndex.getInstance();
                final Collection<VirtualFile> files = new ArrayList<>(index.getContainingFiles(
                        ModuleNameIndex.KEY,
                        moduleName,
                        GlobalSearchScope.getScopeRestrictedByFileTypes(
                                GlobalSearchScope.allScope(project),
                                PhpFileType.INSTANCE
                        )
                ));

                if (!files.isEmpty()) {
                    final VirtualFile virtualFile = files.iterator().next();
                    final PsiDirectory indexedDirectory = PsiManager.getInstance(project)
                            .findDirectory(virtualFile.getParent());
                    if (indexedDirectory != null) {
                        return indexedDirectory;
                    }
                }
            }

            return findEditableModuleDirectoryFromFilesystem(moduleName);
        });
    }

    private void collectEditableFilesystemModuleNames(
            final Collection<String> target,
            final String pattern
    ) {
        for (final VirtualFile packagesRoot : getEditablePackagesRoots()) {
            for (final VirtualFile vendorDirectory : packagesRoot.getChildren()) {
                if (!vendorDirectory.isDirectory()) {
                    continue;
                }

                for (final VirtualFile moduleDirectory : vendorDirectory.getChildren()) {
                    if (!moduleDirectory.isDirectory()) {
                        continue;
                    }
                    if (moduleDirectory.findChild("registration.php") == null) {
                        continue;
                    }

                    final String moduleName = vendorDirectory.getName()
                            + Package.vendorModuleNameSeparator
                            + moduleDirectory.getName();
                    if (moduleName.matches(pattern)) {
                        target.add(moduleName);
                    }
                }
            }
        }
    }

    private @Nullable PsiDirectory findEditableModuleDirectoryFromFilesystem(final String moduleName) {
        final String[] nameParts = moduleName.split(Package.vendorModuleNameSeparator, 2);
        if (nameParts.length != 2) {
            return null;
        }

        for (final VirtualFile packagesRoot : getEditablePackagesRoots()) {
            final VirtualFile vendorDirectory = packagesRoot.findChild(nameParts[0]);
            if (vendorDirectory == null || !vendorDirectory.isDirectory()) {
                continue;
            }

            final VirtualFile moduleDirectory = vendorDirectory.findChild(nameParts[1]);
            if (moduleDirectory == null || !moduleDirectory.isDirectory()) {
                continue;
            }

            final PsiDirectory psiDirectory = PsiManager.getInstance(project).findDirectory(moduleDirectory);
            if (psiDirectory != null) {
                return psiDirectory;
            }
        }

        return null;
    }

    private Collection<VirtualFile> getEditablePackagesRoots() {
        final Set<VirtualFile> packagesRoots = new LinkedHashSet<>();
        final LocalFileSystem fileSystem = LocalFileSystem.getInstance();

        for (final String rootPath : getMagentoRootCandidates()) {
            final VirtualFile packagesRoot = fileSystem.refreshAndFindFileByPath(
                    Paths.get(rootPath, Package.packagesRoot).normalize().toString()
            );
            if (packagesRoot != null && packagesRoot.isDirectory()) {
                packagesRoots.add(packagesRoot);
            }
        }

        return packagesRoots;
    }

    private Collection<String> getMagentoRootCandidates() {
        final Set<String> candidates = new LinkedHashSet<>();
        final String configuredRoot = Settings.getMagentoPath(project);
        if (configuredRoot == null || configuredRoot.isBlank()) {
            return candidates;
        }

        candidates.add(configuredRoot);

        final String basePath = project.getBasePath();
        if (basePath != null) {
            final String rootWithoutLeadingSlash = configuredRoot.startsWith("/")
                    ? configuredRoot.substring(1)
                    : configuredRoot;
            candidates.add(Paths.get(basePath, rootWithoutLeadingSlash).normalize().toString());
            candidates.add(Paths.get(basePath, configuredRoot).normalize().toString());
        }

        return candidates;
    }
}
