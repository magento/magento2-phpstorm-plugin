/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.util.php;

import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;

public final class MagentoReferenceUtil {

    public static final String PHP_REFERENCE_SEPARATOR = "::";

    private MagentoReferenceUtil() {
    }

    /**
     * Check if reference looks like a PHP reference.
     *
     * @param referenceCandidate String
     *
     * @return boolean
     */
    public static boolean isReference(final @NotNull String referenceCandidate) {
        return referenceCandidate.contains(PHP_REFERENCE_SEPARATOR)
                && referenceCandidate.split(PHP_REFERENCE_SEPARATOR).length == 2;
    }

    /**
     * Check if reference looks like a PHP method reference.
     *
     * @param referenceCandidate String
     *
     * @return boolean
     */
    public static boolean isMethodReference(final @NotNull String referenceCandidate) {
        if (isReference(referenceCandidate)) {
            final String referencePart = referenceCandidate.split(PHP_REFERENCE_SEPARATOR)[1];

            return StringUtil.isJavaIdentifier(referencePart)
                    && !isConstantReference(referenceCandidate);
        }

        return false;
    }

    /**
     * Check if reference looks like a PHP constant reference.
     *
     * @param referenceCandidate String
     *
     * @return boolean
     */
    public static boolean isConstantReference(final @NotNull String referenceCandidate) {
        if (isReference(referenceCandidate)) {
            final String referencePart = referenceCandidate.split(PHP_REFERENCE_SEPARATOR)[1];

            return StringUtil.isUpperCase(referencePart.replace("_", ""));
        }

        return false;
    }
}
