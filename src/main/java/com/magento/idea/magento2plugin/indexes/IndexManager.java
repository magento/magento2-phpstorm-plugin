/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.indexes;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.indexing.FileBasedIndex;
import com.intellij.util.indexing.ID;
import com.intellij.util.indexing.FileBasedIndexExtension;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"PMD.ClassNamingConventions", "PMD.UseUtilityClass"})
public class IndexManager {
    private static final Logger LOGGER = Logger.getInstance(IndexManager.class);

    /**
     * Refresh Magento 2 indexes.
     */
    public static void manualReindex() {
        LOGGER.info("Magento manual reindex requested");
        final List<ID<?, ?>> indexIds = getRegisteredIndexIds();

        for (final ID<?, ?> id : indexIds) {
            try {
                FileBasedIndex.getInstance().requestRebuild(id);
                LOGGER.info("Magento index rebuild requested: " + id.getName());
            } catch (NullPointerException exception) { //NOPMD
                LOGGER.info("Magento index is not registered in this IDE, skipping rebuild: " + id.getName());
            } catch (RuntimeException exception) { //NOPMD
                LOGGER.warn("Unable to request Magento index rebuild: " + id.getName(), exception);
            }
        }
        LOGGER.info("Magento manual reindex request completed");
    }

    /** Rebuilds active registrations without static references to optional language implementations. */
    public static List<ID<?, ?>> getRegisteredIndexIds() {
        final List<ID<?, ?>> ids = new ArrayList<>();
        for (final FileBasedIndexExtension<?, ?> extension
                : FileBasedIndexExtension.EXTENSION_POINT_NAME.getExtensionList()) {
            final Class<?> indexClass = extension.getClass();
            if (!indexClass.getName().startsWith("com.magento.idea.magento2plugin.stubs.indexes.")) {
                continue;
            }
            try {
                // KEY is our public constant. The platform's getName() is override-only.
                ids.add((ID<?, ?>) indexClass.getField("KEY").get(null));
            } catch (ReflectiveOperationException exception) {
                LOGGER.warn("Magento index registration has no public KEY: " + indexClass.getName(), exception);
            }
        }
        return ids;
    }
}
