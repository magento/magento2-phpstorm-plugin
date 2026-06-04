/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.util.magento.js;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.FileBasedIndex;
import com.magento.idea.magento2plugin.project.Settings;
import com.magento.idea.magento2plugin.project.diagnostic.NavigationInstrumentation;
import com.magento.idea.magento2plugin.reference.provider.util.GetModuleFileUtil;
import com.magento.idea.magento2plugin.reference.provider.util.GetModuleNameUtil;
import com.magento.idea.magento2plugin.stubs.indexes.js.RequireJsIndex;
import com.magento.idea.magento2plugin.stubs.indexes.xml.ModuleXmlIndex;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RequireJsPathResolver {
    private static final String[] VIEW_AREAS = {"frontend", "adminhtml", "base"};

    private static RequireJsPathResolver INSTANCE;

    private RequireJsPathResolver() {
    }

    public static RequireJsPathResolver getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new RequireJsPathResolver();
        }

        return INSTANCE;
    }

    public @NotNull List<PsiElement> resolveJsFiles(
            final @NotNull Project project,
            final @NotNull String requireJsPath
    ) {
        final List<PsiElement> result = new ArrayList<>();
        final PsiManager psiManager = PsiManager.getInstance(project);

        for (final VirtualFile file : findJsFiles(project, requireJsPath)) {
            final PsiFile psiFile = psiManager.findFile(file);

            if (psiFile != null) {
                result.add(psiFile);
            }
        }

        return result;
    }

    public @NotNull List<PsiElement> resolveJsFilesOrAlias(
            final @NotNull Project project,
            final @NotNull String requireJsPath
    ) {
        final Set<PsiElement> result = new LinkedHashSet<>(resolveJsFiles(project, requireJsPath));
        final Collection<String> mappedPaths = FileBasedIndex.getInstance().getValues(
                RequireJsIndex.KEY,
                normalizeRequireJsPath(requireJsPath),
                GlobalSearchScope.allScope(project)
        );

        for (final String mappedPath : mappedPaths) {
            result.addAll(resolveJsFiles(project, mappedPath));
        }

        return new ArrayList<>(result);
    }

    public @NotNull Collection<VirtualFile> findJsFiles(
            final @NotNull Project project,
            final @NotNull String rawRequireJsPath
    ) {
        final String requireJsPath = normalizeRequireJsPath(rawRequireJsPath);
        final List<VirtualFile> result = new ArrayList<>();
        final String moduleName = GetModuleNameUtil.getInstance().execute(requireJsPath);

        if (moduleName == null) {
            result.addAll(findLibJsFiles(project, requireJsPath));
            NavigationInstrumentation.infoOnce(
                    "requirejs-resolve-lib-" + requireJsPath,
                    () -> "Resolved RequireJS lib path '" + requireJsPath
                            + "' targets=" + result.size()
            );
            return result;
        }

        final String relativePath = requireJsPath.substring(moduleName.length() + 1);
        final Collection<String> moduleRootPaths = getModuleRootPaths(project, moduleName);

        for (final String moduleRootPath : moduleRootPaths) {
            for (final String area : VIEW_AREAS) {
                addIfFound(project, result, moduleRootPath + "/view/" + area + "/web/" + toJsPath(relativePath));
            }
        }
        NavigationInstrumentation.infoOnce(
                "requirejs-resolve-module-" + requireJsPath,
                () -> "Resolved RequireJS module path '" + requireJsPath
                        + "' module=" + moduleName
                        + " moduleRoots=" + moduleRootPaths.size()
                        + " targets=" + result.size()
        );

        return result;
    }

    public @Nullable String getRequireJsPath(final @NotNull PsiFile psiFile) {
        final VirtualFile virtualFile = psiFile.getVirtualFile();

        if (virtualFile == null) {
            return null;
        }

        final String filePath = virtualFile.getPath();
        final String modulePath = getModuleRequireJsPath(psiFile.getProject(), filePath);

        if (modulePath != null) {
            NavigationInstrumentation.infoOnce(
                    "requirejs-path-for-file-module-" + filePath,
                    () -> "Computed Magento RequireJS path '" + modulePath + "' for "
                            + NavigationInstrumentation.describeFile(psiFile)
            );
            return modulePath;
        }

        final String libPath = getLibRequireJsPath(psiFile.getProject(), filePath);

        if (libPath != null) {
            NavigationInstrumentation.infoOnce(
                    "requirejs-path-for-file-lib-" + filePath,
                    () -> "Computed Magento lib RequireJS path '" + libPath + "' for "
                            + NavigationInstrumentation.describeFile(psiFile)
            );
        } else {
            NavigationInstrumentation.infoOnce(
                    "requirejs-path-for-file-empty-" + filePath,
                    () -> "Could not compute Magento RequireJS path for "
                            + NavigationInstrumentation.describeFile(psiFile)
                            + "; " + NavigationInstrumentation.describeSettings(psiFile.getProject())
            );
        }

        return libPath;
    }

    private @Nullable String getModuleRequireJsPath(
            final @NotNull Project project,
            final @NotNull String filePath
    ) {
        final Collection<String> moduleNames = FileBasedIndex.getInstance()
                .getAllKeys(ModuleXmlIndex.KEY, project);
        NavigationInstrumentation.infoOnce(
                "requirejs-module-path-module-key-count-" + project.getLocationHash(),
                () -> "RequireJS path resolver sees module_xml keys=" + moduleNames.size()
                        + "; " + NavigationInstrumentation.describeSettings(project)
        );

        for (final String moduleName : moduleNames) {
            final Collection<String> moduleRootPaths = getModuleRootPaths(project, moduleName);

            for (final String moduleRootPath : moduleRootPaths) {
                for (final String area : VIEW_AREAS) {
                    final String webRoot = moduleRootPath + "/view/" + area + "/web/";

                    if (filePath.startsWith(webRoot) && filePath.endsWith(".js")) {
                        return moduleName + "/" + stripJsExtension(filePath.substring(webRoot.length()));
                    }
                }
            }
        }

        return null;
    }

    private @NotNull Collection<String> getModuleRootPaths(
            final @NotNull Project project,
            final @NotNull String moduleName
    ) {
        final Collection<String> moduleRootPaths = new ArrayList<>(FileBasedIndex.getInstance()
                .getValues(ModuleXmlIndex.KEY, moduleName, GlobalSearchScope.allScope(project)));

        if (!moduleRootPaths.isEmpty()) {
            NavigationInstrumentation.infoOnce(
                    "requirejs-module-roots-index-" + moduleName,
                    () -> "RequireJS module roots from module_xml for '" + moduleName
                            + "' count=" + moduleRootPaths.size()
            );
            return moduleRootPaths;
        }

        final Collection<VirtualFile> moduleFiles = GetModuleFileUtil.getInstance().execute(
                moduleName,
                project
        );

        if (moduleFiles == null) {
            return moduleRootPaths;
        }

        for (final VirtualFile moduleFile : moduleFiles) {
            if (moduleFile == null) {
                continue;
            }
            final VirtualFile moduleRoot = moduleFile.isDirectory()
                    ? moduleFile
                    : moduleFile.getParent();

            if (moduleRoot != null) {
                moduleRootPaths.add(moduleRoot.getPath());
            }
        }
        NavigationInstrumentation.infoOnce(
                "requirejs-module-roots-fallback-" + moduleName,
                () -> "RequireJS module roots from fallback for '" + moduleName
                        + "' count=" + moduleRootPaths.size()
        );

        return moduleRootPaths;
    }

    private @Nullable String getLibRequireJsPath(
            final @NotNull Project project,
            final @NotNull String filePath
    ) {
        final String magentoPath = Settings.getMagentoPath(project);

        if (magentoPath == null) {
            return null;
        }

        final String libWebRoot = magentoPath + "/lib/web/";

        if (!filePath.startsWith(libWebRoot) || !filePath.endsWith(".js")) {
            return null;
        }

        return stripJsExtension(filePath.substring(libWebRoot.length()));
    }

    private @NotNull Collection<VirtualFile> findLibJsFiles(
            final @NotNull Project project,
            final @NotNull String requireJsPath
    ) {
        final List<VirtualFile> result = new ArrayList<>();
        final String magentoPath = Settings.getMagentoPath(project);

        if (magentoPath == null) {
            return result;
        }

        addIfFound(project, result, magentoPath + "/lib/web/" + toJsPath(requireJsPath));

        return result;
    }

    private void addIfFound(
            final @NotNull Project project,
            final @NotNull Collection<VirtualFile> result,
            final @NotNull String filePath
    ) {
        final VirtualFile file = VirtualFileManager.getInstance().findFileByUrl("file://" + filePath);

        if (file != null && !file.isDirectory()) {
            result.add(file);
            return;
        }

        final String fileName = filePath.substring(filePath.lastIndexOf('/') + 1);
        final Collection<VirtualFile> files = FilenameIndex.getVirtualFilesByName(
                fileName,
                GlobalSearchScope.allScope(project)
        );

        for (final VirtualFile matchingFile : files) {
            if (!matchingFile.isDirectory() && matchingFile.getPath().endsWith(filePath)) {
                result.add(matchingFile);
            }
        }
    }

    private @NotNull String normalizeRequireJsPath(final @NotNull String path) {
        return path.replace("\"", "").replace("'", "");
    }

    private @NotNull String toJsPath(final @NotNull String path) {
        return path.endsWith(".js") ? path : path + ".js";
    }

    private @NotNull String stripJsExtension(final @NotNull String path) {
        return path.substring(0, path.length() - ".js".length());
    }
}
