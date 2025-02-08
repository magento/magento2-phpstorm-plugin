/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator.pool;

public interface GeneratorRunnerValidator {

    /**
     * Check if generator should be executed in the chain.
     *
     * @return boolean
     */
    boolean validate();
}
