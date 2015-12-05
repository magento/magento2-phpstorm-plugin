package com.magento.idea.magento2plugin.php.module;

import com.intellij.psi.PsiFile;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Created by dkvashnin on 12/5/15.
 */
public interface MagentoModule {
    @Nullable
    String getMagentoName();

    @Nullable
    List<MagentoModule> getMagentoDependencies();

    @Nullable
    ComposerPackageModel getComposerModel();

    boolean isFileInContext(PsiFile psiFile);

    boolean isClassInContext(PhpClass phpClass);
}
