package com.magento.idea.magento2plugin.php.module;

import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by dkvashnin on 1/9/16.
 */
public class MagentoComponentImp implements MagentoComponent {
    protected final ComposerPackageModel composerPackageModel;
    protected final PsiDirectory directory;

    public MagentoComponentImp(ComposerPackageModel composerPackageModel, PsiDirectory directory) {
        this.composerPackageModel = composerPackageModel;
        this.directory = directory;
    }

    @Nullable
    @Override
    public ComposerPackageModel getComposerModel() {
        return composerPackageModel;
    }

    @Override
    public boolean isFileInContext(@NotNull PsiFile psiFile) {
        PsiDirectory containingDirectory = psiFile.getOriginalFile().getContainingDirectory();
        while (containingDirectory != null) {
            if (containingDirectory.getManager().areElementsEquivalent(containingDirectory, directory)) {
                return true;
            }

            containingDirectory = containingDirectory.getParentDirectory();
        }

        return false;
    }

}
