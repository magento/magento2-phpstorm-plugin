/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.stubs.indexes.data;

import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import java.util.Collection;
import org.jetbrains.annotations.NotNull;

public class PluginData {

    private final String type;
    private final int sortOrder;
    private Collection<PhpClass> phpClassCollection;

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

    public Collection<PhpClass> getPhpClassCollection() {
        return phpClassCollection;
    }

    /**
     * Setting PHP plugin class.
     *
     * @param phpClass collection PHP plugin class
     */
    public void setPhpClass(final @NotNull PhpClass phpClass) {
        final PhpIndex phpIndex = PhpIndex.getInstance(phpClass.getProject());

        phpClassCollection = phpIndex.getClassesByFQN(getType());
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
