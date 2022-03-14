/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.util.ui.UIUtil;
import com.magento.idea.magento2plugin.actions.generation.InjectConstructorArgumentAction;
import com.magento.idea.magento2plugin.actions.generation.data.xml.DiArrayValueData;
import com.magento.idea.magento2plugin.bundles.ValidatorBundle;
import com.magento.idea.magento2plugin.magento.packages.DiArgumentType;
import com.magento.idea.magento2plugin.ui.table.TableGroupWrapper;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.table.DefaultTableModel;
import org.jetbrains.annotations.NotNull;

public class GatherArrayValuesDialog extends AbstractDialog {

    private static final String ITEM_NAME = "Name";
    private static final String ITEM_TYPE = "Type";
    private static final String ITEM_VALUE = "Value";

    private final @NotNull Project project;// NOPMD
    private final DiArrayValueData arrayValueData;

    private JPanel contentPane;
    private JButton buttonCancel;
    private JButton buttonOK;
    private JPanel itemsPane;// NOPMD
    private JScrollPane itemsScrollPane;// NOPMD
    private JTable itemsTable;
    private JButton buttonAdd;
    private JLabel itemsTableErrorMessage;

    /**
     * Array values gathering dialog constructor.
     *
     * @param project Project
     * @param arrayValueData DiArrayValueData
     */
    public GatherArrayValuesDialog(
            final @NotNull Project project,
            final DiArrayValueData arrayValueData
    ) {
        super();

        this.project = project;
        this.arrayValueData = arrayValueData;

        setContentPane(contentPane);
        setModal(true);
        setTitle(InjectConstructorArgumentAction.GATHER_ARRAY_VALUES_ACTION_DESCRIPTION);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(event -> onOK());
        buttonCancel.addActionListener(event -> onCancel());

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent event) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(
                event -> onCancel(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
        );

        initTable();
        itemsTableErrorMessage.setVisible(false);
        itemsTableErrorMessage.setText("");
    }

    /**
     * Open a new array values gathering dialog.
     *
     * @param project Project
     * @param arrayValueData DiArrayValueData
     */
    public static void open(
            final @NotNull Project project,
            final DiArrayValueData arrayValueData
    ) {
        final GatherArrayValuesDialog dialog = new GatherArrayValuesDialog(project, arrayValueData);
        dialog.pack();
        dialog.centerDialog(dialog);
        dialog.setVisible(true);
    }

    /**
     * Fire process if all fields are valid.
     */
    private void onOK() {
        if (itemsTable.isEditing()) {
            itemsTable.getCellEditor().stopCellEditing();
        }
        final List<DiArrayValueData.DiArrayItemData> items = extractItems(getTableModel());
        final Pair<Boolean, String> validationResult = validateItems(items);

        if (!validationResult.getFirst()) {
            showErrorMessage(validationResult.getSecond());
            return;
        }
        arrayValueData.setItems(items);
        exit();
    }

    @Override
    protected void showErrorMessage(final @NotNull String errorMessage) {
        itemsTableErrorMessage.setVisible(true);
        itemsTableErrorMessage.setFont(UIUtil.getLabelFont(UIUtil.FontSize.SMALL));
        itemsTableErrorMessage.setForeground(new Color(252, 119, 83));
        itemsTableErrorMessage.setText(errorMessage);
    }

    private void initTable() {
        final List<String> columns = new LinkedList<>(Arrays.asList(
                ITEM_NAME,
                ITEM_TYPE,
                ITEM_VALUE
        ));
        final Map<String, List<String>> sources = new HashMap<>();
        sources.put(ITEM_TYPE, DiArgumentType.getValueList());

        final TableGroupWrapper tableGroupWrapper = new TableGroupWrapper(
                itemsTable,
                buttonAdd,
                columns,
                new HashMap<>(),
                sources
        );
        tableGroupWrapper.initTableGroup();
    }

    private Pair<Boolean, String> validateItems(
            final List<DiArrayValueData.DiArrayItemData> items
    ) {
        final ValidatorBundle validatorBundle = new ValidatorBundle();
        final List<String> itemsNames = new ArrayList<>();

        for (final DiArrayValueData.DiArrayItemData item : items) {
            final String name = item.getName().trim();

            if (name.isEmpty()) {
                return new Pair<>(
                        Boolean.FALSE,
                        validatorBundle.message("validator.arrayValuesDialog.nameMustNotBeEmpty")
                );
            }
            itemsNames.add(name);
            final DiArgumentType type = item.getType();
            final String value = item.getValue().trim();

            final boolean isValid = type.isValid(value);

            if (!isValid) {
                return new Pair<>(
                        Boolean.FALSE,
                        validatorBundle.message(
                                "validator.arrayValuesDialog.invalidValueForRowWithName",
                                value,
                                name
                        )
                );
            }
        }

        if (itemsNames.stream().distinct().count() != itemsNames.size()) {
            return new Pair<>(
                    Boolean.FALSE,
                    validatorBundle.message(
                            "validator.arrayValuesDialog.namesMustBeUnique"
                    )
            );
        }

        return new Pair<>(Boolean.TRUE, "");
    }

    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private List<DiArrayValueData.DiArrayItemData> extractItems(
            final DefaultTableModel tableModel
    ) {
        final List<DiArrayValueData.DiArrayItemData> items = new ArrayList<>();

        for (int rowNumber = 0; rowNumber < tableModel.getRowCount(); rowNumber++) {
            final DiArrayValueData.DiArrayItemData item = new DiArrayValueData.DiArrayItemData(
                    tableModel.getValueAt(rowNumber, 0).toString(),
                    DiArgumentType.getByValue(tableModel.getValueAt(rowNumber, 1).toString()),
                    tableModel.getValueAt(rowNumber, 2).toString().trim()
            );

            items.add(item);
        }

        return new ArrayList<>(items);
    }

    /**
     * Get table model.
     *
     * @return DefaultTableModel
     */
    private DefaultTableModel getTableModel() {
        return (DefaultTableModel) itemsTable.getModel();
    }
}
