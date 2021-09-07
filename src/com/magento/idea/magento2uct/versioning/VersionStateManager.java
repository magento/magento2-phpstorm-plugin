/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.versioning;

import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2uct.versioning.indexes.DeprecationStateIndex;
import org.jetbrains.annotations.NotNull;

public final class VersionStateManager {

    private static final VersionStateManager INSTANCE = new VersionStateManager();
    private final DeprecationStateIndex deprecationStateIndex;

    public static VersionStateManager getInstance() {
        return INSTANCE;
    }

    private VersionStateManager() {
        deprecationStateIndex = new DeprecationStateIndex();
    }

    /**
     * Check if PHP class is deprecated.
     *
     * @param phpClass PhpClass
     *
     * @return boolean
     */
    public boolean isDeprecated(final @NotNull PhpClass phpClass) {
        return deprecationStateIndex.has(phpClass);
    }

    /**
     * Check if PHP method is deprecated.
     *
     * @param method Method
     *
     * @return boolean
     */
    public boolean isDeprecated(final @NotNull Method method) {
        return deprecationStateIndex.has(method);
    }

    /**
     * Check if PHP field is deprecated.
     *
     * @param field Field
     *
     * @return boolean
     */
    public boolean isDeprecated(final @NotNull Field field) {
        return deprecationStateIndex.has(field);
    }

    /**
     * Check if specified FQN exists in the deprecation index.
     *
     * @param fqn String
     *
     * @return boolean
     */
    public boolean isDeprecated(final @NotNull String fqn) {
        return deprecationStateIndex.has(fqn);
    }
}
