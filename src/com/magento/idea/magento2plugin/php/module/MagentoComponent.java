/**
 * Copyright © Dmytro Kvashnin. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.php.module;

import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

/**
 * Created by dkvashnin on 1/9/16.
 */
public interface MagentoComponent {

    ComposerPackageModel getComposerModel();

    PsiDirectory getDirectory();

    boolean isFileInContext(@NotNull PsiFile psiFile);
}
