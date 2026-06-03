/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.reference.provider.util;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.FileBasedIndex;
import com.intellij.util.indexing.ID;
import com.magento.idea.magento2plugin.stubs.indexes.xml.ModuleXmlIndex;
import java.util.ArrayList;
import org.jetbrains.annotations.NotNull;
import java.util.Collection;

public class GetModuleFileUtil {

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
        final Collection<String> moduleRootPaths = FileBasedIndex.getInstance()
                .getValues(ModuleXmlIndex.KEY, moduleName, GlobalSearchScope.allScope(project));
        final Collection<VirtualFile> moduleRoots = new ArrayList<>();

        for (final String moduleRootPath : moduleRootPaths) {
            final VirtualFile moduleRoot = VirtualFileManager.getInstance()
                    .findFileByUrl("file://" + moduleRootPath);

            if (moduleRoot != null) {
                moduleRoots.add(moduleRoot);
            }
        }

        if (moduleRoots.isEmpty()) {
            moduleRoots.addAll(getPhpRegistrationFiles(moduleName, project));
        }

        return moduleRoots;
    }

    @SuppressWarnings({"PMD.AvoidCatchingThrowable", "unchecked"})
    private Collection<VirtualFile> getPhpRegistrationFiles(
            final @NotNull String moduleName,
            final @NotNull Project project
    ) {
        final Collection<VirtualFile> results = new ArrayList<>();

        try {
            final Class<?> moduleNameIndexClass = Class.forName(
                    "com.magento.idea.magento2plugin.stubs.indexes.ModuleNameIndex"
            );
            final ID<String, String> key = (ID<String, String>) moduleNameIndexClass
                    .getField("KEY")
                    .get(null);

            results.addAll(FileBasedIndex.getInstance()
                    .getContainingFiles(key, moduleName, GlobalSearchScope.allScope(project)));
        } catch (Throwable ignored) { //NOPMD
            // PHP is optional for JavaScript navigation; ignore the legacy PHP index when absent.
        }

        return results;
    }
}
