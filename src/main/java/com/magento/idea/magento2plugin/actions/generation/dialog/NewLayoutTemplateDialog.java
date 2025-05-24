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
import org.jetbrains.annotations.Nullable;

@SuppressWarnings({
        "PMD.TooManyFields",
        "PMD.TooManyMethods",
        "PMD.ConstructorCallsOverridableMethod",
        "PMD.ExcessiveImports",
        "PMD.SingularField",
        "PMD.GodClass"
})
public class NewLayoutTemplateDialog extends AbstractDialog {

    private static final String LAYOUT_NAME = "Layout Name";

    private final Project project;
    private final String moduleName;
    private final PsiDirectory directory;

    private JPanel contentPane;

    @FieldValidation(rule = RuleRegistry.NOT_EMPTY, message = {NotEmptyRule.MESSAGE, LAYOUT_NAME})
    @FieldValidation(
            rule = RuleRegistry.LAYOUT_NAME,
            message = {IdentifierRule.MESSAGE, LAYOUT_NAME}
    )
    private JTextField layoutName;

    private JComboBox<ComboBoxItemData> area;

    // labels
    private JLabel layoutNameLabel; //NOPMD
    private JLabel areaLabel; //NOPMD
    private JLabel layoutNameErrorMessage; //NOPMD

    /**
     * Constructs a new dialog for creating a layout templates.
     *
     * @param project   The current IntelliJ project associated with the dialog.
     * @param directory The PsiDirectory where the new layout will be created.
     */
    public NewLayoutTemplateDialog(final Project project, final PsiDirectory directory) {
        super(project);

        this.project = project;
        this.moduleName = GetModuleNameByDirectoryUtil.execute(directory, project);
        this.directory = directory;

        setTitle(NewLayoutXmlAction.ACTION_DESCRIPTION);
        autoSelectCurrentArea();
        init();
    }

    /**
     * Opens the New Layout Template Dialog, initializes its components.
     *
     * @param project   The current IntelliJ project associated with the dialog.
     * @param directory The PsiDirectory where the new layout will be created.
     */
    public static void open(final Project project, final PsiDirectory directory) {
        final NewLayoutTemplateDialog dialog = new NewLayoutTemplateDialog(project, directory);
        dialog.centerDialog(dialog);
        dialog.showDialog();
    }

    /**
     * Create center panel.
     *
     * @return JComponent
     */
    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return contentPane;
    }

    /**
     * Handles the action performed when the OK button is clicked in the dialog.
     */
    protected void onWriteActionOK() {
        final String[] layoutNameParts = getLayoutNameParts();
        final LayoutXmlData layoutXmlData = new LayoutXmlData(
                getArea(),
                layoutNameParts[0],
                moduleName,
                layoutNameParts[1],
                layoutNameParts[2]
        );
        new LayoutXmlTemplateGenerator(layoutXmlData, project)
                .generate(NewLayoutXmlAction.ACTION_NAME, true);
        exit();
    }

    @SuppressWarnings({"PMD.UnusedPrivateMethod", "PMD.AvoidInstantiatingObjectsInLoops"})
    private void createUIComponents() {
        area = new ComboBox<>();

        for (final Areas areaEntry : Areas.values()) {
            if (areaEntry.equals(Areas.adminhtml) || areaEntry.equals(Areas.frontend)) {
                area.addItem(new ComboBoxItemData(areaEntry.toString(), areaEntry.toString()));
            }
        }
    }

    private void autoSelectCurrentArea() {
        final String selectedDirName = directory.getName();
        final Map<String, Integer> areaIndexMap = new HashMap<>();

        for (int i = 0; i < area.getItemCount(); i++) {
            final ComboBoxItemData item = area.getItemAt(i);
            areaIndexMap.put(item.getKey(), i);
        }

        final Integer selectedIndex = areaIndexMap.get(selectedDirName);
        if (selectedIndex != null) {
            area.setSelectedIndex(selectedIndex);
        }
    }

    @SuppressWarnings({
            "PMD.AvoidLiteralsInIfCondition"
    })
    private String[] getLayoutNameParts() {
        final String[] layoutNameParts = layoutName.getText().trim().split("_");
        String routeName = "";
        String controllerName = "";
        String actionName = "";

        if (layoutNameParts.length >= 1) {
            routeName = layoutNameParts[0];
        }

        if (layoutNameParts.length == 3) {
            controllerName = layoutNameParts[1];
            actionName = layoutNameParts[2];
        }

        if (layoutNameParts.length == 2 || layoutNameParts.length > 3) {
            routeName = layoutName.getText().trim();
        }

        return new String[]{routeName, controllerName, actionName};
    }

    private String getArea() {
        return area.getSelectedItem().toString();
    }

    private void run() {
        area.requestFocusInWindow();
    }
}
