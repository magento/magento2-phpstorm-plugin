/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlTag;
import com.magento.idea.magento2plugin.actions.generation.InjectAViewModelAction;
import com.magento.idea.magento2plugin.actions.generation.data.ViewModelFileData;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.InjectAViewModelDialogValidator;
import com.magento.idea.magento2plugin.actions.generation.generator.ModuleViewModelClassGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.code.ClassArgumentInXmlConfigGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.NamespaceBuilder;
import com.magento.idea.magento2plugin.bundles.CommonBundle;
import com.magento.idea.magento2plugin.bundles.ValidatorBundle;
import com.magento.idea.magento2plugin.magento.packages.XsiTypes;
import com.magento.idea.magento2plugin.util.magento.GetModuleNameByDirectoryUtil;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import org.jetbrains.annotations.NotNull;

public class InjectAViewModelDialog extends AbstractDialog {
    @NotNull
    private final Project project;
    @NotNull
    private final InjectAViewModelDialogValidator validator;
    private final XmlTag targetBlockTag;
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField viewModelClassName;
    private JTextField viewModelDirectory;
    private final CommonBundle commonBundle;
    private final ValidatorBundle validatorBundle;
    private JTextField viewModelArgumentName;
    private JLabel inheritClassLabel;//NOPMD
    private JLabel viewModelDirectoryLabel;//NOPMD
    private JLabel viewModelClassNameLabel;//NOPMD
    private JLabel viewModelArgumentNameLabel;//NOPMD

    /**
     * Constructor.
     *
     * @param project Project
     * @param targetBlockTag XmlTag
     */
    public InjectAViewModelDialog(
            final @NotNull Project project,
            final XmlTag targetBlockTag
    ) {
        super();

        this.project = project;
        this.targetBlockTag = targetBlockTag;
        this.validator = new InjectAViewModelDialogValidator(this);
        this.validatorBundle = new ValidatorBundle();
        this.commonBundle = new CommonBundle();

        this.viewModelArgumentName.setText("viewModel");
        this.viewModelDirectory.setText("ViewModel");

        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent event) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent event) {
                onCancel();
            }
        });

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent event) {
                onCancel();
            }
        });

        contentPane.registerKeyboardAction(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent event) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    protected void onOK() {
        if (!validator.validate(project)) {
            return;
        }
        final String moduleName = GetModuleNameByDirectoryUtil.execute(
                targetBlockTag.getContainingFile().getParent(),
                project
        );
        final NamespaceBuilder namespaceBuilder = new NamespaceBuilder(
                moduleName,
                getViewModelClassName(),
                getViewModelDirectory()
        );
        final PsiFile viewModel = new ModuleViewModelClassGenerator(new ViewModelFileData(
                getViewModelDirectory(),
                getViewModelClassName(),
                moduleName,
                namespaceBuilder.getNamespace()
        ), project).generate(InjectAViewModelAction.actionName, true);
        if (viewModel == null) {
            final String errorMessage = validatorBundle.message(
                    "validator.class.alreadyDeclared",
                    "ViewModel"
            );
            final String errorTitle = commonBundle.message("common.error");
            JOptionPane.showMessageDialog(
                    null,
                    errorMessage,
                    errorTitle,
                    JOptionPane.ERROR_MESSAGE
            );

            return;
        }

        new ClassArgumentInXmlConfigGenerator(
                        project,
                        this.getViewModelArgumentName(),
                        XsiTypes.object.toString(),
                        namespaceBuilder.getClassFqn()
                ).generate(targetBlockTag);

        this.setVisible(false);
    }

    public String getViewModelClassName() {
        return this.viewModelClassName.getText().trim();
    }

    public String getViewModelDirectory() {
        return this.viewModelDirectory.getText().trim();
    }

    public String getViewModelArgumentName() {
        return this.viewModelArgumentName.getText().trim();
    }

    /**
     * Open dialog.
     *
     * @param project Project
     * @param targetXmlTag XmlTag
     */
    public static void open(final @NotNull Project project, final XmlTag targetXmlTag) {
        final InjectAViewModelDialog dialog =
                new InjectAViewModelDialog(project, targetXmlTag);
        dialog.pack();
        dialog.centerDialog(dialog);
        dialog.setVisible(true);
    }
}
