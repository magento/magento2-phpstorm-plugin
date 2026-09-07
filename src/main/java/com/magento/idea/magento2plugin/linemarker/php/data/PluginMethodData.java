/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.linemarker.php.data;

import com.jetbrains.php.lang.psi.elements.Method;

public class PluginMethodData {

    private final Method method;
    private final int sortOrder;
    private final int moduleSequence;

    /**
     * Plugin method data class.
     *
     * @param method Plugin method
     * @param sortOrder Plugin SortOrder integer value
     * @param moduleSequence PHP class placed module sequence integer value
     */
    public PluginMethodData(final Method method, final int sortOrder, final int moduleSequence) {
        this.method = method;
        this.sortOrder = sortOrder;
        this.moduleSequence = moduleSequence;
    }

    public Method getMethod() {
        return method;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public int getModuleSequence() {
        return moduleSequence;
    }

    public String getMethodName() {
        return method.getName();
    }
}
