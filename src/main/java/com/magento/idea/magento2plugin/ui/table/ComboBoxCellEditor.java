/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.ui.table;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;
import javax.swing.AbstractCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

public class ComboBoxCellEditor<T> extends AbstractCellEditor
        implements TableCellEditor, ActionListener {
    private T value;
    private final List<T> options;
    private final JComboBox<T> editorComponent;
    private ActionListener actionListener;

    /**
     * Constructor.
     *
     * @param options List
     */
    public ComboBoxCellEditor(final List<T> options) {
        super();
        this.options = options;
        editorComponent = new JComboBox();

        for (final T option : this.options) {
            editorComponent.addItem(option);
        }
    }

    @Override
    public Component getTableCellEditorComponent(
            final JTable table,
            final Object value,
            final boolean isSelected,
            final int row,
            final int column
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
    public void actionPerformed(final ActionEvent event) {
        final JComboBox<T> comboBox = (JComboBox<T>) event.getSource();
        this.value = (T) comboBox.getSelectedItem();

        if (actionListener != null) {
            actionListener.actionPerformed(event);
        }
        editorComponent.setPopupVisible(false);
    }

    /**
     * Get combobox options.
     *
     * @return List of combobox options.
     */
    public List<T> getOptions() {
        return new LinkedList<>(options);
    }

    /**
     * Get component.
     *
     * @return Component
     */
    public Component getComponent() {
        return editorComponent;
    }

    /**
     * Add additional action listener.
     *
     * @param actionListener ActionListener
     */
    public void addAdditionalActionListener(final ActionListener actionListener) {
        this.actionListener = actionListener;
    }
}
