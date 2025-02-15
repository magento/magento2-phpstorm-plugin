/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.ui.table;

import java.awt.Component;
import java.util.Arrays;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTable;

public class ComboBoxEditor extends DefaultCellEditor {

    private final DefaultComboBoxModel model;
    private final String[] obtainedList;

    /**
     * Custom ComboBox.
     *
     * @param items String[]
     */
    public ComboBoxEditor(final String... items) {
        super(new JComboBox(items));
        this.model = (DefaultComboBoxModel)((JComboBox)getComponent()).getModel();
        this.obtainedList = Arrays.copyOf(items, items.length);
    }

    @Override
    public Component getTableCellEditorComponent(
            final JTable table,
            final Object value,
            final boolean isSelected,
            final int row,
            final int column
    ) {
        if (column == 0) {
            model.removeAllElements();
            for (final String obtained : obtainedList) {
                model.addElement(obtained);
            }
        } else {

            model.removeAllElements();
            final String selectedItem = (String) table.getValueAt(row, 0);
            for (final String obtained : obtainedList) {
                if (!selectedItem.equals(obtained)) {
                    model.addElement(obtained);
                }
            }
        }

        return super.getTableCellEditorComponent(table, value, isSelected, row, column);
    }
}
