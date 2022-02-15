/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.psi.PsiDirectory;
import com.magento.idea.magento2plugin.actions.generation.NewWebApiDeclarationAction;
import com.magento.idea.magento2plugin.actions.generation.data.ui.ComboBoxItemData;
import com.magento.idea.magento2plugin.actions.generation.data.xml.WebApiXmlRouteData;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.annotation.FieldValidation;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.annotation.RuleRegistry;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.IdentifierWithForwardSlash;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.NotEmptyRule;
import com.magento.idea.magento2plugin.actions.generation.generator.xml.WebApiDeclarationGenerator;
import com.magento.idea.magento2plugin.magento.packages.HttpMethod;
import com.magento.idea.magento2plugin.magento.packages.WebApiResource;
import com.magento.idea.magento2plugin.util.magento.GetAclResourcesListUtil;
import com.magento.idea.magento2plugin.util.magento.GetModuleNameByDirectoryUtil;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("PMD.TooManyFields")
public class NewWebApiDeclarationDialog extends AbstractDialog {

    private static final String ROUTE_URL = "Route URL";

    private final @NotNull Project project;
    private final String moduleName;
    private final String classFqn;
    private final String methodName;

    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;

    @FieldValidation(rule = RuleRegistry.NOT_EMPTY, message = {NotEmptyRule.MESSAGE, ROUTE_URL})
    @FieldValidation(rule = RuleRegistry.IDENTIFIER_WITH_FORWARD_SLASH,
            message = {IdentifierWithForwardSlash.MESSAGE, ROUTE_URL})
    private JTextField routeUrl;

    private JComboBox<ComboBoxItemData> httpMethod;
    private JTextField serviceClass;
    private JTextField serviceMethod;
    private JComboBox<ComboBoxItemData> aclResource;

    // labels
    private JLabel routeUrlLabel;//NOPMD
    private JLabel httpMethodLabel;//NOPMD
    private JLabel serviceClassLabel;//NOPMD
    private JLabel serviceMethodLabel;//NOPMD
    private JLabel aclResourceLabel;//NOPMD
    private JLabel routeUrlHint;//NOPMD
    private JLabel routeUrlErrorMessage;//NOPMD

    /**
     * New WebApi declaration dialog constructor.
     *
     * @param project Project
     * @param directory PsiDirectory
     * @param classFqn String
     * @param methodName String
     */
    public NewWebApiDeclarationDialog(
            final @NotNull Project project,
            final @NotNull PsiDirectory directory,
            final @NotNull String classFqn,
            final @NotNull String methodName
    ) {
        super();

        this.project = project;
        this.moduleName = GetModuleNameByDirectoryUtil.execute(directory, project);
        this.classFqn = classFqn;
        this.methodName = methodName;

        setContentPane(contentPane);
        setModal(true);
        setTitle(NewWebApiDeclarationAction.ACTION_DESCRIPTION);
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

        fillPredefinedValuesAndDisableInputs();

        addComponentListener(new FocusOnAFieldListener(() -> routeUrl.requestFocusInWindow()));
    }

    /**
     * Open New WebApi dialog window.
     *
     * @param project Project
     * @param directory PsiDirectory
     * @param classFqn String
     * @param methodName String
     */
    public static void open(
            final @NotNull Project project,
            final @NotNull PsiDirectory directory,
            final @NotNull String classFqn,
            final @NotNull String methodName
    ) {
        final NewWebApiDeclarationDialog dialog =
                new NewWebApiDeclarationDialog(project, directory, classFqn, methodName);
        dialog.pack();
        dialog.centerDialog(dialog);
        dialog.setVisible(true);
    }

    /**
     * Fire generation process if all fields are valid.
     */
    private void onOK() {
        if (validateFormFields()) {
            new WebApiDeclarationGenerator(
                    getDialogDataObject(),
                    project
            ).generate(NewWebApiDeclarationAction.ACTION_NAME, true);
        }
        exit();
    }

    /**
     * Fill predefined values and disable corresponding inputs.
     */
    private void fillPredefinedValuesAndDisableInputs() {
        serviceClass.setText(classFqn);
        serviceClass.setEditable(false);
        serviceClass.setEnabled(false);
        serviceMethod.setText(methodName);
        serviceMethod.setEditable(false);
        serviceMethod.setEnabled(false);
    }

    /**
     * Create custom components and fill their entries.
     */
    @SuppressWarnings({"PMD.UnusedPrivateMethod", "PMD.AvoidInstantiatingObjectsInLoops"})
    private void createUIComponents() {
        httpMethod = new ComboBox<>();
        aclResource = new ComboBox<>();

        for (final String method : HttpMethod.getHttpMethodList()) {
            httpMethod.addItem(new ComboBoxItemData(method, method));
        }

        final List<String> aclResources = GetAclResourcesListUtil.execute(project);
        final List<String> defaultResources = WebApiResource.getDefaultResourcesList();
        defaultResources.addAll(aclResources);

        for (final String acl : defaultResources) {
            aclResource.addItem(new ComboBoxItemData(acl, acl));
        }
    }

    /**
     * Get dialog DTO.
     *
     * @return WebApiXmlRouteData
     */
    private WebApiXmlRouteData getDialogDataObject() {
        final String routeUrlValue = routeUrl.getText().trim()
                .replaceFirst("^/+", "")
                .replaceFirst("/+$", "");

        return new WebApiXmlRouteData(
                moduleName,
                routeUrlValue,
                httpMethod.getSelectedItem() == null
                        ? "" : httpMethod.getSelectedItem().toString().trim(),
                classFqn,
                methodName,
                aclResource.getSelectedItem() == null
                        ? "" : aclResource.getSelectedItem().toString().trim()
        );
    }
}
