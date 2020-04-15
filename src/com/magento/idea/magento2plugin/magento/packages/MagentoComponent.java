/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.magento.packages;

import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

public interface MagentoComponent {

    ComposerPackageModel getComposerModel();

    PsiDirectory getDirectory();

    boolean isFileInContext(@NotNull PsiFile psiFile);
}
