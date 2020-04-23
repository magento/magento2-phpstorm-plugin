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
import com.magento.idea.magento2plugin.magento.packages.Package;
import com.magento.idea.magento2plugin.ui.FilteredComboBox;
import org.jetbrains.annotations.NotNull;
import javax.swing.*;
import java.awt.event.*;
import com.magento.idea.magento2plugin.magento.packages.File;
import java.util.List;

public class CreateAnObserverDialog extends AbstractDialog {
    @NotNull
    private final Project project;
    private String targetEvent;
    @NotNull
    private final CreateAnObserverDialogValidator validator;
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField observerClassName;
    private JLabel observerClassNameLabel;
    private JTextField observerDirectory;
    private JLabel observerDirectoryName;
    private JLabel selectObserverModule;
    private FilteredComboBox observerModule;
    private JComboBox observerArea;
    private JLabel observerAreaLabel;
    private JLabel observerNameLabel;
    private JTextField observerName;

    public CreateAnObserverDialog(@NotNull Project project, String targetEvent) {
        this.project = project;
        this.targetEvent = targetEvent;
        this.validator = CreateAnObserverDialogValidator.getInstance(this);

        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        pushToMiddle();
        fillTargetAreaOptions();

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void fillTargetAreaOptions() {
        for(Package.Areas area: Package.Areas.values()) {
            observerArea.addItem(area.toString());
        }
    }

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

    public static void open(@NotNull Project project, String targetEvent) {
        CreateAnObserverDialog dialog = new CreateAnObserverDialog(project, targetEvent);
        dialog.pack();
        dialog.setVisible(true);
    }

    private void createUIComponents() {
        List<String> allModulesList = ModuleIndex.getInstance(project).getEditableModuleNames();

        this.observerModule = new FilteredComboBox(allModulesList);
    }

    private String getNamespace() {
        String targetModule = getObserverModule();
        String namespace = targetModule.replace(Package.VENDOR_MODULE_NAME_SEPARATOR, Package.FQN_SEPARATOR);
        namespace = namespace.concat(Package.FQN_SEPARATOR);
        return namespace.concat(getObserverDirectory().replace(File.separator, Package.FQN_SEPARATOR));
    }

    private String getObserverClassFqn() {
        return getNamespace().concat(Package.FQN_SEPARATOR).concat(getObserverClassName());
    }
}

