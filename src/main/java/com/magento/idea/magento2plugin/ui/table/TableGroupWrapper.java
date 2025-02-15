/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.ui.table;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

public class TableGroupWrapper {
    private static final String COLUMN_ACTION = "Action";
    private static final String DELETE_ACTION = "Delete";

    private final JTable table;
    private final JButton addNewRowButton;
    private final List<String> columns;
    private final Map<String, String> defaultValues;
    private final Map<String, List<String>> sources;

    /**
     * Constructor.
     *
     * @param tableComponent JTable
     * @param addNewRowButtonComponent JButton
     * @param columnsList List
     * @param defaultColumnsValues Map
     * @param columnsSources Map
     */
    public TableGroupWrapper(
            final JTable tableComponent,
            final JButton addNewRowButtonComponent,
            final List<String> columnsList,
            final Map<String, String> defaultColumnsValues,
            final Map<String, List<String>> columnsSources
    ) {
        table = tableComponent;
        addNewRowButton = addNewRowButtonComponent;
        columns = columnsList;
        defaultValues = defaultColumnsValues;
        sources = columnsSources;
    }

    /**
     * Init table group.
     */
    public void initTableGroup() {
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
     * Get table columns data.
     *
     * @return List
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public List<Map<String, String>> getColumnsData() {
        final List<Map<String, String>> data = new LinkedList<>();
        final DefaultTableModel tableModel = getTableModel();

        for (int row = 0; row < tableModel.getRowCount(); row++) {
            final Map<String, String> columnValues = new LinkedHashMap<>();

            for (int columnIndex = 0; columnIndex < columns.size(); columnIndex++) {
                columnValues.put(
                        columns.get(columnIndex),
                        ((String) tableModel.getValueAt(row, columnIndex)).trim()
                );
            }
            data.add(columnValues);
        }

        return data;
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
            columnsObjectArray[index] = getTitleForColumn(columns.get(index));
        }
        columnsObjectArray[columnsObjectArray.length - 1] = COLUMN_ACTION;
        model.setDataVector(new Object[][] {}, columnsObjectArray);
    }

    /**
     * Get title for column.
     *
     * @param column String
     *
     * @return String
     */
    @SuppressWarnings("PMD.UseLocaleWithCaseConversions")
    private String getTitleForColumn(final String column) {
        if (Character.isUpperCase(column.charAt(0))) {
            return column;
        }
        String title = column.replace("_", " ");

        if (title.contains(":")) {
            title = title.substring(title.indexOf(':') + 1);
        }

        return title.substring(0, 1).toUpperCase() + title.substring(1);
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
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
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

                for (int sourceIndex = 0; sourceIndex < source.size(); sourceIndex++) {
                    sourceArray[sourceIndex] = source.get(sourceIndex);
                }
                final TableColumn currentColumn = table.getColumn(getTitleForColumn(column));
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
        });
    }
}
