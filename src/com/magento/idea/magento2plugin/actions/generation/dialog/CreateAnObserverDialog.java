/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog;

import com.intellij.openapi.project.Project;
import com.magento.idea.magento2plugin.actions.generation.CreateAPluginAction;
import com.magento.idea.magento2plugin.actions.generation.CreateAnObserverAction;
import com.magento.idea.magento2plugin.actions.generation.data.ObserverEventsXmlData;
import com.magento.idea.magento2plugin.actions.generation.data.ObserverFileData;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.CreateAnObserverDialogValidator;
import com.magento.idea.magento2plugin.actions.generation.generator.ObserverClassGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.ObserverEventsXmlGenerator;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.magento.packages.File;
import com.magento.idea.magento2plugin.magento.packages.Package;
import com.magento.idea.magento2plugin.ui.FilteredComboBox;
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

@SuppressWarnings({
        "PMD.TooManyFields",
        "PMD.MissingSerialVersionUID",
        "PMD.DataClass",
        "PMD.UnusedPrivateField",
})
public class CreateAnObserverDialog extends AbstractDialog {
    @NotNull
    private final Project project;
    @NotNull
    private final CreateAnObserverDialogValidator validator;
    private final String targetEvent;
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField observerClassName;
    private JTextField observerDirectory;
    private FilteredComboBox observerModule;
    private JComboBox observerArea;
    private JTextField observerName;
    private JLabel observerClassNameLabel;
    private JLabel observerDirectoryName;
    private JLabel selectObserverModule;
    private JLabel observerAreaLabel;
    private JLabel observerNameLabel;

    /**
     * Constructor.
     *
     * @param project Project Scope
     * @param targetEvent Action Event
     */
    public CreateAnObserverDialog(@NotNull final Project project, final String targetEvent) {
        super();

        this.project = project;
        this.targetEvent = targetEvent;
        this.validator = CreateAnObserverDialogValidator.getInstance(this);

        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        fillTargetAreaOptions();

        buttonOK.addActionListener(e -> onOK());
        buttonCancel.addActionListener(e -> onCancel());

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent event) {
                onCancel();
            }
        });

        contentPane.registerKeyboardAction(
                e -> onCancel(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
        );
    }

    /**
     * Open an action dialog.
     *
     * @param project Project Scope
     * @param targetEvent Action Event
     */
    public static void open(@NotNull final Project project, final String targetEvent) {
        final CreateAnObserverDialog dialog = new CreateAnObserverDialog(project, targetEvent);
        dialog.pack();
        dialog.centerDialog(dialog);
        dialog.setVisible(true);
    }

    /**
     * Setup observer area combobox.
     */
    private void fillTargetAreaOptions() {
        for (final Package.Areas area : Package.Areas.values()) {
            observerArea.addItem(area.toString());
        }
    }

    /**
     * Perform code generation using input data.
     */
    private void onOK() {
        if (!validator.validate(project)) {
            return;
        }
        new ObserverClassGenerator(new ObserverFileData(
                getObserverDirectory(),
                getObserverClassName(),
                getObserverModule(),
                targetEvent,
                getObserverClassFqn(),
                getNamespace()
        ), project).generate(CreateAnObserverAction.ACTION_NAME, true);

        new ObserverEventsXmlGenerator(new ObserverEventsXmlData(
                getObserverArea(),
                getObserverModule(),
                targetEvent,
                getObserverName(),
                getObserverClassFqn()
        ), project).generate(CreateAPluginAction.ACTION_NAME);

        this.setVisible(false);
    }

    public String getObserverClassName() {
        return observerClassName.getText().trim();
    }

    public String getObserverName() {
        return observerName.getText().trim();
    }

    public String getObserverDirectory() {
        return this.observerDirectory.getText().trim();
    }

    public String getObserverArea() {
        return this.observerArea.getSelectedItem().toString();
    }

    public String getObserverModule() {
        return this.observerModule.getSelectedItem().toString();
    }

    private void createUIComponents() { //NOPMD
        final List<String> allModulesList = ModuleIndex.getInstance(project)
                .getEditableModuleNames();

        this.observerModule = new FilteredComboBox(allModulesList);
    }

    private String getNamespace() {
        final String targetModule = getObserverModule();
        String namespace = targetModule.replace(
                Package.VENDOR_MODULE_NAME_SEPARATOR,
                Package.FQN_SEPARATOR
        );

        namespace = namespace.concat(Package.FQN_SEPARATOR);
        return namespace.concat(getObserverDirectory().replace(
                File.separator,
                Package.FQN_SEPARATOR)
        );
    }

    private String getObserverClassFqn() {
        return getNamespace().concat(Package.FQN_SEPARATOR).concat(getObserverClassName());
    }
}

