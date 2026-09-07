/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.stubs.indexes.data;

public final class PluginData {

    private final String type;
    private final int sortOrder;

    /**
     * Plugin data class.
     *
     * @param type Type class
     * @param sortOrder Sort order value.
     */
    public PluginData(final String type, final int sortOrder) {
        this.type = type;
        this.sortOrder = sortOrder;
    }

    public String getType() {
        return type;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    /**
     * Overridden hashCode check.
     *
     * @return boolean
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.sortOrder;

        return prime * result + ((this.getType() == null) ? 0 : this.getType().hashCode());
    }

    /**
     * Overridden quality check.
     *
     * @param object PluginData
     *
     * @return boolean
     */
    @Override
    public boolean equals(final Object object) {
        if (!(object instanceof PluginData)) {
            return false;
        }
        final PluginData compareTo = (PluginData) object;

        return type.equals(compareTo.getType()) && sortOrder == compareTo.getSortOrder();
    }
}
