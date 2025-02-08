/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.PhpCodeUtil;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.magento.idea.magento2plugin.actions.generation.data.ObserverFileData;
import com.magento.idea.magento2plugin.actions.generation.generator.util.DirectoryGenerator;
import com.magento.idea.magento2plugin.actions.generation.generator.util.FileFromTemplateGenerator;
import com.magento.idea.magento2plugin.actions.generation.util.CodeStyleSettings;
import com.magento.idea.magento2plugin.bundles.CommonBundle;
import com.magento.idea.magento2plugin.bundles.ValidatorBundle;
import com.magento.idea.magento2plugin.indexes.ModuleIndex;
import com.magento.idea.magento2plugin.magento.files.Observer;
import com.magento.idea.magento2plugin.magento.packages.File;
import com.magento.idea.magento2plugin.magento.packages.MagentoPhpClass;
import com.magento.idea.magento2plugin.util.GetFirstClassOfFile;
import com.magento.idea.magento2plugin.util.GetPhpClassByFQN;
import java.util.Properties;
import javax.swing.JOptionPane;

public class ObserverClassGenerator extends FileGenerator {
    private final DirectoryGenerator directoryGenerator;
    private final FileFromTemplateGenerator fileFromTemplateGenerator;
    private final GetFirstClassOfFile getFirstClassOfFile;
    private final ObserverFileData observerFileData;
    private final Project project;
    private final ValidatorBundle validatorBundle;
    private final CommonBundle commonBundle;

    /**
     * Constructor.
     *
     * @param observerFileData ObserverFileData
     * @param project Project
     */
    public ObserverClassGenerator(
            final ObserverFileData observerFileData,
            final Project project
    ) {
        super(project);
        this.observerFileData = observerFileData;
        this.project = project;

        this.directoryGenerator = DirectoryGenerator.getInstance();
        this.fileFromTemplateGenerator = new FileFromTemplateGenerator(project);
        this.getFirstClassOfFile = GetFirstClassOfFile.getInstance();
        this.validatorBundle = new ValidatorBundle();
        this.commonBundle = new CommonBundle();
    }

    @Override
    public PsiFile generate(final String actionName) {
        WriteCommandAction.runWriteCommandAction(project, () -> {
            PhpClass observerClass = GetPhpClassByFQN.getInstance(project).execute(
                    observerFileData.getObserverClassFqn()
            );

            if (observerClass == null) {
                observerClass = createObserverClass(actionName);
            }

            if (observerClass == null) {
                final String errorMessage = validatorBundle.message(
                        "validator.file.cantBeCreated",
                        "Observer Class"
                );
                JOptionPane.showMessageDialog(
                        null,
                        errorMessage,
                        commonBundle.message("common.error"),
                        JOptionPane.ERROR_MESSAGE
                );

                return;
            }

            final Properties attributes = new Properties();
            attributes.setProperty("EVENT_NAME", observerFileData.getTargetEvent());

            final String methodTemplate = PhpCodeUtil.getCodeTemplate(
                    Observer.OBSERVER_EXECUTE_TEMPLATE_NAME, attributes, project);


            final PsiFile observerFile = observerClass.getContainingFile();
            final CodeStyleSettings codeStyleSettings =
                    new CodeStyleSettings((PhpFile) observerFile);
            codeStyleSettings.adjustBeforeWrite();

            final PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(project);
            final Document document = psiDocumentManager.getDocument(observerFile);
            final int insertPos = getInsertPos(observerClass);
            document.insertString(insertPos, methodTemplate);
            final int endPos = insertPos + methodTemplate.length() + 1;
            CodeStyleManager.getInstance(project).reformatText(observerFile, insertPos, endPos);
            psiDocumentManager.commitDocument(document);
            codeStyleSettings.restore();
        });
        final PhpClass observerClass = GetPhpClassByFQN.getInstance(project).execute(
                observerFileData.getObserverClassFqn()
        );
        return observerClass.getContainingFile();
    }

    private int getInsertPos(final PhpClass observerClass) {
        int insertPos = -1;
        final LeafPsiElement[] leafElements = PsiTreeUtil.getChildrenOfType(
                observerClass,
                LeafPsiElement.class
        );
        for (final LeafPsiElement leafPsiElement: leafElements) {
            if (!MagentoPhpClass.CLOSING_TAG.equals(leafPsiElement.getText())) {
                continue;
            }
            insertPos = leafPsiElement.getTextOffset();
        }
        return insertPos;
    }

    private PhpClass createObserverClass(final String actionName) {
        PsiDirectory parentDirectory = new ModuleIndex(project)
                .getModuleDirectoryByModuleName(observerFileData.getObserverModule());

        if (parentDirectory == null) {
            return null;
        }
        final String[] observerDirectories = observerFileData.getObserverDirectory()
                .split(File.separator);
        for (final String observerDirectory: observerDirectories) {
            parentDirectory = directoryGenerator.findOrCreateSubdirectory(
                    parentDirectory,
                    observerDirectory
            );
        }

        final Properties attributes = getAttributes();
        final PsiFile observerFile = fileFromTemplateGenerator.generate(
                new Observer(observerFileData.getObserverClassName()),
                attributes,
                parentDirectory,
                actionName
        );
        if (observerFile == null) {
            return null;
        }
        return getFirstClassOfFile.execute((PhpFile) observerFile);
    }

    @Override
    protected void fillAttributes(final Properties attributes) {
        attributes.setProperty("NAME", observerFileData.getObserverClassName());
        attributes.setProperty("NAMESPACE", observerFileData.getNamespace());
    }
}
