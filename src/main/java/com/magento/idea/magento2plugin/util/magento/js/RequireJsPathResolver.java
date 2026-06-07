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
            return result;
        }

        final String relativePath = requireJsPath.substring(moduleName.length() + 1);
        final Collection<String> moduleRootPaths = getModuleRootPaths(project, moduleName);

        for (final String moduleRootPath : moduleRootPaths) {
            for (final String area : VIEW_AREAS) {
                addIfFound(project, result, moduleRootPath + "/view/" + area + "/web/" + toJsPath(relativePath));
            }
        }
        if (result.isEmpty()) {
            addModuleJsCandidatesByPath(project, result, moduleName, relativePath);
        }

        return result;
    }

    private void addModuleJsCandidatesByPath(
            final @NotNull Project project,
            final @NotNull Collection<VirtualFile> result,
            final @NotNull String moduleName,
            final @NotNull String relativePath
    ) {
        final String appCodeModulePath = "/app/code/" + moduleName.replace('_', '/') + "/";
        final String vendorModulePath = "/vendor/" + toComposerPackagePath(moduleName) + "/";

        for (final String area : VIEW_AREAS) {
            addFilenameMatches(project, result, appCodeModulePath + "view/" + area + "/web/" + toJsPath(relativePath));
            addFilenameMatches(project, result, vendorModulePath + "view/" + area + "/web/" + toJsPath(relativePath));
        }
        addThemeFilenameMatches(project, result, moduleName + "/web/" + toJsPath(relativePath));
    }

    private void addThemeFilenameMatches(
            final @NotNull Project project,
            final @NotNull Collection<VirtualFile> result,
            final @NotNull String themePathSuffix
    ) {
        final String fileName = themePathSuffix.substring(themePathSuffix.lastIndexOf('/') + 1);
        final Collection<VirtualFile> files = FilenameIndex.getVirtualFilesByName(
                fileName,
                GlobalSearchScope.allScope(project)
        );

        for (final VirtualFile matchingFile : files) {
            if (!matchingFile.isDirectory()
                    && matchingFile.getPath().contains("/app/design/")
                    && matchingFile.getPath().endsWith("/" + themePathSuffix)) {
                result.add(matchingFile);
            }
        }
    }

    public @Nullable String getRequireJsPath(final @NotNull PsiFile psiFile) {
        final VirtualFile virtualFile = psiFile.getVirtualFile();

        if (virtualFile == null) {
            return null;
        }

        final String filePath = virtualFile.getPath();
        final String modulePath = getModuleRequireJsPath(psiFile.getProject(), filePath);

        if (modulePath != null) {
            return modulePath;
        }

        final String themePath = getThemeRequireJsPath(filePath);

        if (themePath != null) {
            return themePath;
        }

        final String libPath = getLibRequireJsPath(psiFile.getProject(), filePath);

        if (libPath != null) {
        } else {
        }

        return libPath;
    }

    private @Nullable String getModuleRequireJsPath(
            final @NotNull Project project,
            final @NotNull String filePath
    ) {
        final Collection<String> moduleNames = FileBasedIndex.getInstance()
                .getAllKeys(ModuleXmlIndex.KEY, project);

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

    private @Nullable String getThemeRequireJsPath(final @NotNull String filePath) {
        final String marker = "/app/design/";
        final int markerIndex = filePath.indexOf(marker);

        if (markerIndex < 0) {
            return null;
        }
        final String relativePath = filePath.substring(markerIndex + marker.length());
        final String[] parts = relativePath.split("/");

        if (parts.length < 7 || !"web".equals(parts[4]) || !filePath.endsWith(".js")) {
            return null;
        }

        return parts[3] + "/" + stripJsExtension(joinParts(parts, 5));
    }

    private @NotNull Collection<String> getModuleRootPaths(
            final @NotNull Project project,
            final @NotNull String moduleName
    ) {
        final Collection<String> moduleRootPaths = new ArrayList<>(FileBasedIndex.getInstance()
                .getValues(ModuleXmlIndex.KEY, moduleName, GlobalSearchScope.allScope(project)));

        if (!moduleRootPaths.isEmpty()) {
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
        addFilenameMatches(project, result, filePath);
    }

    private void addFilenameMatches(
            final @NotNull Project project,
            final @NotNull Collection<VirtualFile> result,
            final @NotNull String filePath
    ) {
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

    private @NotNull String toComposerPackagePath(final @NotNull String moduleName) {
        final String[] parts = moduleName.split("_", 2);
        final String vendor = parts[0].toLowerCase();
        final String packageName = parts.length > 1 ? "module-" + camelToKebab(parts[1]) : camelToKebab(parts[0]);

        return vendor + "/" + packageName;
    }

    private @NotNull String camelToKebab(final @NotNull String value) {
        return value
                .replaceAll("([a-z0-9])([A-Z])", "$1-$2")
                .replaceAll("([A-Z]+)([A-Z][a-z])", "$1-$2")
                .replace('_', '-')
                .toLowerCase();
    }

    private @NotNull String normalizeRequireJsPath(final @NotNull String path) {
        return path.replace("\"", "").replace("'", "").trim();
    }

    private @NotNull String toJsPath(final @NotNull String path) {
        return path.endsWith(".js") ? path : path + ".js";
    }

    private @NotNull String stripJsExtension(final @NotNull String path) {
        return path.substring(0, path.length() - ".js".length());
    }

    private @NotNull String joinParts(
            final @NotNull String[] parts,
            final int startIndex
    ) {
        final List<String> result = new ArrayList<>();

        for (int index = startIndex; index < parts.length; index++) {
            result.add(parts[index]);
        }

        return String.join("/", result);
    }
}
