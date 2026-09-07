/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.versioning.processors;

import com.intellij.psi.PsiFile;
import com.magento.idea.magento2uct.packages.SupportedVersion;
import org.jetbrains.annotations.NotNull;

public interface IndexProcessor {

    void clearData();

    void process(final @NotNull PsiFile file);

    void save(final @NotNull String basePath, final @NotNull SupportedVersion indexedVersion);
}
