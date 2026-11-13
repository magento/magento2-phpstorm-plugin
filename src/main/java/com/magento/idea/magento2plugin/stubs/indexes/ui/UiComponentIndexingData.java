/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.stubs.indexes.ui;

import com.intellij.openapi.util.Key;
import com.intellij.util.indexing.FileContent;
import com.magento.idea.magento2plugin.stubs.indexes.ui.data.UiComponentNavigationData;
import com.magento.idea.magento2plugin.util.magento.ui.UiComponentScopeResolver;
import java.util.List;
import org.jetbrains.annotations.NotNull;

/** Shares extracted values only for the lifetime of an indexing content snapshot. */
final class UiComponentIndexingData {
    private static final Key<List<UiComponentNavigationData>> DECLARATIONS =
            Key.create("magento.ui.component.indexing.declarations");

    private UiComponentIndexingData() {
    }

    static @NotNull List<UiComponentNavigationData> getDeclarations(final @NotNull FileContent content) {
        // Different files never share a lock or retain one another's PSI. Publish only a complete
        // immutable result, so cancellation leaves this snapshot available for a later retry.
        synchronized (content) {
            List<UiComponentNavigationData> declarations = content.getUserData(DECLARATIONS);
            if (declarations == null) {
                declarations = List.copyOf(UiComponentScopeResolver.getInstance()
                        .collectComponentDeclarations(content.getPsiFile()));
                content.putUserData(DECLARATIONS, declarations);
            }
            return declarations;
        }
    }
}
