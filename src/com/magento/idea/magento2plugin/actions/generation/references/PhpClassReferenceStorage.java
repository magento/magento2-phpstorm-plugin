/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.references;

import gnu.trove.THashMap;
import java.util.Map;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PhpClassReferenceStorage {

    private final Map<String, String> myReferences = new THashMap<>();

    /**
     * Process reference.
     *
     * @param name String
     * @param fqn String
     */
    protected void processReference(
            final @NotNull String name,
            final @NotNull String fqn
    ) {
        this.myReferences.put(name, fqn);
    }

    /**
     * Get from resolved references type FQN by name.
     *
     * @param name String
     *
     * @return String
     */
    public @Nullable String getFqnByName(final @NotNull String name) {
        return this.myReferences.get(name);
    }

    /**
     * Get all names for resolved complex PHP types.
     *
     * @return Set[String]
     */
    public Set<String> getNames() {
        return this.myReferences.keySet();
    }
}
