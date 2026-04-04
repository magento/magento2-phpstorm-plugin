/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator.util;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.application.WriteAction;
import com.intellij.psi.PsiDirectory;
import com.magento.idea.magento2plugin.actions.generation.generator.data.ModuleDirectoriesData;
import com.magento.idea.magento2plugin.magento.packages.File;
import com.magento.idea.magento2plugin.magento.packages.Package;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;
import org.jetbrains.annotations.NotNull;

public class DirectoryGenerator {
    private static DirectoryGenerator instance;

    /**
     * Get Singleton.
     *
     * @return this
     */
    public static DirectoryGenerator getInstance() {
        if (null == instance) { //NOPMD
            instance = new DirectoryGenerator();
        }
        return instance;
    }

    /**
     * Create or find module directories.
     *
     * @param packageName String
     * @param moduleName String
     * @param baseDirectory PsiDirectory
     * @return ModuleDirectoriesData
     */
    public ModuleDirectoriesData createOrFindModuleDirectories(
            final @NotNull String packageName,
            final @NotNull String moduleName,
            final @NotNull PsiDirectory baseDirectory
    ) {
        final PsiDirectory packageDirectory = findOrCreateSubdirectory(baseDirectory, packageName);
        final PsiDirectory moduleDirectory = findOrCreateSubdirectory(packageDirectory, moduleName);
        final PsiDirectory moduleEtcDirectory = findOrCreateSubdirectory(
                moduleDirectory,
                Package.moduleBaseAreaDir
        );
        return new ModuleDirectoriesData(moduleDirectory, moduleEtcDirectory);
    }

    /**
     * Find or create subdirectory.
     *
     * @param parent PsiDirectory
     * @param subdirName String
     * @return PsiDirectory
     */
    public PsiDirectory findOrCreateSubdirectory(
            final @NotNull PsiDirectory parent,
            final @NotNull String subdirName
    ) {
        final PsiDirectory sub = ReadAction.compute(() -> parent.findSubdirectory(subdirName));
        return sub == null ? createSubdirectoryOnEdt(parent, subdirName) : sub;
    }

    /**
     * Find or create subdirectories.
     *
     * @param parent PsiDirectory
     * @param subdirPath String
     *
     * @return PsiDirectory
     */
    public PsiDirectory findOrCreateSubdirectories(
            final @NotNull PsiDirectory parent,
            final @NotNull String subdirPath
    ) {
        PsiDirectory lastDirectory = null;
        final String[] directories = subdirPath.split(File.separator);

        for (final String directory : Arrays.asList(directories)) {
            if (lastDirectory == null) {
                final PsiDirectory subDir = ReadAction.compute(() -> parent.findSubdirectory(directory));

                if (subDir == null) {
                    lastDirectory = createSubdirectoryOnEdt(parent, directory);
                } else {
                    lastDirectory = subDir;
                }
            } else {
                final PsiDirectory currentDirectory = lastDirectory;
                final PsiDirectory subDir = ReadAction.compute(() ->
                        currentDirectory.findSubdirectory(directory));

                if (subDir == null) {
                    final PsiDirectory finalLastDirectory = lastDirectory;
                    lastDirectory = createSubdirectoryOnEdt(finalLastDirectory, directory);
                } else {
                    lastDirectory = subDir;
                }
            }
        }

        return lastDirectory;
    }

    private PsiDirectory createSubdirectoryOnEdt(
            final @NotNull PsiDirectory parent,
            final @NotNull String subdirName
    ) {
        if (ApplicationManager.getApplication().isDispatchThread()) {
            return WriteAction.compute(() -> parent.createSubdirectory(subdirName));
        }

        final AtomicReference<PsiDirectory> result = new AtomicReference<>();
        final AtomicReference<RuntimeException> error = new AtomicReference<>();
        ApplicationManager.getApplication().invokeAndWait(() -> {
            try {
                result.set(WriteAction.compute(() -> parent.createSubdirectory(subdirName)));
            } catch (final RuntimeException exception) {
                error.set(exception);
            }
        });

        if (error.get() != null) {
            throw error.get();
        }
        return result.get();
    }
}
