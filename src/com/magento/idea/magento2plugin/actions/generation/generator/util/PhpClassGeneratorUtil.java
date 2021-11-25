/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator.util;

import java.util.Collections;
import java.util.List;

public final class PhpClassGeneratorUtil {

    private PhpClassGeneratorUtil() {}

    /**
     * Formats PHP class uses.
     *
     * @param uses List
     * @return String
     */
    public static String formatUses(final List<String> uses) {
        Collections.sort(uses);

        return String.join(",", uses);
    }

    /**
     * Fetches the class name from a fully qualified name.
     *
     * @param fqn FQN
     * @return String
     */
    public static String getNameFromFqn(final String fqn) {
        final String[] fqnArray = fqn.split("\\\\");

        return fqnArray[fqnArray.length - 1];
    }
}
