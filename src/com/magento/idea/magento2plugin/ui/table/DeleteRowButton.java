/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.ui.table;

import java.awt.Component;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class DeleteRowButton extends DefaultCellEditor {
    protected JButton button;

    /**
     * Delete Row editor.
     *
     * @param checkBox JCheckBox
     */
    public DeleteRowButton(final JCheckBox checkBox) {
        super(checkBox);
        button = new JButton();
        button.setOpaque(true);
    }

    @Override
    public Component getTableCellEditorComponent(
            final JTable table,
            final Object value,
            final boolean isSelected,
            final int row,
            final int column
    ) {
        ((DefaultTableModel)table.getModel()).removeRow(row);
        return null;
    }
}

