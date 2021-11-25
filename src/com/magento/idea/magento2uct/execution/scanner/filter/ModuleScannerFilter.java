/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.execution.scanner.filter;

import com.magento.idea.magento2uct.execution.scanner.data.ComponentData;

public interface ModuleScannerFilter {

    /**
     * Implement filtering logic for component.
     *
     * @param component ComponentData
     *
     * @return boolean
     */
    boolean isExcluded(final ComponentData component);
}
