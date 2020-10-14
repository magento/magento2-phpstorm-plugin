/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.ui.table;

import javax.swing.AbstractCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

public class ComboBoxCellEditor<T> extends AbstractCellEditor
        implements TableCellEditor, ActionListener {
    private T value;
    private List<T> options;
    private JComboBox<T> editorComponent;
    private ActionListener actionListener;

    public ComboBoxCellEditor(List<T> options) {
        this.options = options;
        editorComponent = new JComboBox();

        for (T option : options) {
            editorComponent.addItem(option);
        }
    }

    @Override
    public Component getTableCellEditorComponent(
            JTable table,
            Object value,
            boolean isSelected,
            int row,
            int column
    ) {
        this.value = (T) value;

        editorComponent.setSelectedItem(value);
        editorComponent.addActionListener(this);

        if (isSelected) {
            editorComponent.setBackground(table.getSelectionBackground());
        } else {
            editorComponent.setBackground(table.getSelectionForeground());
        }

        return editorComponent;
    }

    @Override
    public Object getCellEditorValue() {
        return this.value;
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        JComboBox<T> comboBox = (JComboBox<T>) event.getSource();
        this.value = (T) comboBox.getSelectedItem();

        if (actionListener != null) {
            actionListener.actionPerformed(event);
        }
        editorComponent.setPopupVisible(false);
    }

    public Component getComponent() {
        return editorComponent;
    }

    public void addAdditionalActionListener(ActionListener actionListener) {
        this.actionListener = actionListener;
    }
}
