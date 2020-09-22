/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.ui.table;

import javax.swing.*;
import java.awt.*;

public class ComboBoxEditor extends DefaultCellEditor {
    private DefaultComboBoxModel model;

    private String[] obtainedList;

    public ComboBoxEditor(String[] items) {
        super(new JComboBox(items));
        this.model = (DefaultComboBoxModel)((JComboBox)getComponent()).getModel();
        obtainedList = items;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        if(column == 0) {
            model.removeAllElements();
            for(int i = 0; i < obtainedList.length; i++) {
                model.addElement(obtainedList[i]);
            }
        } else {

            model.removeAllElements();
            String selectedItem = (String) table.getValueAt(row, 0);
            for(int i = 0; i < obtainedList.length; i++) {
                if(!selectedItem.equals(obtainedList[i]))
                    model.addElement(obtainedList[i]);
            }
        }

        return super.getTableCellEditorComponent(table, value, isSelected, row, column);
    }
}
