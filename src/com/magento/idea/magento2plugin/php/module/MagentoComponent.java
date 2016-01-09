package com.magento.idea.magento2plugin.php.module;

import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by dkvashnin on 1/9/16.
 */
public interface MagentoComponent {
    @Nullable
    ComposerPackageModel getComposerModel();

    boolean isFileInContext(@NotNull PsiFile psiFile);
}
