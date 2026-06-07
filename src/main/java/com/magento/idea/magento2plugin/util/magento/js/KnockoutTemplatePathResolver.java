/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.util.magento.js;

import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.JSProperty;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.indexing.FileBasedIndex;
import com.magento.idea.magento2plugin.project.Settings;
import com.magento.idea.magento2plugin.project.diagnostic.NavigationInstrumentation;
import com.magento.idea.magento2plugin.reference.provider.util.GetModuleFileUtil;
import com.magento.idea.magento2plugin.reference.provider.util.GetModuleNameUtil;
import com.magento.idea.magento2plugin.stubs.indexes.xml.ModuleXmlIndex;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class KnockoutTemplatePathResolver {
    private static final String[] VIEW_AREAS = {"frontend", "adminhtml", "base"};
    private static final String TEMPLATE_DIRECTORY = "template";
    private static final String TEMPLATES_DIRECTORY = "templates";
    private static KnockoutTemplatePathResolver INSTANCE;

    private KnockoutTemplatePathResolver() {
    }

    public static KnockoutTemplatePathResolver getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new KnockoutTemplatePathResolver();
        }

        return INSTANCE;
    }

    public @NotNull Set<String> collectTemplatePaths(final @NotNull JSFile jsFile) {
        final Set<String> templatePaths = new LinkedHashSet<>();
        final Collection<JSProperty> properties = PsiTreeUtil.findChildrenOfType(
                jsFile,
                JSProperty.class
        );

        for (final JSProperty property : properties) {
            final String templatePath = getTemplatePath(property);

            if (templatePath != null) {
                templatePaths.add(templatePath);
            }
        }

        return templatePaths;
    }

    public @Nullable String getTemplatePath(final @Nullable JSProperty property) {
        if (property == null || !isTemplateProperty(property)) {
            return null;
        }
        final JSExpression value = property.getValue();

        if (value == null || !isQuotedText(value.getText())) {
            return null;
        }

        return normalizeTemplatePath(value.getText());
    }

    private boolean isTemplateProperty(final @NotNull JSProperty property) {
        if (isTemplatePropertyName(property.getName())) {
            return true;
        }
        final PsiElement parent = property.getParent();

        if (parent == null) {
            return false;
        }
        final JSProperty parentProperty = PsiTreeUtil.getParentOfType(parent, JSProperty.class);

        return parentProperty != null && "templates".equals(parentProperty.getName());
    }

    public boolean isTemplatePropertyName(final @Nullable String propertyName) {
        if (propertyName == null || propertyName.isBlank()) {
            return false;
        }
        final String lowerName = propertyName.toLowerCase();

        return "template".equals(lowerName)
                || lowerName.endsWith("template")
                || lowerName.endsWith("tmpl");
    }

    public @Nullable String normalizeTemplatePath(final @Nullable String rawPath) {
        if (rawPath == null) {
            return null;
        }
        String path = rawPath
                .replace("\"", "")
                .replace("'", "")
                .trim();

        if (path.startsWith("text!")) {
            path = path.substring("text!".length());
        }
        if (path.endsWith(".html")) {
            path = path.substring(0, path.length() - ".html".length());
        }

        return path.isBlank() ? null : path;
    }

    private boolean isQuotedText(final @NotNull String text) {
        final String trimmed = text.trim();

        return (trimmed.startsWith("'") && trimmed.endsWith("'"))
                || (trimmed.startsWith("\"") && trimmed.endsWith("\""));
    }

    public @NotNull List<PsiElement> resolveTemplateFiles(
            final @NotNull Project project,
            final @NotNull String templatePath
    ) {
        final List<PsiElement> result = new ArrayList<>();
        final PsiManager psiManager = PsiManager.getInstance(project);

        for (final VirtualFile file : findTemplateFiles(project, templatePath)) {
            final PsiFile psiFile = psiManager.findFile(file);

            if (psiFile != null) {
                result.add(psiFile);
            }
        }

        return result;
    }

    public @NotNull Collection<VirtualFile> findTemplateFiles(
            final @NotNull Project project,
            final @NotNull String rawTemplatePath
    ) {
        final String templatePath = normalizeTemplatePath(rawTemplatePath);
        final Set<VirtualFile> result = new LinkedHashSet<>();

        if (templatePath == null) {
            return result;
        }
        final String moduleName = GetModuleNameUtil.getInstance().execute(templatePath);

        if (moduleName == null) {
            addNonModuleTemplateCandidates(project, result, templatePath);
            return result;
        }
        final String relativePath = templatePath.substring(moduleName.length() + 1);

        for (final String moduleRootPath : getModuleRootPaths(project, moduleName)) {
            for (final String area : VIEW_AREAS) {
                final String webRoot = moduleRootPath + "/view/" + area + "/web/";

                addIfFound(project, result, webRoot + toHtmlPath(relativePath));
                addIfFound(project, result, webRoot + TEMPLATE_DIRECTORY + "/" + toHtmlPath(relativePath));
                addIfFound(project, result, webRoot + TEMPLATES_DIRECTORY + "/" + toHtmlPath(relativePath));
            }
        }
        if (result.isEmpty()) {
            addModuleTemplateCandidatesByPath(project, result, moduleName, relativePath);
        }
        NavigationInstrumentation.infoOnce(
                "ko-template-resolve-module-" + templatePath,
                () -> "Resolved Knockout template path '" + templatePath
                        + "' module=" + moduleName
                        + " targets=" + result.size()
        );

        return result;
    }

    private void addModuleTemplateCandidatesByPath(
            final @NotNull Project project,
            final @NotNull Set<VirtualFile> result,
            final @NotNull String moduleName,
            final @NotNull String relativePath
    ) {
        final String appCodeModulePath = "/app/code/" + moduleName.replace('_', '/') + "/";
        final String vendorModulePath = "/vendor/" + toComposerPackagePath(moduleName) + "/";

        for (final String area : VIEW_AREAS) {
            addFilenameMatchesByModulePath(project, result, appCodeModulePath, area, relativePath);
            addFilenameMatchesByModulePath(project, result, vendorModulePath, area, relativePath);
        }
        addThemeFilenameMatches(project, result, moduleName, relativePath);
    }

    private void addFilenameMatchesByModulePath(
            final @NotNull Project project,
            final @NotNull Set<VirtualFile> result,
            final @NotNull String modulePath,
            final @NotNull String area,
            final @NotNull String relativePath
    ) {
        final String webRoot = modulePath + "view/" + area + "/web/";

        addFilenameMatches(project, result, webRoot + toHtmlPath(relativePath));
        addFilenameMatches(project, result, webRoot + TEMPLATE_DIRECTORY + "/" + toHtmlPath(relativePath));
        addFilenameMatches(project, result, webRoot + TEMPLATES_DIRECTORY + "/" + toHtmlPath(relativePath));
    }

    private void addThemeFilenameMatches(
            final @NotNull Project project,
            final @NotNull Set<VirtualFile> result,
            final @NotNull String moduleName,
            final @NotNull String relativePath
    ) {
        addThemeFilenameMatches(project, result, moduleName + "/web/" + toHtmlPath(relativePath));
        addThemeFilenameMatches(project, result, moduleName + "/web/" + TEMPLATE_DIRECTORY + "/" + toHtmlPath(relativePath));
        addThemeFilenameMatches(project, result, moduleName + "/web/" + TEMPLATES_DIRECTORY + "/" + toHtmlPath(relativePath));
    }

    private void addThemeFilenameMatches(
            final @NotNull Project project,
            final @NotNull Set<VirtualFile> result,
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

    public @NotNull Set<String> getTemplateRequireJsPaths(final @NotNull PsiFile psiFile) {
        final Set<String> result = new LinkedHashSet<>();
        final VirtualFile virtualFile = psiFile.getVirtualFile();

        if (virtualFile == null || !"html".equals(virtualFile.getExtension())) {
            return result;
        }
        final String filePath = virtualFile.getPath();
        final Project project = psiFile.getProject();
        final Collection<String> moduleNames = FileBasedIndex.getInstance()
                .getAllKeys(ModuleXmlIndex.KEY, project);
        NavigationInstrumentation.infoOnce(
                "ko-template-requirejs-paths-module-key-count-" + project.getLocationHash(),
                () -> "Knockout template path resolver sees module_xml keys=" + moduleNames.size()
                        + "; " + NavigationInstrumentation.describeSettings(project)
        );

        for (final String moduleName : moduleNames) {
            for (final String moduleRootPath : getModuleRootPaths(project, moduleName)) {
                for (final String area : VIEW_AREAS) {
                    final String webRoot = moduleRootPath + "/view/" + area + "/web/";

                    if (!filePath.startsWith(webRoot)) {
                        continue;
                    }
                    addTemplateRequireJsPathAliases(
                            result,
                            moduleName,
                            stripHtmlExtension(filePath.substring(webRoot.length()))
                    );
                }
            }
        }
        addTemplateRequireJsPathsFromFilePath(result, filePath);
        addThemeTemplateRequireJsPathsFromFilePath(result, filePath);
        NavigationInstrumentation.infoOnce(
                "ko-template-requirejs-paths-result-" + filePath,
                () -> "Computed Knockout template RequireJS paths for "
                        + NavigationInstrumentation.describeFile(psiFile)
                        + " count=" + result.size()
        );

        return result;
    }

    private void addTemplateRequireJsPathsFromFilePath(
            final @NotNull Set<String> result,
            final @NotNull String filePath
    ) {
        final String appCodeMarker = "/app/code/";
        final int appCodeIndex = filePath.indexOf(appCodeMarker);

        if (appCodeIndex < 0) {
            return;
        }
        final String moduleRelativePath = filePath.substring(appCodeIndex + appCodeMarker.length());
        final String[] pathParts = moduleRelativePath.split("/");

        if (pathParts.length < 7 || !"view".equals(pathParts[2]) || !"web".equals(pathParts[4])) {
            return;
        }
        final String moduleName = pathParts[0] + "_" + pathParts[1];
        final String webRoot = appCodeMarker + pathParts[0] + "/" + pathParts[1] + "/view/"
                + pathParts[3] + "/web/";
        final int webRootIndex = filePath.indexOf(webRoot);

        if (webRootIndex < 0) {
            return;
        }
        addTemplateRequireJsPathAliases(
                result,
                moduleName,
                stripHtmlExtension(filePath.substring(webRootIndex + webRoot.length()))
        );
    }

    private void addThemeTemplateRequireJsPathsFromFilePath(
            final @NotNull Set<String> result,
            final @NotNull String filePath
    ) {
        final String marker = "/app/design/";
        final int markerIndex = filePath.indexOf(marker);

        if (markerIndex < 0) {
            return;
        }
        final String relativePath = filePath.substring(markerIndex + marker.length());
        final String[] parts = relativePath.split("/");

        if (parts.length < 7 || !"web".equals(parts[4])) {
            return;
        }
        addTemplateRequireJsPathAliases(
                result,
                parts[3],
                stripHtmlExtension(joinParts(parts, 5))
        );
    }

    private void addTemplateRequireJsPathAliases(
            final @NotNull Set<String> result,
            final @NotNull String moduleName,
            final @NotNull String relativePath
    ) {
        result.add(moduleName + "/" + relativePath);
        addDirectoryAlias(result, moduleName, relativePath, TEMPLATE_DIRECTORY);
        addDirectoryAlias(result, moduleName, relativePath, TEMPLATES_DIRECTORY);
    }

    private void addDirectoryAlias(
            final @NotNull Set<String> result,
            final @NotNull String moduleName,
            final @NotNull String relativePath,
            final @NotNull String directory
    ) {
        final String prefix = directory + "/";

        if (relativePath.startsWith(prefix)) {
            result.add(moduleName + "/" + relativePath.substring(prefix.length()));
        }
    }

    private void addNonModuleTemplateCandidates(
            final @NotNull Project project,
            final @NotNull Set<VirtualFile> result,
            final @NotNull String templatePath
    ) {
        final String magentoPath = Settings.getMagentoPath(project);

        if (magentoPath != null) {
            addIfFound(project, result, magentoPath + "/lib/web/" + toHtmlPath(templatePath));
        }
        addFilenameMatches(project, result, toHtmlPath(templatePath));
        addFilenameMatches(project, result, TEMPLATE_DIRECTORY + "/" + toHtmlPath(templatePath));
        addFilenameMatches(project, result, TEMPLATES_DIRECTORY + "/" + toHtmlPath(templatePath));
        NavigationInstrumentation.infoOnce(
                "ko-template-resolve-non-module-" + templatePath,
                () -> "Resolved non-module Knockout template path '" + templatePath
                        + "' targets=" + result.size()
        );
    }

    private @NotNull Collection<String> getModuleRootPaths(
            final @NotNull Project project,
            final @NotNull String moduleName
    ) {
        final Collection<String> moduleRootPaths = new ArrayList<>(FileBasedIndex.getInstance()
                .getValues(ModuleXmlIndex.KEY, moduleName, GlobalSearchScope.allScope(project)));

        if (!moduleRootPaths.isEmpty()) {
            NavigationInstrumentation.infoOnce(
                    "ko-template-module-roots-index-" + moduleName,
                    () -> "Knockout template module roots from module_xml for '" + moduleName
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
                "ko-template-module-roots-fallback-" + moduleName,
                () -> "Knockout template module roots from fallback for '" + moduleName
                        + "' count=" + moduleRootPaths.size()
        );

        return moduleRootPaths;
    }

    private void addIfFound(
            final @NotNull Project project,
            final @NotNull Set<VirtualFile> result,
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
            final @NotNull Set<VirtualFile> result,
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

    private @NotNull String toHtmlPath(final @NotNull String path) {
        return path.endsWith(".html") ? path : path + ".html";
    }

    private @NotNull String stripHtmlExtension(final @NotNull String path) {
        return path.endsWith(".html")
                ? path.substring(0, path.length() - ".html".length())
                : path;
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
