/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.util.magento;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.FileBasedIndex;
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

public class BlockTemplateNavigationUtil {
    private static final String[] VIEW_AREAS = {"frontend", "adminhtml", "base"};
    private static final String TEMPLATE_SEPARATOR = "::";
    private static BlockTemplateNavigationUtil INSTANCE;

    private BlockTemplateNavigationUtil() {
    }

    public static BlockTemplateNavigationUtil getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new BlockTemplateNavigationUtil();
        }

        return INSTANCE;
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
            final @NotNull String templatePath
    ) {
        final Set<VirtualFile> result = new LinkedHashSet<>();
        final TemplatePathParts templatePathParts = parseTemplatePath(templatePath);

        if (templatePathParts == null) {
            return result;
        }

        for (final String moduleRootPath : getModuleRootPaths(project, templatePathParts.getModuleName())) {
            for (final String area : VIEW_AREAS) {
                addIfFound(
                        project,
                        result,
                        moduleRootPath + "/view/" + area + "/templates/" + templatePathParts.getRelativePath()
                );
            }
        }

        return result;
    }

    public @NotNull Set<String> getTemplatePaths(final @NotNull PsiFile psiFile) {
        final Set<String> result = new LinkedHashSet<>();
        final VirtualFile virtualFile = psiFile.getVirtualFile();

        if (virtualFile == null || !"phtml".equals(virtualFile.getExtension())) {
            return result;
        }
        final String filePath = virtualFile.getPath();
        final Project project = psiFile.getProject();

        for (final String moduleName : FileBasedIndex.getInstance().getAllKeys(ModuleXmlIndex.KEY, project)) {
            for (final String moduleRootPath : getModuleRootPaths(project, moduleName)) {
                for (final String area : VIEW_AREAS) {
                    final String templatesRoot = moduleRootPath + "/view/" + area + "/templates/";

                    if (filePath.startsWith(templatesRoot)) {
                        result.add(moduleName + TEMPLATE_SEPARATOR + filePath.substring(templatesRoot.length()));
                    }
                }
            }
        }

        return result;
    }

    private @Nullable TemplatePathParts parseTemplatePath(final @NotNull String templatePath) {
        final int separatorIndex = templatePath.indexOf(TEMPLATE_SEPARATOR);

        if (separatorIndex <= 0 || separatorIndex + TEMPLATE_SEPARATOR.length() >= templatePath.length()) {
            return null;
        }
        final String moduleName = templatePath.substring(0, separatorIndex);
        final String relativePath = templatePath.substring(separatorIndex + TEMPLATE_SEPARATOR.length());

        if (GetModuleNameUtil.getInstance().execute(moduleName) == null || relativePath.isBlank()) {
            return null;
        }

        return new TemplatePathParts(moduleName, relativePath);
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

    private static class TemplatePathParts {
        private final String moduleName;
        private final String relativePath;

        TemplatePathParts(final @NotNull String moduleName, final @NotNull String relativePath) {
            this.moduleName = moduleName;
            this.relativePath = relativePath;
        }

        private @NotNull String getModuleName() {
            return moduleName;
        }

        private @NotNull String getRelativePath() {
            return relativePath;
        }
    }
}
