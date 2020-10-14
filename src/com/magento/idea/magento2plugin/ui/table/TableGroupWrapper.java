package com.magento.idea.magento2plugin.ui.table;

import javax.swing.CellEditor;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListDataEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class TableGroupWrapper {
    private static final String COLUMN_ACTION = "Action";
    private static final String DELETE_ACTION = "Delete";

    private final JTable table;
    private final JButton addNewRowButton;

    private final Map<String, ActionListener> columnsActionListener;

    /**
     * Constructor.
     *
     * @param tableComponent JTable
     * @param addNewRowButtonComponent JButton
     * @param columns List
     * @param defaultValues Map
     * @param sources Map
     * @param actionListeners Map
     */
    public TableGroupWrapper(
            final JTable tableComponent,
            final JButton addNewRowButtonComponent,
            final List<String> columns,
            final Map<String, String> defaultValues,
            final Map<String, List<String>> sources,
            final Map<String, ActionListener> actionListeners
    ) {
        columnsActionListener = new HashMap<>(actionListeners);
        table = tableComponent;
        addNewRowButton = addNewRowButtonComponent;
        addTableColumns(columns);
        addDeleteButton();
        addNewRowRoutine(columns, defaultValues, sources);
    }

    /**
     * Get table model.
     *
     * @return DefaultTableModel
     */
    public DefaultTableModel getTableModel() {
        return (DefaultTableModel) table.getModel();
    }

    /**
     * Add table columns.
     *
     * @param columns List
     */
    private void addTableColumns(final List<String> columns) {
        final DefaultTableModel model = getTableModel();
        final Object[] columnsObjectArray = new Object[columns.size() + 1];

        for (int index = 0; index < columns.size(); index++) {
            columnsObjectArray[index] = columns.get(index);
        }
        columnsObjectArray[columnsObjectArray.length - 1] = COLUMN_ACTION;
        model.setDataVector(new Object[][] {}, columnsObjectArray);
    }

    /**
     * Add delete button to action column.
     */
    private void addDeleteButton() {
        final TableColumn actionColumn = table.getColumn(COLUMN_ACTION);
        actionColumn.setCellRenderer(new TableButton(DELETE_ACTION));
        actionColumn.setCellEditor(new DeleteRowButton(new JCheckBox()));
    }

    /**
     * Adding new row routine.
     *
     * @param columns List
     * @param defaultValues Map
     * @param sources Map
     */
    private void addNewRowRoutine(
            final List<String> columns,
            final Map<String, String> defaultValues,
            final Map<String, List<String>> sources
    ) {
        final DefaultTableModel model = getTableModel();
        final Object[] columnValuesObjectArray = new Object[columns.size() + 1];

        for (int index = 0; index < columns.size(); index++) {
            if (defaultValues != null && defaultValues.containsKey(columns.get(index))) {
                columnValuesObjectArray[index] = defaultValues.get(columns.get(index));
            } else {
                columnValuesObjectArray[index] = "";
            }
            if (sources != null && sources.containsKey(columns.get(index))) {
                final String column = columns.get(index);
                final List<String> source = new LinkedList<>(sources.get(column));
                final String[] sourceArray = new String[source.size()];

                for (int sIndex = 0; sIndex < source.size(); sIndex++) {
                    sourceArray[sIndex] = source.get(sIndex);
                }
                final TableColumn currentColumn = table.getColumn(column);
                currentColumn.setCellRenderer(new ComboBoxRenderer(sourceArray));
                currentColumn.setCellEditor(
                        new ComboBoxCellEditor<String>(Arrays.asList(sourceArray))
                );

                if (columnValuesObjectArray[index].toString().isEmpty()
                        && !columnValuesObjectArray[index].toString().equals(sourceArray[0])) {
                    columnValuesObjectArray[index] = sourceArray[0];
                }
            }
        }
        columnValuesObjectArray[columnValuesObjectArray.length - 1] = DELETE_ACTION;

        addNewRowButton.addActionListener(event -> {
            model.addRow(columnValuesObjectArray);

            for (int index = 0; index < columns.size(); index++) {
                final String column = columns.get(index);

                if (columnsActionListener.containsKey(column)) {
                    TableCellEditor editor = table.getColumnModel()
                            .getColumn(index)
                            .getCellEditor();

                    if (editor instanceof ComboBoxCellEditor) {
                        ((ComboBoxCellEditor<String>) editor)
                                .addAdditionalActionListener(columnsActionListener.get(column));
                    }
                }
            }
        });
    }
}
