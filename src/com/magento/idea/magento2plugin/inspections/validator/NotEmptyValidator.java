/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.inspections.validator;

public class NotEmptyValidator implements InspectionValidator {

    @Override
    public boolean validate(final String value) {
        return value != null && !value.isEmpty();
    }
}
