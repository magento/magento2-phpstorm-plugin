/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.indexes;

import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.IndexNotReadyException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.FileBasedIndex;
import com.intellij.util.indexing.ID;
import com.magento.idea.magento2plugin.magento.packages.Package;
import com.magento.idea.magento2plugin.project.Settings;
import com.magento.idea.magento2plugin.stubs.indexes.xml.ModuleXmlIndex;
import com.magento.idea.magento2plugin.stubs.indexes.xml.ThemeXmlIndex;
import com.magento.idea.magento2plugin.util.magento.IsFileInEditableModuleUtil;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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
        return getNames(ModuleXmlIndex.KEY, withinProject);
    }

    /**
     * Returns Theme Names.
     *
     * @param withinProject boolean
     * @return List
     */
    public List<String> getThemeNames(final boolean withinProject) {
        return getNames(ThemeXmlIndex.KEY, withinProject);
    }

    private List<String> getNames(
            final ID<String, String> indexKey,
            final boolean withinProject
    ) {
        return ReadAction.compute(() -> {
            final Set<String> names = new LinkedHashSet<>();
            final FileBasedIndex index = FileBasedIndex.getInstance();
            final Collection<String> allNames = index.getAllKeys(indexKey, project);
            for (final String name : allNames) {
                final Collection<VirtualFile> files = index.getContainingFiles(
                        indexKey,
                        name,
                        GlobalSearchScope.allScope(project)
                );
                if (files.isEmpty()) {
                    continue;
                }

                for (final VirtualFile virtualFile : files) {
                    final VirtualFile root = indexKey.equals(ModuleXmlIndex.KEY)
                            ? getModuleRoot(virtualFile)
                            : virtualFile.getParent();
                    if (root == null || (withinProject && !IsFileInEditableModuleUtil.execute(project, root))) {
                        continue;
                    }

                    names.add(name);
                    break;
                }
            }

            final List<String> allModulesList = new ArrayList<>(names);
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
            final VirtualFile moduleDirectory = getModuleDirectoryVirtualFileByModuleName(moduleName);
            if (moduleDirectory == null) {
                return null;
            }

            return PsiManager.getInstance(project).findDirectory(moduleDirectory);
        });
    }

    /**
     * Returns VirtualFile directory of the certain module.
     *
     * @param moduleName String
     *
     * @return VirtualFile
     */
    public @Nullable VirtualFile getModuleDirectoryVirtualFileByModuleName(final String moduleName) {
        return ReadAction.compute(() -> {
            if (moduleName == null) {
                return null;
            }

            final VirtualFile indexedDirectory = findIndexedModuleDirectory(moduleName);
            if (indexedDirectory != null) {
                return indexedDirectory;
            }

            final VirtualFile projectRootDirectory = findEditableModuleDirectoryFromProjectRoots(moduleName);
            if (projectRootDirectory != null) {
                return projectRootDirectory;
            }

            final PsiDirectory editableDirectory = findEditableModuleDirectoryFromFilesystem(moduleName);
            return editableDirectory == null ? null : editableDirectory.getVirtualFile();
        });
    }

    private @Nullable VirtualFile findIndexedModuleDirectory(final String moduleName) {
        final Collection<VirtualFile> files;
        try {
            files = new ArrayList<>(FileBasedIndex.getInstance().getContainingFiles(
                    ModuleXmlIndex.KEY,
                    moduleName,
                    GlobalSearchScope.allScope(project)
            ));
        } catch (IndexNotReadyException exception) {
            return null;
        }

        for (final VirtualFile virtualFile : files) {
            final VirtualFile moduleDirectory = getModuleRoot(virtualFile);
            if (moduleDirectory != null && moduleDirectory.isValid() && moduleDirectory.isDirectory()) {
                return moduleDirectory;
            }
        }

        return null;
    }

    private @Nullable VirtualFile findEditableModuleDirectoryFromProjectRoots(final String moduleName) {
        final String[] nameParts = moduleName.split(Package.vendorModuleNameSeparator, 2);
        if (nameParts.length != 2) {
            return null;
        }

        final String configuredRoot = Settings.getMagentoPath(project);
        if (configuredRoot == null || configuredRoot.isBlank()) {
            return null;
        }

        final String rootWithoutLeadingSlash = configuredRoot.startsWith("/")
                ? configuredRoot.substring(1)
                : configuredRoot;
        final String moduleRelativePath = Package.packagesRoot + "/"
                + nameParts[0] + "/" + nameParts[1];
        final List<String> relativePathCandidates = new ArrayList<>();
        relativePathCandidates.add(rootWithoutLeadingSlash + "/" + moduleRelativePath);
        relativePathCandidates.add(moduleRelativePath);

        for (final VirtualFile contentRoot : ProjectRootManager.getInstance(project).getContentRoots()) {
            for (final String relativePath : relativePathCandidates) {
                final VirtualFile moduleDirectory = contentRoot.findFileByRelativePath(relativePath);
                if (moduleDirectory != null
                        && moduleDirectory.isValid()
                        && moduleDirectory.isDirectory()
                        && moduleDirectory.findFileByRelativePath("etc/module.xml") != null) {
                    return moduleDirectory;
                }
            }
        }

        return null;
    }

    private @Nullable PsiDirectory findEditableModuleDirectoryFromFilesystem(final String moduleName) {
        final String[] nameParts = moduleName.split(Package.vendorModuleNameSeparator, 2);
        if (nameParts.length != 2) {
            return null;
        }

        final LocalFileSystem fileSystem = LocalFileSystem.getInstance();
        for (final String rootPath : getMagentoRootCandidates()) {
            final String modulePath = FileUtil.toSystemIndependentName(
                    Paths.get(rootPath, Package.packagesRoot, nameParts[0], nameParts[1]).normalize().toString()
            );
            final VirtualFile moduleDirectory = fileSystem.refreshAndFindFileByPath(modulePath);
            if (moduleDirectory == null
                    || !moduleDirectory.isDirectory()
                    || moduleDirectory.findFileByRelativePath("etc/module.xml") == null) {
                continue;
            }

            final PsiDirectory psiDirectory = PsiManager.getInstance(project).findDirectory(moduleDirectory);
            if (psiDirectory != null) {
                return psiDirectory;
            }
        }

        return null;
    }

    private @Nullable VirtualFile getModuleRoot(final VirtualFile moduleXmlFile) {
        if (moduleXmlFile == null || moduleXmlFile.getParent() == null) {
            return null;
        }

        return moduleXmlFile.getParent().getParent();
    }

    private Collection<String> getMagentoRootCandidates() {
        final Set<String> candidates = new LinkedHashSet<>();
        final String configuredRoot = Settings.getMagentoPath(project);
        if (configuredRoot == null || configuredRoot.isBlank()) {
            return candidates;
        }

        candidates.add(FileUtil.toSystemIndependentName(configuredRoot));

        final String basePath = project.getBasePath();
        if (basePath != null) {
            final String rootWithoutLeadingSlash = configuredRoot.startsWith("/")
                    ? configuredRoot.substring(1)
                    : configuredRoot;
            candidates.add(FileUtil.toSystemIndependentName(
                    Paths.get(basePath, rootWithoutLeadingSlash).normalize().toString()
            ));
            candidates.add(FileUtil.toSystemIndependentName(
                    Paths.get(basePath, configuredRoot).normalize().toString()
            ));
        }

        for (final VirtualFile contentRoot : ProjectRootManager.getInstance(project).getContentRoots()) {
            final String rootWithoutLeadingSlash = configuredRoot.startsWith("/")
                    ? configuredRoot.substring(1)
                    : configuredRoot;
            candidates.add(FileUtil.toSystemIndependentName(
                    Paths.get(contentRoot.getPath(), rootWithoutLeadingSlash).normalize().toString()
            ));
            candidates.add(FileUtil.toSystemIndependentName(
                    Paths.get(contentRoot.getPath(), configuredRoot).normalize().toString()
            ));
        }

        return candidates;
    }
}
