/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.application.WriteAction;
import com.intellij.psi.PsiDirectory;
import com.magento.idea.magento2plugin.actions.generation.generator.data.ModuleDirectoriesData;
import org.jetbrains.annotations.NotNull;

public class DirectoryGenerator {
    private static DirectoryGenerator INSTANCE = null;

    public static DirectoryGenerator getInstance() {
        if (null == INSTANCE) {
            INSTANCE = new DirectoryGenerator();
        }
        return INSTANCE;
    }

    public ModuleDirectoriesData createModuleDirectories(@NotNull String packageName, @NotNull String moduleName, @NotNull PsiDirectory baseDirectory){
        PsiDirectory packageDirectory = findOrCreateSubdirectory(baseDirectory, packageName);
        PsiDirectory moduleDirectory = findOrCreateSubdirectory(packageDirectory, moduleName);
        PsiDirectory moduleEtcDirectory = findOrCreateSubdirectory(moduleDirectory, "etc");
        return new ModuleDirectoriesData(moduleDirectory, moduleEtcDirectory);
    }

    public PsiDirectory findOrCreateSubdirectory(@NotNull PsiDirectory parent, @NotNull String subdirName) {
        final PsiDirectory sub = parent.findSubdirectory(subdirName);
        return sub == null ? WriteAction.compute(() -> parent.createSubdirectory(subdirName)) : sub;
    }
}
