/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.ui.DocumentAdapter;
import com.intellij.util.indexing.FileBasedIndex;
import com.magento.idea.magento2plugin.actions.context.php.NewObserverAction;
import com.magento.idea.magento2plugin.actions.generation.ModuleObserverData;
import com.magento.idea.magento2plugin.actions.generation.data.ObserverEventsXmlData;
import com.magento.idea.magento2plugin.actions.generation.data.ui.ComboBoxItemData;
import com.magento.idea.magento2plugin.actions.generation.dialog.reflection.GetReflectionFieldUtil;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.annotation.FieldValidation;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.annotation.RuleRegistry;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.DirectoryRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.IdentifierWithColonRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.NotEmptyRule;
import com.magento.idea.magento2plugin.actions.generation.dialog.validator.rule.PhpClassRule;
import com.magento.idea.magento2plugin.actions.generation.generator.ModuleObserverGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.ObserverEventsXmlGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.DirectoryGenerator;
import com.magento.idea.magento2plugin.lang.roots.MagentoTestSourceFilter;
import com.magento.idea.magento2plugin.magento.files.ModuleObserverFile;
import com.magento.idea.magento2plugin.magento.packages.Areas;
import com.magento.idea.magento2plugin.magento.packages.Package;
import com.magento.idea.magento2plugin.stubs.indexes.EventNameIndex;
import com.magento.idea.magento2plugin.ui.FilteredComboBox;
import com.magento.idea.magento2plugin.util.CamelCaseToSnakeCase;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({
        "PMD.TooManyFields",
        "PMD.ExcessiveImports",
        "PMD.AvoidInstantiatingObjectsInLoops",
        "PMD.ReturnEmptyCollectionRatherThanNull"
})
public class NewObserverDialog extends AbstractDialog {

    private static final String OBSERVER_NAME = "Observer Name";
    private static final String CLASS_NAME = "Class Name";
    private final Project project;
    private final PsiDirectory baseDir;
    private final String moduleName;
    private final String modulePackage;
    private JPanel contentPanel;
    private JButton buttonOK;
    private JButton buttonCancel;

    @FieldValidation(rule = RuleRegistry.NOT_EMPTY,
            message = {NotEmptyRule.MESSAGE, OBSERVER_NAME})
    @FieldValidation(rule = RuleRegistry.IDENTIFIER_WITH_COLON,
            message = {IdentifierWithColonRule.MESSAGE, OBSERVER_NAME})
    private JTextField observerName;
    @FieldValidation(rule = RuleRegistry.NOT_EMPTY,
            message = {NotEmptyRule.MESSAGE, CLASS_NAME})
    @FieldValidation(rule = RuleRegistry.PHP_CLASS,
            message = {PhpClassRule.MESSAGE, CLASS_NAME})
    private JTextField className;
    private JTextField directoryStructure;
    private FilteredComboBox eventName;
    private JComboBox<ComboBoxItemData> observerArea;
    private JLabel observerNameLabel;// NOPMD
    private JLabel observerNameErrorMessage;// NOPMD
    private JLabel classNameLabel;// NOPMD
    private JLabel classNameErrorMessage;// NOPMD
    private JLabel directoryStructureLabel;// NOPMD
    private JLabel directoryStructureErrorMessage;// NOPMD
    private JLabel eventNamesLabel;// NOPMD
    private JLabel targetAreaLabel;// NOPMD

    /**
     * Constructor.
     *
     * @param project Project
     * @param directory PsiDirectory
     * @param modulePackage String
     * @param moduleName String
     */
    public NewObserverDialog(
            final Project project,
            final PsiDirectory directory,
            final String modulePackage,
            final String moduleName
    ) {
        super();

        this.project = project;
        this.baseDir = directory;
        this.modulePackage = modulePackage;
        this.moduleName = moduleName;

        setContentPane(contentPanel);
        setModal(false);
        setTitle(NewObserverAction.ACTION_DESCRIPTION);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener((final ActionEvent event) -> onOK());
        buttonCancel.addActionListener((final ActionEvent event) -> onCancel());

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
                (final ActionEvent event) -> onCancel(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
        );

        className.getDocument().addDocumentListener(new DocumentAdapter() {
            @SuppressWarnings("PMD.AccessorMethodGeneration")
            @Override
            public void textChanged(final @NotNull DocumentEvent event) {
                autoCompleteObserverName();
            }
        });

        addComponentListener(
                new FocusOnAFieldListener(() -> className.requestFocusInWindow())
        );
    }

    /**
     * Open dialog.
     *
     * @param project Project
     * @param directory PsiDirectory
     */
    public static void open(
            final Project project,
            final PsiDirectory directory,
            final String modulePackage,
            final String moduleName
    ) {
        final NewObserverDialog dialog = new NewObserverDialog(
                project,
                directory,
                modulePackage,
                moduleName
        );
        dialog.pack();
        dialog.centerDialog(dialog);
        dialog.setVisible(true);
    }

    private  String getModuleName() {
        return modulePackage.concat(Package.fqnSeparator).concat(moduleName);
    }

    public String getObserverName() {
        return observerName.getText().trim();
    }

    public String getClassName() {
        return className.getText().trim();
    }

    public String getEventName() {
        return eventName.getSelectedItem().toString();
    }

    public String getObserverArea() {
        return this.observerArea.getSelectedItem().toString();
    }

    public String getDirectoryStructure() {
        return directoryStructure.getText().trim();
    }

    protected void onOK() {
        if (validateFields()) {
            PsiDirectory observerDirectory = baseDir;

            if (!getDirectoryStructure().isEmpty()) {
                observerDirectory = DirectoryGenerator.getInstance().findOrCreateSubdirectories(
                        baseDir,
                        getDirectoryStructure()
                );
            }
            new ModuleObserverGenerator(
                    new ModuleObserverData(
                            modulePackage,
                            moduleName,
                            getObserverClassFqn(),
                            getEventName(),
                            observerDirectory,
                            ModuleObserverFile.resolveClassNameFromInput(getClassName())
                    ),
                    project
            ).generate(NewObserverAction.ACTION_NAME, true);

            new ObserverEventsXmlGenerator(
                    new ObserverEventsXmlData(
                            getObserverArea(),
                            getModuleName().replace(
                                    Package.fqnSeparator,
                                    Package.vendorModuleNameSeparator
                            ),
                            getEventName(),
                            getObserverName(),
                            getObserverClassFqn().concat(Package.fqnSeparator).concat(
                                    ModuleObserverFile.resolveClassNameFromInput(getClassName())
                            )
                    ),
                    project
            ).generate(NewObserverAction.ACTION_NAME);
            exit();
        }
    }

    private boolean validateFields() {
        final PsiFile[] directoryFiles = getDirectoryFiles(baseDir);
        final Field classNameField = GetReflectionFieldUtil.getByName("className", this.getClass());

        if (directoryFiles != null && classNameField != null) {
            for (final PsiFile file : directoryFiles) {
                final String className = ModuleObserverFile.resolveClassNameFromInput(
                        getClassName()
                );

                if (file.getName().equals(className + ModuleObserverFile.EXTENSION)) {
                    showErrorMessage(
                            classNameField,
                            "Class name " + className + " already exist."
                    );

                    return false;
                }
            }
        }
        final Field directoryStructureField = GetReflectionFieldUtil.getByName(
                "directoryStructure",
                this.getClass()
        );

        if (!getDirectoryStructure().isEmpty() && directoryStructureField != null
                && !DirectoryRule.getInstance().check(getDirectoryStructure())
        ) {
            showErrorMessage(
                    directoryStructureField,
                    "The Directory Path field does not contain a valid directory."
            );

            return false;
        }

        return validateFormFields();
    }

    private PsiFile[] getDirectoryFiles(final PsiDirectory targetDirectory) {
        PsiDirectory directory = targetDirectory;

        if (!getDirectoryStructure().isEmpty()) {
            final String[] directories = getDirectoryStructure().split(Package.V_FILE_SEPARATOR);

            for (final String dir : directories) {
                final PsiDirectory subdirectory = directory.findSubdirectory(dir);

                if (subdirectory == null) {
                    return null;
                }

                directory = subdirectory;
            }
        }

        return directory.getFiles();
    }

    private String getObserverClassFqn() {
        final String folderStructureFqn = getDirectoryStructure().replace(
                Package.V_FILE_SEPARATOR, Package.fqnSeparator
        );
        String folderFqn = NewObserverAction.ROOT_DIRECTORY;

        if (!folderStructureFqn.isEmpty()) {
            folderFqn =  folderFqn.concat(Package.fqnSeparator).concat(folderStructureFqn);
        }

        return getModuleName().concat(Package.fqnSeparator).concat(folderFqn);
    }

    private void autoCompleteObserverName() {
        final String className = getClassName();

        if (className.isEmpty()) {
            return;
        }
        final String modifiedClassName = ModuleObserverFile.resolveClassNameFromInput(className);
        final String classNameInSnakeCase = CamelCaseToSnakeCase.getInstance()
                .convert(modifiedClassName);

        final String modulePackageModified = modulePackage.substring(0, 1)
                .toLowerCase(Locale.getDefault()) + modulePackage.substring(1);
        final String moduleNameModified = moduleName.substring(0, 1)
                .toLowerCase(Locale.getDefault()) + moduleName.substring(1);

        observerName.setText(
                modulePackageModified + "_" + moduleNameModified + "_" + classNameInSnakeCase
        );
    }

    @SuppressWarnings({"PMD.UnusedPrivateMethod"})
    private void createUIComponents() {
        observerArea = new ComboBox<>();

        for (final Areas areaEntry : Areas.values()) {
            observerArea.addItem(new ComboBoxItemData(areaEntry.toString(), areaEntry.toString()));
        }
        final Collection<String> events = FileBasedIndex.getInstance().getAllKeys(
                EventNameIndex.KEY, project
        );
        // Filter all events declared only for tests.
        events.removeIf(event -> {
            final Collection<VirtualFile> files = FileBasedIndex.getInstance()
                    .getContainingFiles(
                            EventNameIndex.KEY,
                            event,
                            GlobalSearchScope.allScope(project)
                    );
            final List<VirtualFile> realObservers = new ArrayList<>();

            for (final VirtualFile file : files) {
                if (!MagentoTestSourceFilter.isTestSources(file, project)) {
                    realObservers.add(file);
                }
            }

            return realObservers.isEmpty();
        });
        this.eventName = new FilteredComboBox(new ArrayList<>(events));
    }
}
