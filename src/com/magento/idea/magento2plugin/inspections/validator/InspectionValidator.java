/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.inspections.validator;

/**
 * All Inspections validators should implement this validator.
 */
public interface InspectionValidator {

    /**
     * Validate if provided value acceptable by concrete validator implementation.
     *
     * @param value String
     *
     * @return boolean
     */
    boolean validate(final String value);
}
