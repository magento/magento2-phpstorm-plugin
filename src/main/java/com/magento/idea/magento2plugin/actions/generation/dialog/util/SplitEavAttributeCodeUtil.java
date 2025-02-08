/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 *
 */

package com.magento.idea.magento2plugin.actions.generation.dialog.util;

import org.jetbrains.annotations.NotNull;

public final class SplitEavAttributeCodeUtil {

    public static final String ATTRIBUTE_SEPARATOR = "_";

    private SplitEavAttributeCodeUtil(){}

    /**
     * Return separated attribute code.
     *
     * @param attributeCode String
     * @return String[]
     */
    @NotNull
    public static String[] execute(final String attributeCode) {
        return attributeCode.split(ATTRIBUTE_SEPARATOR);
    }
}
