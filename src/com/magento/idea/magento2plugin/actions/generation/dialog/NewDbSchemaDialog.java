/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.magento.idea.magento2plugin.actions.generation.NewDbSchemaAction;
import com.magento.idea.magento2plugin.actions.generation.data.DbSchemaXmlData;
import com.magento.idea.magento2plugin.actions.generation.data.DbSchemaXmlSourceData;
import com.magento.idea.magento2plugin.actions.generation.data.ui.ComboBoxItemData;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.DefaultCellEditor;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;

import com.magento.idea.magento2plugin.actions.generation.dialog.validator.annotation.FieldValidation;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.annotation.RuleRegistry;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.AlphanumericWithUnderscoreRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.Lowercase;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.NotEmptyRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.TableNameLength;
import com.magento.idea.magento2plugin.actions.generation.generator.DbSchemaXmlGenerator;
import com.magento.idea.magento2plugin.magento.files.ModuleDbSchemaXml;
import com.magento.idea.magento2plugin.ui.table.ComboBoxCellEditor;
import com.magento.idea.magento2plugin.ui.table.TableGroupWrapper;
import com.magento.idea.magento2plugin.util.magento.GetModuleNameByDirectoryUtil;
import org.jetbrains.annotations.NotNull;

public class NewDbSchemaDialog extends AbstractDialog {
    private static final String TABLE_NAME = "Table Name";

    // Table Columns
    private static final String COLUMN_TYPE = "Type";
    private static final String COLUMN_NAME = "Name";
    private static final String COLUMN_PADDING = "Padding";
    private static final String COLUMN_UNSIGNED = "Unsigned";
    private static final String COLUMN_NULLABLE = "Nullable";
    private static final String COLUMN_IDENTITY = "Identity";
    private static final String COLUMN_COMMENT = "Comment";
    private static final String COLUMN_LENGTH = "Length";
    private static final String COLUMN_PRECISION = "Precision";
    private static final String COLUMN_SCALE = "Scale";
    private static final String COLUMN_DEFAULT = "Default";
    private static final String COLUMN_ON_CREATE = "On Create";
    private static final String COLUMN_ON_UPDATE = "On Update";

    private final Project project;
    private final String moduleName;
    private final PsiDirectory directory;
    private JPanel contentPanel;

    // Buttons
    private JButton buttonOK;
    private JButton buttonCancel;

    // Fields
    @FieldValidation(rule = RuleRegistry.NOT_EMPTY, message = {NotEmptyRule.MESSAGE, TABLE_NAME})
    @FieldValidation(rule = RuleRegistry.LOWERCASE, message = {Lowercase.MESSAGE, TABLE_NAME})
    @FieldValidation(
            rule = RuleRegistry.ALPHANUMERIC_WITH_UNDERSCORE,
            message = {AlphanumericWithUnderscoreRule.MESSAGE, TABLE_NAME}
    )
    @FieldValidation(
            rule = RuleRegistry.TABLE_NAME_LENGTH,
            message = {TableNameLength.MESSAGE}
    )
    private JTextField tableName;

    private JTextField tableComment;
    private JComboBox<ComboBoxItemData> tableEngine;
    private JComboBox<ComboBoxItemData> tableResource;

    // Table Columns UI components group
    private JTable columnsTable;
    private JButton addColumnButton;
    private JScrollPane columnsScrollPanel;//NOPMD

    // Table Constraints UI components group
    private JTable constraintsTable;
    private JButton addConstraintButton;
    private JScrollPane constraintsScrollPanel;//NOPMD

    // Labels
    private JLabel tableNameLabel;//NOPMD
    private JLabel tableEngineLabel;//NOPMD
    private JLabel tableResourceLabel;//NOPMD
    private JLabel tableCommentLabel;//NOPMD
    private JLabel tableColumnsLabel;//NOPMD
    private JLabel tableConstraintsLabel;//NOPMD

    /**
     * Constructor.
     *
     * @param project Project
     * @param directory PsiDirectory
     */
    public NewDbSchemaDialog(
            final @NotNull Project project,
            final @NotNull PsiDirectory directory
    ) {
        super();
        this.project = project;
        this.directory = directory;
        moduleName = GetModuleNameByDirectoryUtil.execute(directory, project);

        setTitle(NewDbSchemaAction.ACTION_DESCRIPTION);
        setContentPane(contentPanel);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        buttonOK.addActionListener(e -> onOK());
        buttonCancel.addActionListener(e -> onCancel());

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent event) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPanel.registerKeyboardAction(
                event -> onCancel(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
        );
        fillComboBoxes();
        initializeColumnsUiComponentGroup();
    }

    /**
     * On buttonOK action listener.
     */
    private void onOK() {
        if (!validateFormFields()) {
            return;
        }
        generateDbSchemaXmlFile();

        this.setVisible(false);
    }

    /**
     * Open new declarative schema dialog.
     *
     * @param project Project
     * @param directory PsiDirectory
     */
    public static void open(
            final @NotNull Project project,
            final @NotNull PsiDirectory directory
    ) {
        final NewDbSchemaDialog dialog = new NewDbSchemaDialog(project, directory);
        dialog.pack();
        dialog.centerDialog(dialog);
        dialog.setVisible(true);
    }

    private void generateDbSchemaXmlFile() {
        new DbSchemaXmlGenerator(
                new DbSchemaXmlData(
                        getTableName(),
                        getTableResource(),
                        getTableEngine(),
                        getTableComment()
                ),
                project,
                moduleName
        ).generate(NewDbSchemaAction.ACTION_NAME, false);
    }

    private void initializeColumnsUiComponentGroup() {
        final List<String> columns = new LinkedList<>(Arrays.asList(
                COLUMN_TYPE,
                COLUMN_NAME,
                COLUMN_PADDING,
                COLUMN_UNSIGNED,
                COLUMN_NULLABLE,
                COLUMN_IDENTITY,
                COLUMN_LENGTH,
                COLUMN_PRECISION,
                COLUMN_SCALE,
                COLUMN_DEFAULT,
                COLUMN_ON_CREATE,
                COLUMN_ON_UPDATE,
                COLUMN_COMMENT
        ));
        // Set default values for columns
        final Map<String, String> defaultValues = new HashMap<>();
        defaultValues.put(COLUMN_NULLABLE, "false");
        // Set sources for columns
        final Map<String, List<String>> sources = new HashMap<>();
        final List<String> booleanSource = Arrays.asList("true", "false");
        sources.put(COLUMN_TYPE, DbSchemaXmlSourceData.getColumnTypes());
        sources.put(COLUMN_UNSIGNED, booleanSource);
        sources.put(COLUMN_NULLABLE, booleanSource);
        sources.put(COLUMN_IDENTITY, booleanSource);
        sources.put(COLUMN_ON_CREATE, booleanSource);
        sources.put(COLUMN_ON_UPDATE, booleanSource);
        // Set action listeners for columns
        final Map<String, ActionListener> actionListeners = new HashMap<>();
        actionListeners.put(COLUMN_TYPE, event -> {
            if (event.getActionCommand().equals("comboBoxChanged")) {
                if (event.getSource() instanceof JComboBox) {
                    final JComboBox source = (JComboBox) event.getSource();
                    final JTable table = (JTable) source.getParent();
                    final String value = source.getSelectedItem().toString();

                    if (!value.isEmpty()) {
                        final int editingRow = ((JTable) source.getParent()).getEditingRow();
                        // Index should starts from the `1` to not consider xsi:type attribute
                        for (int index = 1; index < columns.size(); index++) {
                            TableCellEditor cellEditor = table.getCellEditor(editingRow, index);
                            JComponent component = null;

                            if (cellEditor instanceof DefaultCellEditor) {
                                component = (JComponent) ((DefaultCellEditor) cellEditor)
                                        .getComponent();
                            } else if (cellEditor instanceof ComboBoxCellEditor) {
                                component = (JComponent) ((ComboBoxCellEditor) cellEditor)
                                        .getComponent();
                            }

                            if (component instanceof JComboBox) {
                                component.setEnabled(ModuleDbSchemaXml.getAllowedAttributes(value)
                                        .contains(columns.get(index).toLowerCase()));
                            } else if (component instanceof JTextField) {
                                ((JTextField) component).setEditable(
                                        ModuleDbSchemaXml.getAllowedAttributes(value)
                                        .contains(columns.get(index).toLowerCase())
                                );
                            }
                        }
                    }
                }
            }
        });
        // Initialize new Table Group
        TableGroupWrapper tableGroupWrapper = new TableGroupWrapper(
                columnsTable,
                addColumnButton,
                columns,
                defaultValues,
                sources,
                actionListeners
        );
    }

    /**
     * Fill ComboBoxes ui components with predefined constant values.
     */
    private void fillComboBoxes() {
        // Table Engine ComboBox defaults.
        for (final String engine : DbSchemaXmlSourceData.getTableEngineSource()) {
            tableEngine.addItem(new ComboBoxItemData(engine, engine));
        }
        // Table Resource ComboBox defaults.
        for (final String resource : DbSchemaXmlSourceData.getTableResourceSource()) {
            tableResource.addItem(new ComboBoxItemData(resource, resource));
        }
    }

    /**
     * Get tableName field value.
     *
     * @return String
     */
    private String getTableName() {
        return tableName.getText().trim();
    }

    /**
     * Get tableResource field value.
     *
     * @return String
     */
    private String getTableResource() {
        return tableResource.getSelectedItem().toString().trim();
    }

    /**
     * Get tableEngine field value.
     *
     * @return String
     */
    private String getTableEngine() {
        return tableEngine.getSelectedItem().toString().trim();
    }

    /**
     * Get tableComment field value.
     *
     * @return String
     */
    private String getTableComment() {
        return tableComment.getText().trim();
    }
}
