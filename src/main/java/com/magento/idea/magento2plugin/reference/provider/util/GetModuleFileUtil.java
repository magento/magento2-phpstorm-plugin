/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.reference.provider.util;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.FileBasedIndex;
import com.magento.idea.magento2plugin.project.Settings;
import com.magento.idea.magento2plugin.project.diagnostic.NavigationInstrumentation;
import com.magento.idea.magento2plugin.stubs.indexes.xml.ModuleXmlIndex;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Collection;

public class GetModuleFileUtil {
    private static final Pattern MODULE_NAME_PATTERN = Pattern.compile(
            "<module\\s+[^>]*name\\s*=\\s*['\\\"]([^'\\\"]+)['\\\"]"
    );

    private static GetModuleFileUtil INSTANCE;

    public static GetModuleFileUtil getInstance() {
        if (null == INSTANCE) {
            INSTANCE = new GetModuleFileUtil();
        }
        return INSTANCE;
    }

    public Collection<VirtualFile> execute(@NotNull String fileName, Project project)
    {
        String moduleName = GetModuleNameUtil.getInstance().execute(fileName);
        if (null == moduleName || moduleName.isEmpty()) {
            return null;
        }
        final Collection<VirtualFile> moduleXmlFiles = FileBasedIndex.getInstance().getContainingFiles(
                ModuleXmlIndex.KEY,
                moduleName,
                GlobalSearchScope.allScope(project)
        );
        final Collection<VirtualFile> moduleRoots = new ArrayList<>();

        for (final VirtualFile moduleXmlFile : moduleXmlFiles) {
            final VirtualFile moduleRoot = getModuleRoot(moduleXmlFile);
            if (moduleRoot != null) {
                moduleRoots.add(moduleRoot);
            }
        }

        if (moduleRoots.isEmpty()) {
            moduleRoots.addAll(findModuleRootsInMagentoPath(moduleName, project));
        }

        return moduleRoots;
    }

    private @NotNull Collection<VirtualFile> findModuleRootsInMagentoPath(
            final @NotNull String moduleName,
            final @NotNull Project project
    ) {
        final Set<VirtualFile> moduleRoots = new LinkedHashSet<>();
        final String magentoPath = Settings.getMagentoPath(project);

        if (magentoPath == null) {
            return moduleRoots;
        }

        addRootIfModuleMatches(moduleRoots, moduleName, magentoPath + "/app/code/" + moduleName.replace('_', '/'));
        addRootIfModuleMatches(moduleRoots, moduleName, magentoPath + "/vendor/" + toComposerPackagePath(moduleName));

        if (moduleRoots.isEmpty()) {
            for (final VirtualFile moduleXmlFile : com.magento.idea.magento2plugin.util.magento.MagentoVfsUtil
                    .findMagentoFiles(project, file -> "module.xml".equals(file.getName())
                            && file.getParent() != null
                            && "etc".equals(file.getParent().getName()))) {
                final VirtualFile moduleRoot = getModuleRoot(moduleXmlFile);

                if (moduleRoot != null && moduleName.equals(readModuleName(moduleXmlFile))) {
                    moduleRoots.add(moduleRoot);
                }
            }
        }

        NavigationInstrumentation.infoOnce(
                "module-root-vfs-fallback-" + moduleName,
                () -> "Module roots from Magento VFS fallback for '" + moduleName
                        + "' count=" + moduleRoots.size()
        );

        return moduleRoots;
    }

    private void addRootIfModuleMatches(
            final @NotNull Collection<VirtualFile> moduleRoots,
            final @NotNull String moduleName,
            final @NotNull String moduleRootPath
    ) {
        final VirtualFile moduleXml = com.magento.idea.magento2plugin.util.magento.MagentoVfsUtil
                .findByPath(moduleRootPath + "/etc/module.xml");

        if (moduleXml == null || moduleXml.isDirectory()) {
            return;
        }
        final VirtualFile moduleRoot = getModuleRoot(moduleXml);

        if (moduleRoot != null && moduleName.equals(readModuleName(moduleXml))) {
            moduleRoots.add(moduleRoot);
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

    private @Nullable String readModuleName(final @NotNull VirtualFile moduleXmlFile) {
        try {
            final Matcher matcher = MODULE_NAME_PATTERN.matcher(VfsUtilCore.loadText(moduleXmlFile));

            return matcher.find() ? matcher.group(1) : null;
        } catch (Exception ignored) {
            return null;
        }
    }

    private @Nullable VirtualFile getModuleRoot(final VirtualFile moduleXmlFile) {
        if (moduleXmlFile == null || moduleXmlFile.getParent() == null) {
            return null;
        }

        return moduleXmlFile.getParent().getParent();
    }
}
