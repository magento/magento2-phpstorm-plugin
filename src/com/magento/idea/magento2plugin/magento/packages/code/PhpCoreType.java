/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.packages.code;

import org.jetbrains.annotations.NotNull;

public enum PhpCoreType {
    EXCEPTION("Exception");

    /**
     * PHP Core type.
     */
    private final String type;

    /**
     * PHP Core type ENUM constructor.
     *
     * @param type String
     */
    PhpCoreType(final @NotNull String type) {
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
}
