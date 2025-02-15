/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule;

/**
 * Interface for all dialog validation rules where value has a string type.
 */
public interface ValidationRule {

    /**
     * Check whether the value passes the validation rule.
     *
     * @param value String
     *
     * @return boolean
     */
    boolean check(String value);
}
