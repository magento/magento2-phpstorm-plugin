/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.packages.code;

import com.magento.idea.magento2plugin.actions.generation.generator.util.PhpClassGeneratorUtil;
import org.jetbrains.annotations.NotNull;

public enum BackendModuleType {
    CONTEXT("Magento\\Backend\\App\\Action\\Context"),
    EXTENDS("Magento\\Backend\\App\\Action"),
    RESULT_PAGE("Magento\\Backend\\Model\\View\\Result\\Page");

    /**
     * Backend module type.
     */
    private final String type;

    /**
     * Backend module type ENUM constructor.
     *
     * @param type String
     */
    BackendModuleType(final @NotNull String type) {
        this.type = type;
    }

    /**
     * Get type.
     *
     * @return String
     */
    public @NotNull String getType() {
        return type;
    }

    /**
     * Get name from type FQN.
     *
     * @return String
     */
    public @NotNull String getTypeName() {
        return PhpClassGeneratorUtil.getNameFromFqn(getType());
    }
}
