/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.versioning.processors;

import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

public interface IndexProcessor<T> {

    T process(final @NotNull PsiFile file);
}
