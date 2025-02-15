/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.actions.generation.NewDbSchemaAction;
import com.magento.idea.magento2plugin.actions.generation.data.DbSchemaXmlData;
import com.magento.idea.magento2plugin.actions.generation.data.ui.ComboBoxItemData;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.annotation.FieldValidation;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.annotation.RuleRegistry;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.AlphanumericWithUnderscoreRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.Lowercase;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.NotEmptyRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.TableNameLength;
import com.magento.idea.magento2plugin.actions.generation.generator.DbSchemaWhitelistJsonGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.DbSchemaXmlGenerator;
import com.magento.idea.magento2plugin.magento.packages.database.ColumnAttributes;
import com.magento.idea.magento2plugin.magento.packages.database.TableColumnTypes;
import com.magento.idea.magento2plugin.magento.packages.database.TableEngines;
import com.magento.idea.magento2plugin.magento.packages.database.TableResources;
import com.magento.idea.magento2plugin.ui.table.TableGroupWrapper;
import com.magento.idea.magento2plugin.util.magento.GetModuleNameByDirectoryUtil;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"PMD.TooManyFields", "PMD.ExcessiveImports"})
public class NewDbSchemaDialog extends AbstractDialog {

    private static final String TABLE_NAME = "Table Name";

    private final Project project;
    private final String moduleName;
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
    private TableGroupWrapper columnsTableGroupWrapper;
    private JTable columnsTable;
    private JButton addColumnButton;
    private JScrollPane columnsScrollPanel;//NOPMD

    // Labels
    private JLabel tableNameLabel;//NOPMD
    private JLabel tableEngineLabel;//NOPMD
    private JLabel tableResourceLabel;//NOPMD
    private JLabel tableCommentLabel;//NOPMD
    private JLabel tableColumnsLabel;//NOPMD
    private JLabel tableNameErrorMessage;//NOPMD

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

        addComponentListener(new FocusOnAFieldListener(() -> tableName.requestFocusInWindow()));
    }

    /**
     * On buttonOK action listener.
     */
    private void onOK() {
        if (columnsTable.isEditing()) {
            columnsTable.getCellEditor().stopCellEditing();
        }

        if (validateFormFields()) {
            final DbSchemaXmlData dbSchemaXmlData = new DbSchemaXmlData(
                    getTableName(),
                    getTableResource(),
                    getTableEngine(),
                    getTableComment(),
                    getColumns()
            );
            final PsiFile dbSchemaXmlFile = generateDbSchemaXmlFile(dbSchemaXmlData);

            if (dbSchemaXmlFile == null) {
                return;
            }
            generateWhitelistJsonFile(dbSchemaXmlData);
            exit();
        }
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

    /**
     * Run db_schema.xml file generator.
     *
     * @param dbSchemaXmlData DbSchemaXmlData
     */
    private PsiFile generateDbSchemaXmlFile(final @NotNull DbSchemaXmlData dbSchemaXmlData) {
        return new DbSchemaXmlGenerator(
                dbSchemaXmlData,
                project,
                moduleName
        ).generate(NewDbSchemaAction.ACTION_NAME, false);
    }

    /**
     * Run db_schema_whitelist.json file generator.
     *
     * @param dbSchemaXmlData DbSchemaXmlData
     */
    private void generateWhitelistJsonFile(final @NotNull DbSchemaXmlData dbSchemaXmlData) {
        new DbSchemaWhitelistJsonGenerator(
                project,
                dbSchemaXmlData,
                moduleName
        ).generate(NewDbSchemaAction.ACTION_NAME, false);
    }

    /**
     * Initialize columns ui component.
     */
    private void initializeColumnsUiComponentGroup() {
        final List<String> columns = new LinkedList<>(Arrays.asList(
                ColumnAttributes.TYPE.getName(),
                ColumnAttributes.NAME.getName(),
                ColumnAttributes.PADDING.getName(),
                ColumnAttributes.UNSIGNED.getName(),
                ColumnAttributes.NULLABLE.getName(),
                ColumnAttributes.IDENTITY.getName(),
                ColumnAttributes.LENGTH.getName(),
                ColumnAttributes.PRECISION.getName(),
                ColumnAttributes.SCALE.getName(),
                ColumnAttributes.ON_UPDATE.getName(),
                ColumnAttributes.DEFAULT.getName(),
                ColumnAttributes.COMMENT.getName()
        ));
        // Set default values for columns
        final Map<String, String> defaultValues = new HashMap<>();
        defaultValues.put(ColumnAttributes.NULLABLE.getName(), "false");
        defaultValues.put(ColumnAttributes.IDENTITY.getName(), "false");
        // Set sources for columns
        final Map<String, List<String>> sources = new HashMap<>();
        final List<String> booleanSource = Arrays.asList("true", "false");
        final List<String> columnTypes = TableColumnTypes.getTableColumnTypesList();
        columnTypes.add(0, "");
        sources.put(
                ColumnAttributes.TYPE.getName(),
                columnTypes
        );
        sources.put(ColumnAttributes.UNSIGNED.getName(), booleanSource);
        sources.put(ColumnAttributes.NULLABLE.getName(), booleanSource);
        sources.put(ColumnAttributes.IDENTITY.getName(), booleanSource);
        sources.put(ColumnAttributes.ON_UPDATE.getName(), booleanSource);
        // Initialize new Table Group
        columnsTableGroupWrapper = new TableGroupWrapper(
                columnsTable,
                addColumnButton,
                columns,
                defaultValues,
                sources
        );
        columnsTableGroupWrapper.initTableGroup();
    }

    /**
     * Fill ComboBoxes ui components with predefined constant values.
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private void fillComboBoxes() {
        // Table Engine ComboBox defaults.
        for (final String engine : TableEngines.getTableEnginesList()) {
            tableEngine.addItem(new ComboBoxItemData(engine, engine));
        }
        // Table Resource ComboBox defaults.
        for (final String resource : TableResources.getTableResourcesList()) {
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

    /**
     * Get columnsTable values.
     *
     * @return List
     */
    private List<Map<String, String>> getColumns() {
        return columnsTableGroupWrapper.getColumnsData();
    }
}
