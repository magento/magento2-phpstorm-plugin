/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.ui.table;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class DeleteRowButton extends DefaultCellEditor {
    protected JButton button;

    public DeleteRowButton(JCheckBox checkBox) {
        super(checkBox);
        button = new JButton();
        button.setOpaque(true);
    }

    public Component getTableCellEditorComponent(JTable table, Object value,
                                                 boolean isSelected, int row, int column) {
        ((DefaultTableModel)table.getModel()).removeRow(row);
        return null;
    }
}

