/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.magento.packages.code;

import com.magento.idea.magento2plugin.actions.generation.generator.util.PhpClassGeneratorUtil;
import org.jetbrains.annotations.NotNull;

public enum ExceptionType {
    COULD_NOT_SAVE("Magento\\Framework\\Exception\\CouldNotSaveException"),
    COULD_NOT_DELETE("Magento\\Framework\\Exception\\CouldNotDeleteException"),
    NO_SUCH_ENTITY_EXCEPTION("Magento\\Framework\\Exception\\NoSuchEntityException");

    /**
     * Magento Exception type.
     */
    private final String type;

    /**
     * Magento Exception type ENUM constructor.
     *
     * @param type String
     */
    ExceptionType(final @NotNull String type) {
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
