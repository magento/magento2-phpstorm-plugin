/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.psi.PsiDirectory;
import com.magento.idea.magento2plugin.actions.context.xml.NewLayoutXmlAction;
import com.magento.idea.magento2plugin.actions.generation.data.LayoutXmlData;
import com.magento.idea.magento2plugin.actions.generation.data.ui.ComboBoxItemData;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.annotation.FieldValidation;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.annotation.RuleRegistry;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.IdentifierRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.NotEmptyRule;
import com.magento.idea.magento2plugin.actions.generation.generator.LayoutXmlTemplateGenerator;
import com.magento.idea.magento2plugin.magento.packages.Areas;
import com.magento.idea.magento2plugin.util.magento.GetModuleNameByDirectoryUtil;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import org.jetbrains.annotations.NotNull;

public class NewLayoutTemplateDialog extends AbstractDialog {

    private static final String LAYOUT_NAME = "Layout Name";

    private final @NotNull Project project;
    private final String moduleName;
    private final PsiDirectory directory;

    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;

    @FieldValidation(rule = RuleRegistry.NOT_EMPTY, message = {NotEmptyRule.MESSAGE, LAYOUT_NAME})
    @FieldValidation(rule = RuleRegistry.LAYOUT_NAME,
            message = {IdentifierRule.MESSAGE, LAYOUT_NAME})
    private JTextField layoutName;

    private JComboBox<ComboBoxItemData> area;

    // labels
    private JLabel layoutNameLabel; // NOPMD
    private JLabel areaLabel; // NOPMD
    private JLabel layoutNameErrorMessage; // NOPMD

    /**
     * NewLayoutTemplateDialog constructor.
     *
     * @param project Project
     * @param directory PsiDirectory
     */
    public NewLayoutTemplateDialog(
            final @NotNull Project project,
            final @NotNull PsiDirectory directory
    ) {
        super();

        this.project = project;
        this.moduleName = GetModuleNameByDirectoryUtil.execute(directory, project);
        this.directory = directory;

        setContentPane(contentPane);
        setModal(false);
        setTitle(NewLayoutXmlAction.ACTION_DESCRIPTION);
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

        addComponentListener(new FocusOnAFieldListener(() -> area.requestFocusInWindow()));
        autoSelectCurrentArea();
    }

    /**
     * Open a new layout template dialog.
     *
     * @param project Project
     * @param directory Directory
     */
    public static void open(
            final @NotNull Project project,
            final @NotNull PsiDirectory directory
    ) {
        final NewLayoutTemplateDialog dialog = new NewLayoutTemplateDialog(project, directory);
        dialog.pack();
        dialog.centerDialog(dialog);
        dialog.setVisible(true);
    }

    /**
     * Fire generation process if all fields are valid.
     */
    private void onOK() {
        if (validateFormFields()) {
            final String[] layoutNameParts = getLayoutNameParts();
            new LayoutXmlTemplateGenerator(
                    new LayoutXmlData(
                            getArea(),
                            layoutNameParts[0],
                            moduleName,
                            layoutNameParts[1],
                            layoutNameParts[2]
                    ),
                    project
            ).generate(NewLayoutXmlAction.ACTION_NAME, true);
            exit();
        }
    }

    /**
     * Create custom components and fill their entries.
     */
    @SuppressWarnings({"PMD.UnusedPrivateMethod", "PMD.AvoidInstantiatingObjectsInLoops"})
    private void createUIComponents() {
        area = new ComboBox<>();

        for (final Areas areaEntry : Areas.values()) {
            if (!areaEntry.equals(Areas.adminhtml) && !areaEntry.equals(Areas.frontend)) {
                continue;
            }
            area.addItem(new ComboBoxItemData(areaEntry.toString(), areaEntry.toString()));
        }
    }

    private void autoSelectCurrentArea() {
        final String selectedDirName = directory.getName();
        final Map<String, Integer> areaIndexMap = new HashMap<>();

        for (int i = 0; i < area.getItemCount(); i++) {
            final ComboBoxItemData item = area.getItemAt(i);
            areaIndexMap.put(item.getKey(), i);
        }

        if (areaIndexMap.containsKey(selectedDirName)) {
            area.setSelectedIndex(areaIndexMap.get(selectedDirName));
        }
    }

    /**
     * Get parts of inserted layout name.
     *
     * @return String[]
     */
    private String[] getLayoutNameParts() {

        final String[] layoutNameParts = layoutName.getText().trim().split("_");
        String routeName = "";
        String controllerName = "";
        String actionName = "";

        if (layoutNameParts.length >= 1) { // NOPMD
            routeName = layoutNameParts[0];
        }

        if (layoutNameParts.length == 3) { // NOPMD
            controllerName = layoutNameParts[1];
            actionName = layoutNameParts[2];
        }

        if (layoutNameParts.length == 2 || layoutNameParts.length > 3) { // NOPMD
            routeName = layoutName.getText().trim();
        }

        return new String[]{routeName, controllerName, actionName};
    }

    /**
     * Get area.
     *
     * @return String
     */
    private String getArea() {
        return area.getSelectedItem().toString();
    }
}
