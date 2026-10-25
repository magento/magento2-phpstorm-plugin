/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.stubs.indexes;

import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

/** Adds event names from optional language support to the shared declaration index. */
public interface EventNameIndexProvider {
    ExtensionPointName<EventNameIndexProvider> EP_NAME = ExtensionPointName.create(
            "com.magento.idea.magento2plugin.eventNameIndexProvider"
    );

    boolean accepts(@NotNull VirtualFile file);

    void collectEventNames(@NotNull PsiFile file, @NotNull Map<String, Void> names);
}
