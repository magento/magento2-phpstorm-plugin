/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.util.module;

import com.intellij.json.psi.JsonFile;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.magento.idea.magento2plugin.magento.files.ComposerJson;
import com.magento.idea.magento2plugin.magento.packages.File;
import java.nio.file.Path;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class UctModuleLocatorUtil {

    private static final String UCT_COMPOSER_NAME = "magento\\/upgrade-compatibility-tool";
    private static final String UCT_EXECUTABLE_RELATIVE_PATH = "bin" + File.separator + "uct";

    private UctModuleLocatorUtil() {
    }

    /**
     * Find UCT executable for the specified project.
     *
     * @param project Project
     *
     * @return VirtualFile
     */
    public static @Nullable VirtualFile locateUctExecutable(final @NotNull Project project) {
        final PsiDirectory uctModuleDir = locateModule(project);

        if (uctModuleDir == null) {
            return null;
        }

        return VfsUtil.findFile(
                Path.of(
                        uctModuleDir.getVirtualFile().getPath()
                                + File.separator
                                + UCT_EXECUTABLE_RELATIVE_PATH
                ),
                false
        );
    }

    /**
     * Find UCT module base directory.
     *
     * @param project Project
     *
     * @return PsiDirectory
     */
    @SuppressWarnings({
            "PMD.CognitiveComplexity",
            "PMD.NPathComplexity",
            "PMD.CyclomaticComplexity",
            "PMD.AvoidDeeplyNestedIfStmts"
    })
    public static @Nullable PsiDirectory locateModule(final @NotNull Project project) {
        final String projectBasePath = project.getBasePath();

        if (projectBasePath == null) {
            return null;
        }
        final VirtualFile projectBaseVf = VfsUtil.findFile(Path.of(projectBasePath), false);

        if (projectBaseVf == null) {
            return null;
        }
        final PsiManager psiManager = PsiManager.getInstance(project);
        final PsiDirectory projectBaseDir = psiManager.findDirectory(projectBaseVf);

        if (projectBaseDir == null) {
            return null;
        }
        final PsiFile composerConfigurationFile = projectBaseDir.findFile(ComposerJson.FILE_NAME);

        if (composerConfigurationFile instanceof JsonFile) {
            final boolean hasUctAsProjectDependency = composerConfigurationFile
                    .getText()
                    .contains(UCT_COMPOSER_NAME);

            if (hasUctAsProjectDependency) {
                PsiDirectory uctBaseDir = findSubdirectoryByPath(
                        projectBaseDir,
                        "vendor".concat(File.separator)
                                .concat("magento")
                                .concat(File.separator)
                                .concat("module-upgrade-compatibility-tool")
                );
                if (uctBaseDir == null) {
                    uctBaseDir = findSubdirectoryByPath(
                            projectBaseDir,
                            "app".concat(File.separator)
                                    .concat("code")
                                    .concat(File.separator)
                                    .concat("Magento")
                                    .concat(File.separator)
                                    .concat("UpgradeCompatibilityTool")
                    );
                }
                if (uctBaseDir != null) {
                    return uctBaseDir;
                }
            }

            for (final PsiDirectory subDir : projectBaseDir.getSubdirectories()) {
                final PsiFile composerFile = subDir.findFile(ComposerJson.FILE_NAME);

                if (composerFile == null) {
                    continue;
                }
                final boolean isUctModule = composerFile.getText().contains(UCT_COMPOSER_NAME);

                if (isUctModule) {
                    return subDir;
                }
            }
        }

        return null;
    }

    /**
     * Find subdirectory by path.
     *
     * @param parentDir PsiDirectory
     * @param path String
     *
     * @return PsiDirectory
     */
    private static @Nullable PsiDirectory findSubdirectoryByPath(
            final @NotNull PsiDirectory parentDir,
            final @NotNull String path
    ) {
        final String[] pathParts = path.split(File.separator);
        PsiDirectory lookupDir = parentDir;

        for (final String pathPart : pathParts) {
            if (lookupDir == null) {
                return null;
            }
            lookupDir = lookupDir.findSubdirectory(pathPart);
        }

        return lookupDir;
    }
}
