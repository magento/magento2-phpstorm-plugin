/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog.validator.annotation;

import com.magento.idea.magento2plugin.bundles.ValidatorBundle;
import java.util.Arrays;
import org.jetbrains.annotations.NotNull;

public final class ValidationMessageExtractorUtil {

    private static final int MIN_MESSAGE_ARRAY_LENGTH = 1;
    private static final ValidatorBundle VALIDATOR_BUNDLE = new ValidatorBundle();

    private ValidationMessageExtractorUtil() {}

    /**
     * Extract validation message from validation annotation.
     *
     * @param annotation FieldValidation
     *
     * @return String
     */
    public static String extract(final @NotNull FieldValidation annotation) {
        String[] params;

        if (annotation.message().length > MIN_MESSAGE_ARRAY_LENGTH) {
            params = Arrays.copyOfRange(annotation.message(), 1, annotation.message().length);
        } else {
            params = new String[]{};
        }

        return VALIDATOR_BUNDLE.message(annotation.message()[0], (Object[]) params);
    }
}
