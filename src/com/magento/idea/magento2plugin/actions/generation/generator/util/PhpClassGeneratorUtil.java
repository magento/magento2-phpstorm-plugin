/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator.util;

import com.jetbrains.php.lang.PhpLangUtil;
import com.magento.idea.magento2plugin.util.RegExUtil;
import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public final class PhpClassGeneratorUtil {

    private PhpClassGeneratorUtil() {}

    /**
     * Formats PHP class uses.
     *
     * @param uses List
     *
     * @return String
     */
    public static String formatUses(final @NotNull List<String> uses) {
        Collections.sort(uses);

        return String.join(",", uses);
    }

    /**
     * Fetches the class name from a fully qualified name.
     *
     * @param fqn FQN
     *
     * @return String
     */
    public static String getNameFromFqn(final @NotNull String fqn) {
        final String[] fqnArray = fqn.split("\\\\");

        return fqnArray[fqnArray.length - 1];
    }

    /**
     * Check if provided string is a valid PHP class FQN.
     *
     * @param fqnCandidate String
     *
     * @return boolean
     */
    public static boolean isValidFqn(final @NotNull String fqnCandidate) {
        return PhpLangUtil.isFqn(fqnCandidate) || fqnCandidate.matches(RegExUtil.PhpRegex.FQN);
    }
}
