/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.ui.table;

import java.awt.Component;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

public class TableButton extends JButton implements TableCellRenderer {
    private final String defaultValue;

    /**
     * Table button renderer.
     *
     * @param defaultValue String
     */
    public TableButton(final String defaultValue) {
        super();
        this.defaultValue = defaultValue;
        setOpaque(true);
    }

    @Override
    public Component getTableCellRendererComponent(
            final JTable table,
            final Object value,
            final boolean isSelected,
            final boolean hasFocus,
            final int row,
            final int column
    ) {
        setText(defaultValue);
        return this;
    }
}