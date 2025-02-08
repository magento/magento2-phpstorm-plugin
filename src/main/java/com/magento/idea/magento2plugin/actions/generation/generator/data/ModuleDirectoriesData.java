/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.actions.generation.generator.data;

import com.intellij.psi.PsiDirectory;
import org.jetbrains.annotations.NotNull;

public class ModuleDirectoriesData {
    @NotNull
    private final PsiDirectory moduleDirectory;
    @NotNull
    private final PsiDirectory moduleEtc;

    public ModuleDirectoriesData(@NotNull PsiDirectory moduleDirectory, @NotNull PsiDirectory moduleEtc) {
        this.moduleDirectory = moduleDirectory;
        this.moduleEtc = moduleEtc;
    }

    @NotNull
    public PsiDirectory getModuleDirectory() {
        return moduleDirectory;
    }

    @NotNull
    public PsiDirectory getModuleEtcDirectory() {
        return moduleEtc;
    }
}
